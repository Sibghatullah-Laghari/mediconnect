# Contributing to MediConnect

First off, thank you for considering contributing to MediConnect! Your efforts help make it a valuable tool for the healthcare community.

---

## 🚦 Getting Started

1. **Fork the Repository**: Create your own copy of the project on GitHub.
2. **Clone Locally**:
   ```bash
   git clone https://github.com/your-username/mediconnect.git
Set Up Your Development Environment: Follow the instructions in SETUP.md.

Create a Branch: Use a descriptive name, e.g., feature/appointment-cancellation or fix/auth-leak.

🛠️ Development Standards
Backend (Java/Spring Boot)
Code Style: Follow the Google Java Style Guide.

REST Principles: Use appropriate HTTP methods (GET, POST, PUT, PATCH, DELETE) and meaningful status codes.

Validation: Leverage Jakarta Bean Validation (@NotNull, @NotBlank, etc.) in DTOs.

Testing: Write unit tests for services and integration tests for controllers.

Frontend (React)
Component Design: Prefer functional components with hooks.

State Management: Use React Query for server state; avoid global state (Redux/Context) unless absolutely needed.

Styling: Apply Tailwind CSS utility classes—avoid inline styles or raw CSS files.

Performance: Memoize expensive computations and prevent unnecessary re-renders.

🧪 Submission Process
Format Your Code: Ensure consistent indentation and formatting.

Run Tests:

Backend: ./mvnw test

Frontend: npm run lint

Commit Changes: Follow Conventional Commits (e.g., feat: add doctor specialization filter).

Open a Pull Request: Provide a clear description of your changes and link any related issues.

🛡️ Code of Conduct
We are committed to fostering a welcoming and respectful community. Please be professional and considerate in all interactions.

⚖️ License
By contributing, you agree that your contributions will be licensed under the project's MIT License.
