# Security Policy & Audit – MediConnect

This document provides an overview of the security architecture, audit findings, and protection mechanisms implemented in MediConnect to improve the application's overall security posture.

---

## 🔐 Security Architecture

MediConnect uses a layered security approach to provide protection across authentication, authorization, and infrastructure.

### 1. Authentication

* **JWT-Based Authentication:** Uses stateless and short-lived JSON Web Tokens (JWTs) for user authentication.
* **Refresh Token Rotation:** Refresh tokens are securely hashed before storage and can be used only once before rotation.
* **Email OTP Verification:** A six-digit verification code is required during account registration and for sensitive account-related changes.

### 2. Authorization

* **Role-Based Access Control (RBAC):** Access permissions are enforced through Spring Security Method Security (`@PreAuthorize` and `@EnableMethodSecurity`).
* **Resource Ownership Validation:** Service-layer checks ensure users can access only resources associated with their own accounts (for example, patients can view only their own appointments).

### 3. Infrastructure Security

* **Rate Limiting:** Authentication endpoints are protected from brute-force attacks using Bucket4j.
* **Security Headers:** Standard HTTP security headers, including HSTS, CSP, and X-Content-Type-Options, are configured through a centralized security filter.
* **CORS Configuration:** Cross-Origin Resource Sharing (CORS) is limited to explicitly configured trusted origins.

---

## 🛡️ Security Improvements

The following improvements were introduced after the latest security review:

| Finding                            | Improvement                                                                                                                   |
| :--------------------------------- | :---------------------------------------------------------------------------------------------------------------------------- |
| **Repeated Database Queries**      | Improved the JWT authentication filter to reduce unnecessary user lookups and token processing.                               |
| **Exposure of Sensitive Fields**   | Updated DTO mappings to prevent internal fields such as `passwordHash` and `deletedAt` from appearing in API responses.       |
| **Protection Against Brute Force** | Added an `AccountLockoutService` that temporarily locks accounts for 15 minutes after five consecutive failed login attempts. |
| **JWT Secret Hardening**           | Enforced a minimum 32-byte `JWT_SECRET` with secure HMAC-SHA256 token signing.                                                |
| **XSS & Injection Protection**     | Applied a strict Content Security Policy (CSP) and ensured database operations use parameterized JPA queries.                 |

---

## 📋 Production Security Checklist

Before deploying MediConnect to a production environment, verify the following:

* [ ] Replace `JWT_SECRET` with a strong and randomly generated secret.
* [ ] Configure a reliable production SMTP provider for OTP email delivery.
* [ ] Confirm that `ALLOWED_ORIGINS` contains only trusted domains and does not use wildcard (`*`) values.
* [ ] Keep the database on a private network and avoid exposing it directly to the public internet.
* [ ] Enable HTTPS with SSL/TLS for all communication between clients and the server.

---

## 🚨 Reporting Security Issues

If you discover a potential security vulnerability, please avoid opening a public GitHub issue. Instead, report the issue privately by contacting the security team at **[security@mediconnect.com](mailto:security@mediconnect.com)**.
