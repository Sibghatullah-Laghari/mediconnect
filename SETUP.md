# Setup Guide - MediConnect

This guide provides step-by-step instructions for setting up the MediConnect development environment.

---

## 🛠️ Prerequisites

Before you begin, ensure you have the following installed:
- **Docker & Docker Compose** (Recommended)
- **JDK 21** (If running backend locally)
- **Node.js 18+** & **npm** (If running frontend locally)
- **PostgreSQL 16** (If running DB locally)

---

## 🚀 Docker Setup (Recommended)

The easiest way to get started is using Docker Compose.

1.  **Clone the Repository**:
    ```bash
    git clone https://github.com/your-username/mediconnect.git
    cd mediconnect
    ```

2.  **Configure Environment Variables**:
    Create a `.env` file in the root directory and copy the contents from [Environment Variables](#-environment-variables).

3.  **Launch the Services**:
    ```bash
    docker compose up --build
    ```

4.  **Verify**:
    - Frontend: `http://localhost:5173`
    - Backend: `http://localhost:8080`
    - Postgres: `localhost:5432`

---

## 💻 Manual Setup (Development Mode)

### 1. Database
- Create a PostgreSQL database named `mediconnect`.
- Update `backend/src/main/resources/application-dev.properties` or set environment variables.

### 2. Backend
1. Navigate to the backend directory:
   ```bash
   cd backend
   ```
2. Build the project:
   ```bash
   ./mvnw clean install
   ```
3. Run the application:
   ```bash
   ./mvnw spring-boot:run
   ```

### 3. Frontend
1. Navigate to the frontend directory:
   ```bash
   cd frontend
   ```
2. Install dependencies:
   ```bash
   npm install
   ```
3. Run in development mode:
   ```bash
   npm run dev
   ```

---

## 🔑 Environment Variables

Create a `.env` file in the root with the following variables. **Note: Do not commit your real `.env` file to version control.**

```env
# Database Configuration
POSTGRES_DB=mediconnect
POSTGRES_USER=postgres
POSTGRES_PASSWORD=your_secure_password
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/mediconnect

# Security
JWT_SECRET=your_32_character_long_secret_key_here
JWT_ACCESS_TOKEN_EXPIRY_MINUTES=60
JWT_REFRESH_TOKEN_EXPIRY_DAYS=7

# Mail (SMTP)
SPRING_MAIL_HOST=smtp.gmail.com
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=your-email@gmail.com
SPRING_MAIL_PASSWORD=your-app-password
SPRING_MAIL_SMTP_AUTH=true
SPRING_MAIL_SMTP_STARTTLS=true

# Application
SPRING_PROFILES_ACTIVE=dev
ALLOWED_ORIGINS=http://localhost:5173
```

---

## 🧪 Testing

### Backend Tests
Run the Spring Boot test suite:
```bash
cd backend
./mvnw test
```

### Frontend Linting
Run ESLint to check for code quality issues:
```bash
cd frontend
npm run lint
```

---

## 🛠️ Troubleshooting

- **Database Connection Issues**: Ensure the `SPRING_DATASOURCE_URL` correctly points to the `postgres` service name when using Docker, or `localhost` if running locally.
- **CORS Errors**: Verify that `ALLOWED_ORIGINS` in your `.env` matches the URL of your frontend.
- **Mail Failures**: If using Gmail, ensure you have "App Passwords" enabled and are using the specific app password instead of your main account password.
