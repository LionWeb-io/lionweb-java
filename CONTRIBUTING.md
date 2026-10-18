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
- Java 17 or higher to build (CI runs on Java 17 and 25)
- Docker (required to run functional tests with Testcontainers)
- Git

**First-time setup:**

```bash
git clone https://github.com/LionWeb-io/lionweb-jvm.git
cd lionweb-jvm
./gradlew setup   # generates the Xtext sources of emf-builtins
```

Open the project in IntelliJ IDEA or another IDE after running `setup` so that generated
sources are on the classpath.

---

## Project Structure

| Module | Description |
|--------|-------------|
| `core` | LionCore (M2) and LionWeb (M1) Java implementations, JSON & Protobuf serializers |
| `client` | Client for LionWeb Server; in-memory server implementation |
| `extensions` | Serialization extensions (Protobuf bulk import, LionWeb Archive) and extended client |
| `emf` | Converters between LionCore/LionWeb and Ecore/EMF |
| `emf-builtins` | Eclipse EPackage for LionCore built-in elements (Groovy build) |
| `kotlin-core` | Kotlin-idiomatic wrappers for `core` |
| `kotlin-client` | Kotlin-idiomatic wrappers for `client` |
| `gradle-plugin` | Gradle plugin to generate Java from LionWeb language definitions |
| `client-testing` | Shared testing utilities for functional tests against LionWeb Server |
| `server` | Standalone LionWeb repository server (HTTP bulk API, WebSocket delta protocol) |

---

## Building & Testing

```bash
# Compile, run all unit tests, and check formatting
./gradlew check

# Run functional tests (requires Docker)
./gradlew functionalTest

# Run integration tests (download external test resources)
./gradlew :core:integrationTest :client:integrationTest :server:integrationTest :gradle-plugin:integrationTest

# Run performance tests
./gradlew :core:performanceTest

# Check formatting
./gradlew spotlessCheck ktlintCheck

# Apply formatting
./gradlew format

# Generate Javadoc
./gradlew myJavadoc

# Check for dependency updates
./gradlew dependencyUpdates
```

**Test types:**

| Type | Command | Requires |
|------|---------|----------|
| Unit | `./gradlew test` | Nothing |
| Integration | `./gradlew <module>:integrationTest` | Network access |
| Functional | `./gradlew functionalTest` | Docker (LionWeb Server) |
| Performance | `./gradlew :core:performanceTest`, JMH benchmarks via `./gradlew :core:jmhBenchmark` | Nothing |

See [TESTING.md](TESTING.md) for what each kind of test verifies, where the tests live, and how
benchmarks and coverage reports work.

---

## Code Conventions

### Java

- Formatting is enforced via **Spotless** (Google Java Format). Run `./gradlew format` before
  committing.
- The published libraries target **Java 11** — do not use APIs that are unavailable in Java 11
  (the Gradle plugin targets Java 17).
- Prefer returning `Optional` over `null` for optional values in public APIs.

### Kotlin

- Formatting is enforced via **ktlint**. Run `./gradlew format` before committing.
- Follow idiomatic Kotlin style; prefer data classes, extension functions, and named arguments.

### Tests

- Use **JUnit 5** for Java tests and **kotlin-test** (on JUnit 5) for Kotlin tests.
- Place new unit tests under `src/test/java` (or `src/test/kotlin`) in the relevant module.
- Tests that need a LionWeb Server in Docker go under `src/functionalTest`; other tests that go
  beyond a single module in isolation go under `src/integrationTest`.
- New public API should be accompanied by at least one test.
- See [TESTING.md](TESTING.md) to choose the right kind of test.

### Commits & Branches

- Use descriptive branch names: `feature/short-description`, `fix/short-description`,
  `chore/short-description`.
- Write commit messages in the imperative mood ("Add X", "Fix Y", "Remove Z").
- Keep commits focused — one logical change per commit.

---

## Submitting Changes

1. Fork the repository and create a branch from `main`.
2. Make your changes, ensuring `./gradlew check` passes locally (it includes the formatting
   checks).
3. Add or update tests as appropriate.
4. Open a pull request against `main` with a clear description of what was changed and why.
5. Respond to review feedback promptly.

For significant changes or new features, please open an issue first to discuss the approach
before investing time in an implementation.

---

## Questions

Feel free to open a GitHub Issue for questions, bug reports, or feature requests.
