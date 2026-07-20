# Security Policy & Audit – MediConnect

This document provides an overview of the security architecture, audit findings, and protection mechanisms implemented in MediConnect to improve the application's overall security posture.

> **📅 Audit Snapshot:** This policy was last reviewed on **2026-07-16** – all findings and improvements listed below are up to date as of this date.

---

## 🔐 Security Architecture

MediConnect uses a layered security approach to provide protection across authentication, authorization, and infrastructure.

### 1. Authentication

* **JWT-Based Authentication:** Uses stateless and short-lived JSON Web Tokens (JWTs) for user authentication.
* **Refresh Token Rotation:** Refresh tokens are securely hashed before storage and can be used only once before rotation.
* **Email OTP Verification:** A six-digit verification code is required during account registration and for sensitive account-related changes.

> 💡 *Best practice:* The OTP expires after 5 minutes – this window is deliberately short to reduce the risk of replay attacks.

### 2. Authorization

* **Role-Based Access Control (RBAC):** Access permissions are enforced through Spring Security Method Security (`@PreAuthorize` and `@EnableMethodSecurity`).
* **Resource Ownership Validation:** Service-layer checks ensure users can access only resources associated with their own accounts (for example, patients can view only their own appointments).

> 📌 *Note:* All authorization decisions are logged for audit trails – check the `security_audit_log` table in production.

### 3. Infrastructure Security

* **Rate Limiting:** Authentication endpoints are protected from brute-force attacks using Bucket4j.
* **Security Headers:** Standard HTTP security headers, including HSTS, CSP, and X-Content-Type-Options, are configured through a centralized security filter.
* **CORS Configuration:** Cross-Origin Resource Sharing (CORS) is limited to explicitly configured trusted origins.

---

## 🛡️ Security Improvements (Latest Review)

The following improvements were introduced after the latest security review:

| Finding                            | Improvement                                                                                                                   |
| :--------------------------------- | :---------------------------------------------------------------------------------------------------------------------------- |
| **Repeated Database Queries**      | Improved the JWT authentication filter to reduce unnecessary user lookups and token processing.                               |
| **Exposure of Sensitive Fields**   | Updated DTO mappings to prevent internal fields such as `passwordHash` and `deletedAt` from appearing in API responses.       |
| **Protection Against Brute Force** | Added an `AccountLockoutService` that temporarily locks accounts for 15 minutes after five consecutive failed login attempts. |
| **JWT Secret Hardening**           | Enforced a minimum 32-byte `JWT_SECRET` with secure HMAC-SHA256 token signing.                                                |
| **XSS & Injection Protection**     | Applied a strict Content Security Policy (CSP) and ensured database operations use parameterized JPA queries.                 |

> 🔍 *Deep dive:* The lockout mechanism uses a Redis-backed cache in production to survive restarts – fallback to in‑memory cache is used in development.

---

## 📋 Production Security Checklist:-

Before deploying MediConnect to a production environment, verify the following:

- [ ] Replace `JWT_SECRET` with a strong and randomly generated secret (e.g., `openssl rand -hex 32`).
- [ ] Configure a reliable production SMTP provider for OTP email delivery – avoid Gmail for production.
- [ ] Confirm that `ALLOWED_ORIGINS` contains only trusted domains and does not use wildcard (`*`) values.
- [ ] Keep the database on a private network and avoid exposing it directly to the public internet.
- [ ] Enable HTTPS with SSL/TLS for all communication between clients and the server (use Let's Encrypt or a trusted CA).

> ⚠️ **Critical:** Also set `SPRING_PROFILES_ACTIVE=prod` to enable production-specific security constraints (stricter CORS, HTTPS-only cookies, etc.).

---

## 🚨 Reporting Security Issues

If you discover a potential security vulnerability, please avoid opening a public GitHub issue. Instead, report the issue privately by contacting the security team at **[security@mediconnect.com](mailto:security@mediconnect.com)**.

> 📌 *Response SLA:* The team acknowledges all reports within 48 hours and provides a preliminary fix timeline within 5 business days.

---

## 📝 Recent Security Patch Notes (added 2026-07-16)

- **CVE‑2026‑0001 (mitigated):** Upgraded `spring-boot-starter-web` to 3.2.9 to address a minor DoS vulnerability in the default exception handler.
- **Logging enhancement:** Added `@Mask` annotations to sensitive fields (e.g., email, phone) in log messages to prevent PII leakage.
- **Session fixation:** Added `SessionFixationProtectionFilter` to regenerate session IDs on login (even though JWT is stateless, this protects mixed‑mode deployments).
- **Dependency audit:** Ran `owasp dependency-check` – no high‑severity vulnerabilities found in current dependencies.

---

*This document is reviewed quarterly. Last review: 2026-07-16. Next review scheduled: 2026-10-16.*
