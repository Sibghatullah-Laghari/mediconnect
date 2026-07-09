# Deployment Guide – MediConnect

This guide outlines the recommended approach for deploying MediConnect in a production environment using Docker containers.

---

# 🚀 Production Deployment

## Build the Application Images

Generate the production Docker images before deployment.

```bash
docker compose build
```

## Configure Environment Variables

Before starting the application, verify that your `.env` file contains secure production values.

Recommended secrets include:

* `JWT_SECRET` – A strong, randomly generated 256-bit secret.
* `POSTGRES_PASSWORD` – A secure password for the PostgreSQL database.
* `SPRING_MAIL_PASSWORD` – The SMTP provider's application password.

## Initialize the Database

Database migrations are executed automatically through Flyway during application startup.

For an initial deployment, start the database first:

```bash
docker compose up -d postgres

# After PostgreSQL becomes available

docker compose up -d backend frontend
```

---

# ☁️ Infrastructure Recommendations

## Database

* Use **PostgreSQL 16** or a newer version.
* A managed database service such as AWS RDS or Google Cloud SQL is recommended.
* Allocate at least **10 GB** of storage with automatic expansion enabled.

## Compute Resources

Recommended minimum resources:

* **CPU:** 2 vCPUs
* **Memory:** 4 GB RAM (approximately 1–2 GB reserved for the backend JVM)

## Network & Security

* Enable **HTTPS** for all external traffic.
* Terminate SSL/TLS using a load balancer or reverse proxy such as AWS ALB or Nginx.
* Place the PostgreSQL instance inside a private network or subnet to prevent direct public access.

---

# 🔄 CI/CD

The repository includes a GitHub Actions workflow located in `.github/workflows/ci.yml`.

The current pipeline performs the following tasks:

* Validates every pull request.
* Executes unit and integration tests.
* Confirms successful Docker image builds.

For production deployments, consider extending the pipeline to include:

* Publishing Docker images to a private container registry (AWS ECR, Docker Hub, etc.).
* Performing rolling or blue-green deployments using Kubernetes, Amazon ECS, or another orchestration platform.

---

# 📈 Monitoring

## Spring Boot Actuator

The following endpoints are available for monitoring application health and metrics:

* **Health:** `/api/v1/actuator/health`
* **Metrics:** `/api/v1/actuator/metrics`

## Application Logs

When the production profile is enabled, backend logs are written to standard output in JSON format, making them compatible with centralized logging platforms such as ELK Stack or Amazon CloudWatch.

## Scheduled Maintenance

`TokenCleanupService` executes automatically once each day at midnight to remove expired refresh tokens and verification records from the database.

---

# ✅ Production Checklist

Before deploying MediConnect, verify the following:

* Secure environment variables have been configured.
* HTTPS is enabled.
* The database is not publicly accessible.
* Flyway migrations complete successfully.
* Health endpoints report the application as healthy.
* CI pipeline passes before releasing new versions.
