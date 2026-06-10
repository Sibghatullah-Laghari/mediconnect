# MediConnect API

Production-oriented Spring Boot healthcare REST API for patient profiles, doctor directory, appointment booking, and JWT authentication.

## Highlights

- Layered architecture (controller → service → repository)
- JWT access + refresh tokens with rotation
- Email verification, password reset, account lockout
- Role-based access control with ownership checks (IDOR protection)
- Flyway database migrations
- OpenAPI / Swagger UI
- Docker Compose deployment
- GitHub Actions CI with JaCoCo coverage
- Testcontainers integration tests

## Tech Stack

| Layer | Technology |
| --- | --- |
| Runtime | Java 21, Spring Boot 3.2 |
| Security | Spring Security, JWT (jjwt) |
| Persistence | PostgreSQL, Spring Data JPA, Flyway |
| API Docs | springdoc-openapi |
| Observability | Spring Actuator |
| Testing | JUnit 5, Mockito, Testcontainers |

## Quick Start

### Prerequisites

- Java 21+
- Docker & Docker Compose (recommended)
- Maven 3.9+ (or use `./mvnw`)

### 1. Configure environment

```bash
cp .env.example .env
# Edit JWT_SECRET and database credentials
```

### 2. Start with Docker Compose

```bash
docker compose up --build
```

API: `http://localhost:8080`  
Swagger: `http://localhost:8080/swagger-ui.html`  
Health: `http://localhost:8080/actuator/health`

### 3. Run locally (without Docker)

```bash
# Start PostgreSQL, then:
cd backend
./mvnw spring-boot:run
```

## API Overview

| Method | Endpoint | Auth | Description |
| --- | --- | --- | --- |
| POST | `/api/v1/users/register` | Public | Register patient/doctor account |
| POST | `/api/auth/login` | Public | Login, returns JWT pair |
| POST | `/api/auth/refresh` | Public | Rotate refresh token |
| POST | `/api/auth/verify-email` | Public | Verify email with OTP code |
| POST | `/api/auth/forgot-password` | Public | Request password reset |
| POST | `/api/auth/reset-password` | Public | Reset password with token |
| CRUD | `/api/v1/patients/**` | JWT | Patient profiles |
| CRUD | `/api/v1/doctors/**` | JWT | Doctor directory |
| CRUD | `/api/v1/appointments/**` | JWT | Appointment lifecycle |

## Security Model

- **Authentication**: Stateless JWT (15 min access, 30 day refresh)
- **Authorization**: `@PreAuthorize` role checks + service-layer ownership validation
- **Account protection**: 5-attempt lockout with 30-minute auto-unlock
- **Registration**: ADMIN role cannot be self-assigned
- **Rate limiting**: In-memory per-IP filter (100 req/min)

## Testing

```bash
cd backend
./mvnw verify
```

Integration tests use Testcontainers (requires Docker).

## Project Structure

```text
backend/src/main/java/com/mediconnect/
├── config/          # Security, OpenAPI
├── controller/      # REST endpoints
├── dto/             # Request/response records
├── exception/       # Domain exceptions + global handler
├── model/           # JPA entities
├── repository/      # Spring Data repositories
├── security/        # JWT, filters, ownership
└── service/         # Business logic
```

## License

MIT — see repository for details.
