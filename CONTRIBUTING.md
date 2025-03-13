# Contributing to Saga Transaction Pattern

Thank you for considering contributing to the Saga Transaction Pattern repository! This document outlines the guidelines for contributing to this project.

## Code of Conduct

By participating in this project, you agree to abide by our code of conduct. Please be respectful to other contributors and maintain a positive and inclusive environment.

## How to Contribute

1. **Fork the repository** to your GitHub account.
2. **Clone your fork** to your local machine.
3. **Create a new branch** for your feature or bug fix.
4. **Make your changes** and commit them with descriptive commit messages.
5. **Push your changes** to your fork on GitHub.
6. **Submit a pull request** from your branch to our `main` branch.

## Development Guidelines

### Code Style

- Follow standard Java code style and formatting.
- Use meaningful variable and method names.
- Add appropriate javadoc comments to classes and methods.
- Keep methods focused and smaller than 30-40 lines when possible.

### Commit Messages

- Use clear and descriptive commit messages.
- Start with a concise summary line (max 50 characters).
- Followed by a detailed explanation if necessary.

Example:
```
Add inventory compensation handling for failed payments

This implements the compensating transaction logic in the inventory service
to handle cases where payment fails after inventory has been reserved.
```

### Pull Requests

- Include a descriptive title and detailed description.
- Reference any related issues with GitHub's keywords (e.g., "Fixes #123").
- Keep PRs focused on a single concern/feature.
- Make sure all tests pass and add new tests for your code.

## Testing

- Write unit tests for new features and bug fixes.
- Ensure all tests pass before submitting a pull request.
- Consider adding integration tests for complex functionality.

## Documentation

- Update the README.md if you change functionality.
- Document new features or changes in behavior.
- Update any affected architecture diagrams or documentation.

## Issues

- Search for existing issues before creating a new one.
- Use the provided issue templates when available.
- Be specific about the problem or enhancement.
- Include steps to reproduce any bugs.

## Code Review

- Be open to feedback and constructive criticism.
- Respond to review comments promptly.
- Make requested changes and push them to your branch.

## License

By contributing to this project, you agree that your contributions will be licensed under the project's MIT License. 