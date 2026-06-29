# Architecture Overview - MediConnect

*Version: 1.0.1 | Last Updated: 2026-06-29*

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
