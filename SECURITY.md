# Security Policy & Audit – MediConnect

This document summarizes the security design, audit outcomes, and protection mechanisms implemented within MediConnect to strengthen the application's overall security posture.

---

## 🔐 Security Architecture

MediConnect applies a layered security approach, ensuring protection across authentication, authorization, and infrastructure.

### 1. Authentication

* **JWT-Based Authentication:** Uses stateless, short-lived JSON Web Tokens (JWT) for user authentication.
* **Refresh Token Rotation:** Refresh tokens are securely hashed before storage and can only be used once before being replaced.
* **Email OTP Verification:** A six-digit verification code is required during account registration and for sensitive account updates.

### 2. Authorization

* **Role-Based Access Control (RBAC):** Access permissions are enforced using Spring Security Method Security (`@PreAuthorize` and `@EnableMethodSecurity`).
* **Resource Ownership Validation:** Service-layer checks ensure users can only access resources that belong to their own accounts (for example, patients can view only their own appointments).

### 3. Infrastructure Security

* **Rate Limiting:** Authentication endpoints are protected against brute-force attacks using Bucket4j.
* **Security Headers:** Standard HTTP security headers, including HSTS, CSP, and X-Content-Type-Options, are applied through a centralized filter.
* **CORS Configuration:** Cross-Origin Resource Sharing is restricted to explicitly configured trusted origins.

---

## 🛡️ Security Improvements

The following enhancements were introduced after the latest security review:

| Finding                            | Improvement                                                                                                                   |
| :--------------------------------- | :---------------------------------------------------------------------------------------------------------------------------- |
| **Repeated Database Queries**      | Improved the JWT authentication filter to minimize unnecessary user lookups and token processing.                             |
| **Exposure of Sensitive Fields**   | Updated DTO mappings to prevent internal fields such as `passwordHash` and `deletedAt` from being returned.                   |
| **Protection Against Brute Force** | Added an `AccountLockoutService` that temporarily locks accounts for 15 minutes after five consecutive failed login attempts. |
| **JWT Secret Hardening**           | Enforced a minimum 32-byte `JWT_SECRET` and secure HMAC-SHA256 signing.                                                       |
| **XSS & Injection Protection**     | Applied a strict Content Security Policy (CSP) and ensured database operations use parameterized JPA queries.                 |

---

## 📋 Production Security Checklist

Before deploying to production, verify the following:

* [ ] Replace `JWT_SECRET` with a strong, randomly generated secret.
* [ ] Configure a reliable production SMTP provider for OTP email delivery.
* [ ] Ensure `ALLOWED_ORIGINS` contains only trusted domains and does not use wildcards (`*`).
* [ ] Keep the database on a private network and avoid exposing it directly to the public internet.
* [ ] Enable HTTPS with SSL/TLS for all client-server communication.

---

## 🚨 Reporting Security Issues

If you discover a potential security vulnerability, please avoid creating a public GitHub issue. Instead, report it privately by contacting the security team at **[security@mediconnect.com](mailto:security@mediconnect.com)**.
