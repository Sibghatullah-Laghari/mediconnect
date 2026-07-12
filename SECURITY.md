# Security Policy & Audit – MediConnect

This document summarizes the security architecture, audit findings, and protection mechanisms implemented in MediConnect to strengthen the application's overall security posture.

---

## 🔐 Security Architecture

MediConnect follows a layered security approach that provides protection across authentication, authorization, and infrastructure.

### 1. Authentication

* **JWT-Based Authentication:** Uses stateless, short-lived JSON Web Tokens (JWTs) for user authentication.
* **Refresh Token Rotation:** Refresh tokens are securely hashed before being stored and can be used only once before being rotated.
* **Email OTP Verification:** A six-digit verification code is required during account registration and for sensitive account-related updates.

### 2. Authorization

* **Role-Based Access Control (RBAC):** Access permissions are enforced using Spring Security Method Security (`@PreAuthorize` and `@EnableMethodSecurity`).
* **Resource Ownership Validation:** Service-layer validation ensures users can access only the resources that belong to their own accounts (for example, patients can view only their own appointments).

### 3. Infrastructure Security

* **Rate Limiting:** Authentication endpoints are protected against brute-force attacks using Bucket4j.
* **Security Headers:** Standard HTTP security headers, including HSTS, CSP, and X-Content-Type-Options, are applied through a centralized security filter.
* **CORS Configuration:** Cross-Origin Resource Sharing (CORS) is restricted to explicitly configured trusted origins.

---

## 🛡️ Security Improvements

The following improvements were implemented following the latest security review:

| Finding | Improvement |
| :--- | :--- |
| **Repeated Database Queries** | Optimized the JWT authentication filter to reduce unnecessary user lookups and token processing. |
| **Exposure of Sensitive Fields** | Updated DTO mappings to prevent internal fields such as `passwordHash` and `deletedAt` from being exposed in API responses. |
| **Protection Against Brute Force** | Introduced an `AccountLockoutService` that temporarily locks accounts for 15 minutes after five consecutive failed login attempts. |
| **JWT Secret Hardening** | Enforced a minimum 32-byte `JWT_SECRET` and secure HMAC-SHA256 token signing. |
| **XSS & Injection Protection** | Applied a strict Content Security Policy (CSP) and ensured database operations use parameterized JPA queries. |

---

## 📋 Production Security Checklist

Before deploying MediConnect to production, verify the following:

* [ ] Replace `JWT_SECRET` with a strong, randomly generated secret.
* [ ] Configure a reliable production SMTP provider for OTP email delivery.
* [ ] Ensure `ALLOWED_ORIGINS` contains only trusted domains and does not use wildcard (`*`) values.
* [ ] Keep the database on a private network and avoid exposing it directly to the public internet.
* [ ] Enable HTTPS with SSL/TLS for all client-server communication.

---

## 🚨 Reporting Security Issues

If you discover a potential security vulnerability, please avoid creating a public GitHub issue. Instead, report it privately by contacting the security team at **[security@mediconnect.com](mailto:security@mediconnect.com)**.
