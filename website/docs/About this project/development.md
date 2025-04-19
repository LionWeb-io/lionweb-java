---
sidebar_position: 31
---

# Contributing to LionWeb Java

Thank you for your interest in contributing to LionWeb Java! This document provides guidelines and instructions for contributing to the project.


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
   - Run `./gradlew spotlessApply` before committing

## Making Changes

1. **Write Tests**: Ensure your changes are covered by tests
2. **Update Documentation**: Keep documentation up to date with your changes
3. **Commit Messages**: Write clear, descriptive commit messages
4. **Pull Request**: Create a pull request when ready for review

## Testing

- Run all tests: `./gradlew check && ./gradlew functionalTest`
- Generate test coverage report: `./gradlew jacocoTestReport`

## Pull Request Process

1. Update the CHANGELOG.md
2. Ensure all tests pass
3. Update documentation if needed
4. Request review from maintainers
5. Address any feedback
6. Merging has to be performed by the proponent, if he has the necessary privileges, otherwise from the maintainers

## Release Process

1. Update CHANGELOG.md
2. Run `./gradlew release`
