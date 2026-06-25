# Architecture Overview - MediConnect

This document describes the high-level architecture of MediConnect, providing insights into the design patterns, data flow, and technical decisions that drive the platform.

---

## 🏗️ Design Philosophy

MediConnect is designed as a **Modular Monolith**. While it resides in a single repository and deployment unit, the code is structured with clear domain boundaries, allowing for a future transition to microservices if necessary.

### Key Patterns
- **Performance**: Optimized queries with `JOIN FETCH` to eliminate N+1 problems; asynchronous processing for emails; optimized JWT filter chain.
- **Reliability**: Scheduled cleanup services for session maintenance.
- **Stateless Security**: Leveraging JWT for authentication to support horizontal scaling.

---

## 🧱 Component Diagram

```text
[ Browser / Mobile Client ]
           |
           v
[   Nginx (SPA Hosting)   ]
           |
           v
[ Spring Boot API (Tomcat) ] <--- [ Mail Server (SMTP) ]
           |
           v
[   PostgreSQL Database    ]
```

---

## 🛠️ Backend Architecture

The backend is a Spring Boot 3 application leveraging Java 21 features.

### Layers
1. **API Layer (Controllers)**: Handles HTTP requests, performs basic DTO validation, and delegates to the service layer.
2. **Business Layer (Services)**: Contains the core business logic, complex validations, and cross-domain orchestration.
3. **Security Layer (Spring Security)**: Manages authentication filters, token validation, and role-based access control (RBAC).
4. **Data Layer (Repositories)**: Uses Spring Data JPA for object-relational mapping and database interaction.

### Identity Management
Currently, the system uses **Email-based Identity Coupling**. A User record in the auth system is linked to a Doctor or Patient profile via their email address.
*Note: A transition to Surrogate Key (UUID/Long ID) linkage is planned in the roadmap.*

---

## 🎨 Frontend Architecture

The frontend is a React 18 SPA built with Vite.

### Core Technologies
- **TanStack Query (React Query)**: Handles all server-state, including caching, background refetching, and mutation states.
- **React Hook Form**: Optimized form handling with Zod schema validation.
- **Tailwind CSS**: Utility-first styling with a custom theme.
- **Axios**: Configured with interceptors for automatic JWT refresh on 401 errors.

---

## 💾 Data Modeling & Persistence

MediConnect uses **PostgreSQL** with **Flyway** for versioned schema management.

### Key Entities
- **User**: Authentication credentials and role (PATIENT, DOCTOR, ADMIN).
- **Patient**: Medical profile linked to a User.
- **Doctor**: Professional profile (specialization, bio) linked to a User.
- **Appointment**: Junction entity linking Patient and Doctor with status tracking (PENDING, CONFIRMED, COMPLETED, CANCELLED).
- **RefreshToken**: Opaque tokens for session management.
- **EmailVerificationToken**: Short-lived OTPs for account assurance.

### Soft Deletes
The system implements a soft-delete strategy using `@SQLDelete` and `@Where` JPA annotations to ensure data can be recovered and audit trails are preserved.

---

## 🔒 Security Model

### Authentication Flow
1. **Login**: User provides email/password; server returns Access Token (JWT) and Refresh Token.
2. **Access**: Access Token is passed in the `Authorization: Bearer` header.
3. **Refresh**: When the Access Token expires, the frontend uses the Refresh Token to obtain a new pair.
4. **OTP**: Sensitive actions or registration require verification of a 6-digit code sent via email.

### Authorization
MediConnect uses Method Security (`@PreAuthorize`) and Request Matching to enforce RBAC:
- **Admin**: Full access to all management APIs.
- **Doctor**: Access to their own schedule and profile.
- **Patient**: Access to their own bookings and profile.

---

## 🚦 Deployment & DevOps

### Containerization
The platform is fully containerized using Docker:
- `backend/Dockerfile`: Multi-stage build for the Spring Boot JAR.
- `frontend/Dockerfile`: Multi-stage build using Node.js for building and Nginx for serving.
- `docker-compose.yml`: Orchestrates the App, DB, and Network.

### CI/CD
GitHub Actions workflows are configured to:
- Run backend unit and integration tests.
- Build and verify the frontend production bundle.
- Ensure container builds are valid.
