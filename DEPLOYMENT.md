# Deployment Guide – MediConnect

This guide describes the recommended approach for deploying MediConnect to a production environment using Docker containers.

---

# 🚀 Production Deployment

## Build the Application Images

Build the production Docker images before deploying the application.

```bash
docker compose build
```

## Configure Environment Variables

Before starting the application, ensure that the `.env` file contains secure production values.

Recommended secrets include:

* `JWT_SECRET` – A strong, randomly generated 256-bit secret.
* `POSTGRES_PASSWORD` – A secure password for the PostgreSQL database.
* `SPRING_MAIL_PASSWORD` – The application password for your SMTP provider.

## Initialize the Database

Database migrations are executed automatically by Flyway during application startup.

For the initial deployment, start the database service first:

```bash
docker compose up -d postgres

# Wait until PostgreSQL is ready

docker compose up -d backend frontend
```

---

# ☁️ Infrastructure Recommendations

## Database

* Use **PostgreSQL 16** or a newer version.
* A managed database service, such as AWS RDS or Google Cloud SQL, is recommended.
* Allocate at least **10 GB** of storage with automatic storage expansion enabled.

## Compute Resources

Recommended minimum resources:

* **CPU:** 2 vCPUs
* **Memory:** 4 GB RAM (approximately 1–2 GB reserved for the backend JVM)

## Network & Security

* Enable **HTTPS** for all external traffic.
* Terminate SSL/TLS at a load balancer or reverse proxy, such as AWS ALB or Nginx.
* Place the PostgreSQL instance inside a private network or subnet to prevent direct public access.

---

# 🔄 CI/CD

The repository includes a GitHub Actions workflow located in `.github/workflows/ci.yml`.

The current pipeline performs the following tasks:

* Validates every pull request.
* Runs unit and integration tests.
* Verifies that Docker images build successfully.

For production deployments, consider extending the pipeline to include:

* Publishing Docker images to a private container registry (AWS ECR, Docker Hub, etc.).
* Performing rolling or blue-green deployments using Kubernetes, Amazon ECS, or another container orchestration platform.

---

# 📈 Monitoring

## Spring Boot Actuator

The following endpoints are available for monitoring application health and metrics:

* **Health:** `/api/v1/actuator/health`
* **Metrics:** `/api/v1/actuator/metrics`

## Application Logs

When the production profile is enabled, backend logs are written to standard output in JSON format, making them compatible with centralized logging platforms such as the ELK Stack or Amazon CloudWatch.

## Scheduled Maintenance

`TokenCleanupService` runs automatically once every day at midnight to remove expired refresh tokens and verification records from the database.

---

# ✅ Production Checklist

Before deploying MediConnect, verify the following:

* Secure environment variables are properly configured.
* HTTPS is enabled.
* The database is not publicly accessible.
* Flyway migrations complete successfully.
* Health endpoints report the application as healthy.
* The CI pipeline passes before releasing a new version.
