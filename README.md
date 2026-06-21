# MediConnect

MediConnect is a healthcare appointment system with:

- a Spring Boot backend in `backend/`
- a React frontend in `frontend/`

## Project Structure

- `backend/` — Java 21 + Spring Boot 3 API
- `frontend/` — Vite + React UI
- `docker-compose.yml` — local PostgreSQL, backend, and frontend stack
- `requests.http` — sample API requests

## Run Locally

Backend:

```bash
cd backend
./mvnw spring-boot:run
```

Frontend:

```bash
cd frontend
npm install
npm run dev
```

## Docker

Start the full stack:

```bash
docker compose up --build
```

Service URLs:

- Frontend: `http://localhost:5173`
- Backend API: `http://localhost:8080/api/v1`
- Health check: `http://localhost:8080/actuator/health`

## Verification

- Backend: `cd backend && ./mvnw test`
- Frontend: `cd frontend && npm run build`

## Notes

- The repository is organized as one root project with separate `backend/` and `frontend/` folders.
- Avoid committing generated build output such as `backend/target/` or `frontend/dist/`.
