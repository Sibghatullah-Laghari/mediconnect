# MediConnect API

Spring Boot healthcare REST API for patient, doctor, appointment, and authentication workflows.

## Project Overview

MediConnect is a backend-focused healthcare system designed to manage core clinic workflows through secure REST APIs. The project demonstrates practical backend engineering: layered architecture, authentication, validation, persistence, API documentation, and testable service logic.

## Planned Features

- Patient registration and profile management
- Doctor profile and specialization management
- Appointment booking, status updates, and cancellation
- Role-based authentication for patients, doctors, and admins
- JWT-based login and protected endpoints
- PostgreSQL-backed persistence
- Swagger/OpenAPI documentation
- Validation and centralized exception handling

## Architecture

```text
Controller Layer
    -> Service Layer
        -> Repository Layer
            -> PostgreSQL Database
```

Recommended package structure:

```text
com.mediconnect.backend
|-- auth
|-- patient
|-- doctor
|-- appointment
|-- common
|   |-- exception
|   `-- config
`-- security
```

## Tech Stack

- Java 17+
- Spring Boot 3+
- Spring Web
- Spring Security
- Spring Data JPA
- PostgreSQL
- Maven
- JWT
- Swagger / OpenAPI

## Installation

```bash
git clone https://github.com/Sibghatullah-Laghari/mediconnect.git
cd mediconnect/backend/backend
./mvnw clean install
```

## Configuration

Update `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/mediconnect
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.jpa.hibernate.ddl-auto=update
jwt.secret=replace-with-a-secure-secret
```

## Run Locally

```bash
./mvnw spring-boot:run
```

Default API URL:

```text
http://localhost:8080
```

## API Documentation

Recommended endpoints:

| Method | Endpoint | Description |
| --- | --- | --- |
| POST | `/api/auth/register` | Register a user |
| POST | `/api/auth/login` | Authenticate and return JWT |
| GET | `/api/patients` | List patients |
| GET | `/api/doctors` | List doctors |
| POST | `/api/appointments` | Book appointment |
| PATCH | `/api/appointments/{id}/status` | Update appointment status |

When Swagger is added:

```text
http://localhost:8080/swagger-ui/index.html
```

## Database Schema

Recommended first schema:

```text
users
- id
- full_name
- email
- password_hash
- role
- created_at

patients
- id
- user_id
- date_of_birth
- phone

doctors
- id
- user_id
- specialization
- availability_status

appointments
- id
- patient_id
- doctor_id
- appointment_time
- status
- notes
```

## Screenshots

Add screenshots

## Future Improvements

- Email reminders
- Doctor availability calendar
- Admin dashboard endpoints
- Docker Compose
- CI workflow
- Integration tests with Testcontainers
- AI will be integrated.

## Contributing

Contributions are welcome after the first stable API version is complete. Please open an issue before large changes.
