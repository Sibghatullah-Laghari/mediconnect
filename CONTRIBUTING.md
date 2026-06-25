# Contributing to MediConnect

First off, thank you for considering contributing to MediConnect! It's people like you that make MediConnect a great tool for the healthcare community.

---

## 🚦 Getting Started

1.  **Fork the Repository**: Create your own copy of the project.
2.  **Clone Locally**:
    ```bash
    git clone https://github.com/your-username/mediconnect.git
    ```
3.  **Setup Development Environment**: Follow the [SETUP.md](./SETUP.md) guide.
4.  **Create a Branch**: Use a descriptive name like `feature/appointment-cancellation` or `fix/auth-leak`.

---

## 🛠️ Development Standards

### Backend (Java/Spring Boot)
- **Code Style**: Follow standard Google Java Style Guide.
- **REST Principles**: Use appropriate HTTP methods (GET, POST, PUT, PATCH, DELETE) and status codes.
- **Validation**: Use Jakarta Bean Validation (`@NotNull`, `@NotBlank`, etc.) in DTOs.
- **Testing**: Write unit tests for new services and integration tests for new controllers.

### Frontend (React)
- **Component Design**: Prefer functional components and hooks.
- **State Management**: Use React Query for server state. Avoid global state (Redux/Context) unless absolutely necessary.
- **Styling**: Use Tailwind CSS utility classes. Avoid inline styles or raw CSS files.
- **Performance**: Memoize expensive calculations and avoid unnecessary re-renders.

---

## 🧪 Submission Process

1.  **Format Your Code**: Ensure consistent indentation and formatting.
2.  **Run Tests**:
    - Backend: `./mvnw test`
    - Frontend: `npm run lint`
3.  **Commit Changes**: Follow [Conventional Commits](https://www.conventionalcommits.org/) (e.g., `feat: add doctor specialization filter`).
4.  **Open a Pull Request**: Provide a clear description of the changes and link any related issues.

---

## 🛡️ Code of Conduct

We are committed to providing a welcoming and inspiring community for all. Please be respectful and professional in all communications.

---

## ⚖️ License

By contributing, you agree that your contributions will be licensed under the project's MIT License.
