# MediConnect – Production-Ready Healthcare Management Platform

MediConnect is a modern healthcare appointment management platform built with **Spring Boot** and **React**. The application focuses on security, scalability, and long-term maintainability while providing an efficient experience for patients, healthcare professionals, and administrators.

---

# 🚀 Features

## 👤 Patient Features

* **Secure Account Registration** – Email verification through a one-time password (OTP).
* **Doctor Search** – Search for doctors by specialization, availability, and additional filters.
* **Appointment Scheduling** – Schedule appointments with automatic conflict validation.
* **Patient Dashboard** – Track upcoming appointments and manage personal healthcare information.

## 🩺 Doctor Features

* **Appointment Management** – Confirm, complete, or cancel scheduled patient appointments.
* **Professional Profile Management** – Manage qualifications, specialties, experience, and consultation fees.
* **Availability Tracking** – Track schedules and daily patient appointments.

## 🛡️ Administration

* **User Management** – Manage patient, doctor, and administrator accounts.
* **Data Integrity** – Soft-delete support to preserve historical records.
* **Platform Security** – Integrated rate limiting and account lockout mechanisms for authentication endpoints.

---

# 🏗️ Technology Stack

## Backend

* **Framework:** Spring Boot 3.2.5 (Java 21)
* **Authentication:** Stateless JWT authentication with refresh token rotation.
* **Database:** PostgreSQL 16 using Spring Data JPA.
* **Performance:** Optimized database queries with `JOIN FETCH` and asynchronous processing where appropriate.
* **Core Libraries:** Hibernate, HikariCP, Bucket4j, Lombok, and Jakarta Validation.
* **Reliability:** Centralized exception handling and scheduled maintenance tasks.

## Frontend

* **Framework:** React 18 with Vite.
* **State Management:** TanStack Query (React Query v5)..
* **UI:** Tailwind CSS with Shadcn UI components.
* **Form Validation:** React Hook Form integrated with Zod..

## DevOps

* **Containerization:** Docker using multi-stage image builds.
* **Continuous Integration:** GitHub Actions for automated project verification.
* **Monitoring:** Spring Boot Actuator for health checks and application metrics.

---

# 🔒 Security & Performance

Security is implemented across the platform through multiple layers of protection.

* **Efficient JWT Processing** – Minimizes unnecessary token parsing and redundant database queries.
* **Rate Limiting** – Protects authentication endpoints against brute-force attempts.
* **Account Lockout** – Automatically locks user accounts after repeated failed login attempts.
* **Soft Delete Support** – Maintains historical records without permanently deleting data.
* **HTTP Security Headers** – Includes CSP, HSTS, XSS protection, and other recommended security headers.

---

# 🛠️ Getting Started

Detailed installation instructions are available in **SETUP.md**.

## Quick Start

```bash
# Clone the project repository
git clone https://github.com/mediconnect/mediconnect.git.

# Create the local environment configuration
cp .env.example .env

# Build and launch the application
docker compose up --build
