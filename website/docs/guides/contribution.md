---
sidebar_position: 3
---

# Contributing to LionWeb Java

Thank you for your interest in contributing to LionWeb Java! This document provides guidelines and instructions for contributing to the project.

## Code of Conduct

By participating in this project, you agree to abide by our Code of Conduct. Please be respectful and considerate of others.

## Getting Started

1. Fork the repository
2. Clone your fork
3. Set up the development environment:
   ```bash
   ./gradlew setup
   ```
4. Create a new branch for your changes

## Development Workflow

1. **Create an Issue**: Before starting work, create an issue describing the problem or feature
2. **Branch Naming**: Use descriptive branch names (e.g., `fix/issue-123` or `feature/new-feature`)
3. **Code Style**: Follow the project's code style guidelines
   - Use the provided spotless configuration
   - Run `./gradlew spotlessApply` before committing

## Making Changes

1. **Write Tests**: Ensure your changes are covered by tests
2. **Update Documentation**: Keep documentation up to date with your changes
3. **Commit Messages**: Write clear, descriptive commit messages
4. **Pull Request**: Create a pull request when ready for review

## Testing

- Run all tests: `./gradlew test`
- Generate test coverage report: `./gradlew jacocoTestReport`
- Run specific tests: `./gradlew :module:test --tests "TestClass"`

## Pull Request Process

1. Update the CHANGELOG.md
2. Ensure all tests pass
3. Update documentation if needed
4. Request review from maintainers
5. Address any feedback
6. Wait for approval before merging

## Release Process

1. Update version in gradle.properties
2. Update CHANGELOG.md
3. Create a release tag
4. Build and publish artifacts

## Documentation

- Keep documentation up to date
- Add new documentation for new features
- Update existing documentation for changes

## Questions?

If you have any questions, feel free to:
- Open an issue
- Contact the maintainers
- Join our community discussions

## Recognition

All contributors will be recognized in:
- The project's README
- Release notes
- Project documentation 