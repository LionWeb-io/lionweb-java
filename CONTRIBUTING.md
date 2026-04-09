# Contributing to LionWeb JVM

Thank you for your interest in contributing! This document explains how to set up your
development environment, the conventions used in this project, and the process for submitting
changes.

## Table of Contents

- [Development Setup](#development-setup)
- [Project Structure](#project-structure)
- [Building & Testing](#building--testing)
- [Code Conventions](#code-conventions)
- [Submitting Changes](#submitting-changes)

---

## Development Setup

**Prerequisites:**
- Java 11 or higher (Java 17 recommended; tests run on 17 and 21)
- Docker (required to run functional tests with Testcontainers)
- Git

**First-time setup:**

```bash
git clone https://github.com/LionWeb-io/lionweb-java.git
cd lionweb-java
./gradlew setup   # generates Protobuf and Xtext sources
```

Open the project in IntelliJ IDEA or another IDE after running `setup` so that generated
sources are on the classpath.

---

## Project Structure

| Module | Description |
|--------|-------------|
| `core` | LionCore (M2) and LionWeb (M1) Java implementations, JSON & Protobuf serializers |
| `client` | Client for LionWeb Server; in-memory server implementation |
| `extensions` | Additional serialization formats (Protobuf, LionWeb Archive) |
| `emf` | Converters between LionCore/LionWeb and Ecore/EMF |
| `emf-builtins` | Eclipse EPackage for LionCore built-in elements (Groovy build) |
| `kotlin-core` | Kotlin-idiomatic wrappers for `core` |
| `kotlin-client` | Kotlin-idiomatic wrappers for `client` |
| `gradle-plugin` | Gradle plugin to generate Java from LionWeb language definitions |
| `client-testing` | Shared testing utilities for functional tests against LionWeb Server |
| `docs-examples` | Code examples used in the documentation |

---

## Building & Testing

```bash
# Compile and run all unit tests
./gradlew check

# Run functional tests (requires Docker)
./gradlew functionalTest

# Run integration tests (downloads external test resources)
./gradlew integrationTest

# Check formatting
./gradlew spotlessCheck

# Apply formatting
./gradlew format

# Generate Javadoc
./gradlew myJavadoc

# Check for dependency updates
./gradlew dependencyUpdates
```

**Test types:**

| Type | Command | Requires Docker |
|------|---------|-----------------|
| Unit | `./gradlew test` | No |
| Functional | `./gradlew functionalTest` | Yes |
| Integration | `./gradlew integrationTest` | No |
| Performance | `./gradlew check -PincludeExpensiveTests` | No |

---

## Code Conventions

### Java

- Formatting is enforced via **Spotless** (Google Java Format). Run `./gradlew format` before
  committing.
- The target runtime is **Java 8** — do not use APIs that are unavailable in Java 8.
- Prefer returning `Optional` over `null` for optional values in public APIs.

### Kotlin

- Formatting is enforced via **ktlint**. Run `./gradlew format` before committing.
- Follow idiomatic Kotlin style; prefer data classes, extension functions, and named arguments.

### Tests

- Use **JUnit 5** for Java tests and **Kotest** for Kotlin tests.
- Place new unit tests under `src/test/java` (or `src/test/kotlin`) in the relevant module.
- Functional tests that require a running server go under `src/functionalTest`.
- New public API should be accompanied by at least one test.

### Commits & Branches

- Use descriptive branch names: `feature/short-description`, `fix/short-description`,
  `chore/short-description`.
- Write commit messages in the imperative mood ("Add X", "Fix Y", "Remove Z").
- Keep commits focused — one logical change per commit.

---

## Submitting Changes

1. Fork the repository and create a branch from `master`.
2. Make your changes, ensuring `./gradlew check` and `./gradlew spotlessCheck` pass locally.
3. Add or update tests as appropriate.
4. Open a pull request against `master` with a clear description of what was changed and why.
5. Respond to review feedback promptly.

For significant changes or new features, please open an issue first to discuss the approach
before investing time in an implementation.

---

## Questions

Feel free to open a GitHub Issue for questions, bug reports, or feature requests.
