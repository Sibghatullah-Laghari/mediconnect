# Security Policy & Audit - MediConnect

This document outlines the security architecture, audit findings, and hardening measures implemented in MediConnect.

---

## 🔐 Security Architecture

MediConnect follows a defense-in-depth strategy, integrating security at multiple layers of the stack.

### 1. Authentication
- **Stateless JWT**: Uses short-lived JSON Web Tokens for authentication.
- **Refresh Token Rotation**: Hashed refresh tokens stored in the database with one-time-use logic (revoked on use).
- **OTP Verification**: Registration and sensitive profile changes require 6-digit email verification.

### 2. Authorization
- **Role-Based Access Control (RBAC)**: Enforced via Spring Security Method Security (`@PreAuthorize`, `@EnableMethodSecurity`).
- **Domain Scoping**: Service layer ensures users can only access data they own (e.g., Patients can only see their own appointments).

### 3. Infrastructure Security
- **Rate Limiting**: Protected against brute-force attacks via `Bucket4j` on auth endpoints.
- **Security Headers**: Standard headers (HSTS, CSP, No-Sniff) are enforced via a global filter.
- **CORS**: Strict CORS policy allowing only configured origins.

---

## 🛡️ Hardening Measures (Audit Fixes)

During the recent security audit, the following improvements were implemented:

| Issue | Resolution |
| :--- | :--- |
| **Redundant DB Lookups** | Optimized JWT Filter to reduce redundant user loading and token parsing. |
| **Sensitive Data Exposure** | Ensured DTOs never expose internal fields like `passwordHash` or `deletedAt`. |
| **Account Lockout** | Implemented `AccountLockoutService` to lock accounts after 5 failed attempts (15 min duration). |
| **Token Hijacking** | Enforced 32-byte minimum length for `JWT_SECRET` and secure HMAC-SHA256 signing. |
| **XSS / Injection** | Enforced strict Content Security Policy (CSP) and parameterized JPA queries. |

---

## 📋 Security Checklist for Production

- [ ] Change `JWT_SECRET` to a cryptographically strong, random string.
- [ ] Use a production-grade SMTP server for OTP delivery.
- [ ] Ensure `ALLOWED_ORIGINS` does not include wildcards (`*`).
- [ ] Run the database on a private network, not exposed to the internet.
- [ ] Enable SSL/TLS for all traffic.

---

## 🚨 Reporting a Vulnerability

If you find a security vulnerability, please do not open a public issue. Instead, contact the security team at security@mediconnect.com.
