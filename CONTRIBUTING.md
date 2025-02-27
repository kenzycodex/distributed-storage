# Contributing to DistributedStorage

Thank you for your interest in contributing to DistributedStorage! This document provides guidelines and instructions for contributing to this project.

## Code of Conduct

Please read and follow our [Code of Conduct](CODE_OF_CONDUCT.md) to foster an inclusive and respectful community.

## How Can I Contribute?

### Reporting Bugs

Before creating a bug report:

1. Check the [Issues](https://github.com/yourusername/distributed-storage/issues) page to see if the bug has already been reported
2. If you don't find an existing issue, create a new one with the "bug" label
3. Include detailed steps to reproduce the issue
4. Provide information about your environment (OS, Java version, etc.)
5. If possible, add screenshots or logs to help explain the problem

### Suggesting Enhancements

Enhancement suggestions are tracked as GitHub issues:

1. Check for existing feature requests
2. Provide a clear and detailed explanation of your suggestion
3. Explain why this enhancement would be useful
4. If applicable, include examples of how the feature would work

### Pull Requests

Follow these steps to submit a pull request:

1. Fork the repository
2. Create a branch from `main`
3. Make your changes
4. Write or update tests for the changes
5. Ensure all tests pass
6. Update documentation if necessary
7. Submit a pull request to the `main` branch

## Development Setup

### Prerequisites

- Java 17
- Maven 3.6+
- MySQL 8.0+
- Git

### Setup Instructions

1. Fork the repository
2. Clone your fork:
   ```bash
   git clone https://github.com/kenzycodex/distributed-storage.git
   cd distributed-storage
   ```
3. Add the original repository as a remote:
   ```bash
   git remote add upstream https://github.com/original-owner/distributed-storage.git
   ```
4. Create a branch for your work:
   ```bash
   git checkout -b feature/your-feature-name
   ```

### Build and Test

```bash
# Build the project
mvn clean package

# Run tests
mvn test

# Run the application in development mode
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

## Style Guidelines

### Java Code Style

- Follow standard Java naming conventions
- Use 4 spaces for indentation
- Maximum line length of 120 characters
- Always include braces for control structures (if, for, while, etc.)
- Add meaningful comments for complex logic

### Commit Messages

- Use a clear and descriptive title
- Begin with an action verb (Add, Fix, Update, Refactor, etc.)
- Keep the first line under 50 characters
- Provide more details in the body if necessary
- Reference related issues when applicable

Example:
```
Add weighted load balancing strategy

- Implements a new load balancing algorithm that assigns weights based on node capacity
- Adds configuration options for weight calculation
- Updates documentation with new strategy details

Fixes #42
```

### Documentation

- Update README.md if you add or change features
- Document public APIs with Javadoc
- Keep documentation in sync with code changes
- Use Markdown for documentation files

## Review Process

1. Each pull request requires at least one review before merging
2. Maintainers may request changes or improvements
3. All automated tests must pass
4. Code style and documentation requirements must be met

## License

By contributing to DistributedStorage, you agree that your contributions will be licensed under the same [MIT License](LICENSE) that covers the project.

## Questions?

If you have any questions or need help, feel free to open an issue with the "question" label or contact the maintainers directly.

Thank you for contributing to DistributedStorage!