# Deployment Guide - MediConnect

MediConnect is designed to be deployed in a containerized environment. This guide covers the steps for production deployment.

---

## 🚢 Containerized Deployment (Recommended)

### 1. Build Production Images
```bash
docker compose build
```

### 2. Configure Production Secrets
Ensure your `.env` file contains production-grade secrets:
- `JWT_SECRET`: Random 256-bit string.
- `POSTGRES_PASSWORD`: Strong password for DB.
- `SPRING_MAIL_PASSWORD`: App-specific password for SMTP.

### 3. Database Management
Flyway will automatically run migrations on startup. For the first deployment:
```bash
docker compose up -d postgres
# Wait for DB to be ready
docker compose up -d backend frontend
```

---

## ☁️ Cloud Infrastructure Requirements

### Database
- **PostgreSQL 16+**: Managed service recommended (AWS RDS, Google Cloud SQL).
- **Storage**: Minimum 10GB with auto-growth.

### Compute
- **CPU**: 2 vCPUs minimum for backend.
- **RAM**: 4GB minimum (Backend JVM requires ~1-2GB).

### Networking
- **SSL/TLS**: Mandatory for all clinical data. Use a Load Balancer (AWS ALB, Nginx Ingress) for SSL termination.
- **VPC**: Database should be in a private subnet.

---

## 🔄 CI/CD Pipeline

The project includes a GitHub Actions workflow in `.github/workflows/ci.yml` that:
1. Validates the build on every PR.
2. Runs unit and integration tests.
3. Verifies container build compatibility.

For production CD, it is recommended to extend this to:
1. Push images to a private registry (AWS ECR, Docker Hub).
2. Trigger a blue-green or rolling update on your orchestrator (Kubernetes, ECS).

---

## 📈 Monitoring & Maintenance

### Actuator Endpoints
- Health: `/api/v1/actuator/health`
- Metrics: `/api/v1/actuator/metrics`

### Logs
Backend logs are configured to output to `stdout` in JSON format (when `prod` profile is active) for consumption by ELK or CloudWatch.

### Token Cleanup
The `TokenCleanupService` runs daily at midnight to prune expired sessions from the database.
