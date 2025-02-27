# Contributor Workflow Guide

This document describes the workflow for contributing to the DistributedStorage project. It provides step-by-step guidance for new and existing contributors to understand how to effectively participate in the project.

## Getting Started

### First-time Setup

1. **Fork the repository**
    - Visit the [DistributedStorage repository](https://github.com/kenzycodex/distributed-storage)
    - Click the "Fork" button in the top-right corner
    - This creates a copy of the repository in your GitHub account

2. **Clone your fork**
   ```bash
   git clone https://github.com/your-username/distributed-storage.git
   cd distributed-storage
   ```

3. **Add upstream remote**
   ```bash
   git remote add upstream https://github.com/kenzycodex/distributed-storage.git
   ```

4. **Set up development environment**
    - Install prerequisites (Java 17, Maven, MySQL)
    - Configure the database according to README.md
    - Build the project to verify your setup works:
      ```bash
      mvn clean install
      ```

### Staying Updated

Keep your fork synchronized with the upstream repository:

```bash
git checkout main
git fetch upstream
git merge upstream/main
git push origin main
```

## Contribution Workflow

### 1. Find or Create an Issue

- Check existing [issues](https://github.com/kenzycodex/distributed-storage/issues) for something to work on
- Look for issues labeled `good first issue` if you're new
- If you have a new idea or found a bug, create a new issue first

### 2. Discuss the Approach

- Comment on the issue to indicate you'd like to work on it
- Discuss your proposed solution or ask questions
- Wait for a maintainer to assign the issue to you

### 3. Create a Branch

Follow the branch naming convention:
- `feature/issue-number-short-description` for new features
- `bugfix/issue-number-short-description` for bug fixes
- `docs/issue-number-short-description` for documentation changes

Example:
```bash
git checkout main
git pull upstream main
git checkout -b feature/123-add-file-compression
```

### 4. Make Changes

- Follow the project's code style and conventions
- Write tests for your changes
- Update documentation as needed
- Commit your changes with meaningful commit messages:
  ```bash
  git commit -m "feat: Add file compression support
  
  Implement zlib compression for files larger than 1MB to reduce
  storage requirements. Add configuration options and update docs.
  
  Fixes #123"
  ```

### 5. Run Tests

Ensure all tests pass before submitting:
```bash
mvn test
```

### 6. Push Changes

Push your branch to your fork:
```bash
git push origin feature/123-add-file-compression
```

### 7. Create a Pull Request

- Go to your fork on GitHub
- Click "Compare & pull request"
- Ensure the base repository is `kenzycodex/distributed-storage` and the base branch is `main`
- Fill in the PR template with all required information
- Link the PR to the issue by including "Fixes #123" or "Relates to #123" in the description

### 8. Address Review Feedback

- Respond to review comments
- Make requested changes
- Push additional commits to your branch
- Discuss any points of disagreement respectfully

### 9. Update Your PR (if needed)

If main has changed since you created your branch:
```bash
git checkout feature/123-add-file-compression
git fetch upstream
git merge upstream/main
git push origin feature/123-add-file-compression
```

### 10. PR Merged

Once your PR is merged:
- The maintainer will thank you for your contribution
- You'll be added to ACKNOWLEDGEMENTS.md (for significant contributions)
- The issue will be closed
- Your branch can be deleted

## Additional Guidelines

### Commit Messages

Follow the [Conventional Commits](https://www.conventionalcommits.org/) format:
- `feat:` for new features
- `fix:` for bug fixes
- `docs:` for documentation changes
- `test:` for test additions or modifications
- `refactor:` for code refactoring
- `chore:` for routine tasks, maintenance, etc.

Example:
```
feat: Add weighted round-robin load balancing strategy

Implement a new load balancing algorithm that assigns weights to nodes
based on their capacity and current load. Includes configuration options
and documentation.

Resolves #456
```

### Code Style

- Follow Java conventions
- Use 4 spaces for indentation
- Maximum line length of 120 characters
- Include JavaDoc comments for public methods
- Run code formatting before committing:
  ```bash
  mvn spotless:apply
  ```

### Testing

- Write unit tests for all new code
- Maintain or improve test coverage
- Consider adding integration tests for complex features

### Documentation

- Update any relevant documentation
- Add JavaDoc comments for public APIs
- Include examples for new features
- Ensure README.md stays current

## Getting Help

If you need help at any point:

- Comment on the issue you're working on
- Start a discussion in the appropriate category
- Ask specific questions in the PR

Remember, everyone in the community was new once. Don't hesitate to ask for guidance!

---

Thank you for contributing to DistributedStorage! Your efforts help make this project better for everyone.