# Setup Guide – MediConnect

This guide describes how to configure and run the MediConnect project for local development using Docker or a manual setup.

---

# 🛠️ Prerequisites

Before starting, ensure the following software is installed on your system:

* **Docker & Docker Compose** (recommended for a quick setup)
* **Java Development Kit (JDK 21)** for running the backend locally
* **Node.js 18 or later** with **npm** for the frontend
* **PostgreSQL 16** if you intend to use a local database instead of Docker

---

# 🚀 Running with Docker (Recommended)

Docker offers the fastest and most convenient approach to launch the complete application.

### 1. Clone the Repository

```bash
git clone https://github.com/your-username/mediconnect.git
cd mediconnect
```

### 2. Configure Environment Variables

Create a `.env` file in the project root and add the values listed in the **Environment Variables** section below.

### 3. Build and Start the Containers

```bash
docker compose up --build
```

### 4. Verify the Services

Once all containers have started successfully, the application should be available at:

* **Frontend:** `http://localhost:5173`
* **Backend API:** `http://localhost:8080`
* **PostgreSQL:** `localhost:5432`

---

# 💻 Manual Development Setup

If you prefer to avoid Docker, each service can be started independently.

## Database

* Create a PostgreSQL database named **mediconnect**.
* Update the database settings in `backend/src/main/resources/application-dev.properties` or provide the required environment variables.

## Backend

Navigate to the backend project:

```bash
cd backend
```

Build the application:

```bash
./mvnw clean install
```

Run the Spring Boot application:

```bash
./mvnw spring-boot:run
```

## Frontend

Navigate to the frontend directory:

```bash
cd frontend
```

Install the project dependencies:

```bash
npm install
```

Launch the development server:

```bash
npm run dev
```

---

# 🔑 Environment Variables

Create a `.env` file in the project root before starting the application.

> **Important:** Never commit your real `.env` file or sensitive credentials to version control.

```env
# Database Configuration
POSTGRES_DB=mediconnect
POSTGRES_USER=postgres
POSTGRES_PASSWORD=your_secure_password
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/mediconnect

# JWT Configuration
JWT_SECRET=your_32_character_long_secret_key_here
JWT_ACCESS_TOKEN_EXPIRY_MINUTES=60
JWT_REFRESH_TOKEN_EXPIRY_DAYS=7

# Mail Configuration
SPRING_MAIL_HOST=smtp.gmail.com
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=your-email@gmail.com
SPRING_MAIL_PASSWORD=your-app-password
SPRING_MAIL_SMTP_AUTH=true
SPRING_MAIL_SMTP_STARTTLS=true

# Application Configuration
SPRING_PROFILES_ACTIVE=dev
ALLOWED_ORIGINS=http://localhost:5173
```

---

# 🧪 Running Tests

## Backend

Execute the Spring Boot test suite:

```bash
cd backend
./mvnw test
```

## Frontend

Run ESLint to check for potential code quality issues:

```bash
cd frontend
npm run lint
```

---

# 🛠️ Common Issues

### Database Connection Problems

Confirm that `SPRING_DATASOURCE_URL` uses the correct database host:

* Use **postgres** when running the application through Docker.
* Use **localhost** when connecting to a locally installed PostgreSQL instance.

### CORS Errors

Make sure the `ALLOWED_ORIGINS` value matches the URL where the frontend application is running.

### Email Configuration Issues

When using Gmail as the SMTP provider, generate and use an **App Password** rather than your regular account password. Also ensure that the SMTP settings are configured correctly.
