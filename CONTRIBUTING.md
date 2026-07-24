# Contributing to MediConnect

Thank you for your interest in contributing to MediConnect. Every contribution—whether it's fixing bugs, improving documentation, or adding new features—helps improve the project for everyone.

---

# 🚀 Getting Started.

To begin contributing:

1. **Fork the repository** to your own GitHub account.
2. **Clone your fork** to your local machine.

```bash id="n9p5ih"
git clone https://github.com/your-username/mediconnect.git
```

3. **Configure your development environment** by following the instructions provided in `SETUP.md`.
4. **Create a dedicated branch** for your work using a meaningful branch name, for example:

   * `feature/appointment-cancellation`
   * `feature/doctor-search`
   * `fix/authentication-bug`

---

# 🛠️ Development Guidelines.

## Backend (Spring Boot / Java).

* Follow the **Google Java Style Guide** for consistent code formatting.
* Design REST APIs using appropriate HTTP methods (`GET`, `POST`, `PUT`, `PATCH`, and `DELETE`).
* Return meaningful HTTP status codes and error responses.
* Use **Jakarta Bean Validation** annotations such as `@NotNull` and `@NotBlank` in request DTOs.
* Add unit tests for business logic and integration tests for REST controllers whenever applicable.

## Frontend (React).

* Prefer functional components with React Hooks.
* Manage server-side data using **TanStack Query (React Query)**.
* Avoid introducing global state unless it is clearly justified.
* Use **Tailwind CSS** for styling instead of inline styles or custom CSS whenever possible.
* Optimize rendering by memoizing expensive computations and reducing unnecessary component updates.

---

# 🧪 Before Submitting.

Before opening a pull request, make sure you have completed the following steps:

* Format and review your code.
* Execute the backend test suite.

```bash id="p7u7g0"
./mvnw test
```

* Run frontend linting...

```bash id="vlu6wn"
npm run lint
```

* Use **Conventional Commits** for commit messages, for example:

```text id="8q7z1k"
feat: add doctor specialization filter
fix: resolve appointment validation issue
docs: update setup guide
```

* Create a pull request with a clear explanation of your changes and reference any related issues when appropriate.

---

# 🤝 Community Guidelines.

Please be respectful, constructive, and professional when interacting with other contributors. We aim to maintain a welcoming environment where everyone can collaborate effectively.

---

# 📄 License.

By submitting contributions to MediConnect, you agree that your work will be distributed under the terms of the project's **MIT License**.
