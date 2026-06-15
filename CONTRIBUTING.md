# Contributing

Thank you for your interest in contributing in mediconnect.

## How To Contribute

1. Fork the repository.
2. Create a feature branch:

```bash
git checkout -b feature/short-description
```

3. Make a focused change.
4. Add or update tests where needed.
5. Run the project checks.
6. Open a pull request with a clear description.

## Development Guidelines

- Keep commits focused and descriptive.
- Follow the existing package structure and naming conventions.
- Prefer clear service-layer logic over placing business rules in controllers.
- Validate API input and return consistent error responses.
- Do not commit secrets, local database credentials, IDE metadata, or generated build files.

## Pull Request Checklist

- The change has a clear purpose.
- Tests pass locally.
- Documentation is updated when behavior changes.
- New endpoints are documented.
- Screenshots or API examples are included when helpful.

## Reporting Issues

When reporting a bug, error, or issue include:

- What happened
- What you expected
- Steps to reproduce
- Environment details
- Logs or screenshots if available
