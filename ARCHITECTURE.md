# Architecture Overview — MediConnect

*Version: 1.0.1 | Last Updated: 2026-06-29*

This document outlines the high‑level architecture of MediConnect, offering insight into the design patterns, data flows, and technical decisions that underpin the platform.

---

## 🏗️ Design Philosophy

MediConnect follows a **Modular Monolith** approach. Although it lives in a single repository and deploys as one unit, the code is organised around clear domain boundaries, which makes a future transition to microservices feasible with minimal friction.

### Key Patterns
- **Performance**: Optimised queries using `JOIN FETCH` to avoid N+1 issues; asynchronous processing for emails; streamlined JWT filter chain.
- **Reliability**: Scheduled cleanup services to maintain session hygiene.
- **Stateless Security**: JWT‑based authentication enables horizontal scaling without session affinity.

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
