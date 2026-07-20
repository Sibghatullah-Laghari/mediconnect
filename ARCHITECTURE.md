# Architecture Overview – MediConnect

**Version:** 1.0.1
**Last Updated:** 2026-06-29

This document provides an overview of MediConnect's architecture, highlighting its overall structure, design principles, and the core technologies that support the platform.

---

# 🏗️ Architectural Approach

MediConnect is built as a **Modular Monolith**, where the application is deployed as a single service while maintaining well-defined boundaries between business domains. This approach keeps development straightforward today while making future migration to a microservices architecture significantly easier.

## Core Design Principles

* **Performance-Oriented** – Database access is optimized using `JOIN FETCH` queries to eliminate N+1 problems. Asynchronous processing is used for email delivery, and the authentication pipeline is streamlined to reduce overhead.
* **Reliability** – Scheduled background services automatically clean up expired sessions and other temporary data to keep the system healthy.
* **Stateless Authentication** – JWT-based authentication removes server-side session dependencies, making the application easier to scale horizontally.

---

# 🧱 High-Level Components

```text
+---------------------------+
|  Browser / Mobile Client  |
+---------------------------+
             |
             v
+---------------------------+
|     Nginx (Frontend)      |
+---------------------------+
             |
             v
+---------------------------+        +----------------------+
|  Spring Boot REST API     |<------>|    SMTP Mail Server  |
|       (Embedded Tomcat)   |        +----------------------+
+---------------------------+
             |
             v
+---------------------------+
|    PostgreSQL Database    |
+---------------------------+
```

### Component Responsibilities:-

* **Client Applications** – Web and mobile clients communicate with the backend through REST APIs.
* **Nginx** – Serves the frontend application and forwards API requests to the backend while supporting SPA routing.
* **Spring Boot API** – Handles authentication, business logic, validation, and communication with external services.
* **SMTP Server** – Delivers OTP verification emails and other email notifications.
* **PostgreSQL** – Stores application data, user accounts, appointments, and other persistent information.
