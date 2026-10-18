# Testing LionWeb JVM

This document describes the kinds of tests used in this repository, what each kind is meant to
verify, where the tests live, and how to run them locally and in CI.

## Table of Contents

- [Overview](#overview)
- [Unit Tests](#unit-tests)
- [Integration Tests](#integration-tests)
- [Functional Tests](#functional-tests)
- [Performance](#performance)
- [Code Coverage](#code-coverage)
- [Continuous Integration](#continuous-integration)
- [Writing New Tests](#writing-new-tests)

---

## Overview

Tests are organized in separate Gradle source sets, one per kind of test. Each source set has its
own task, so the fast tests can be run on every change while the slower ones are run on demand.

The kinds of tests are distinguished mainly by **what they need to run**:

- **Unit tests** need nothing but the code of the module.
- **Functional tests** need an external LionWeb Server, started in **Docker**. This is what sets
  them apart: they are the slowest tests, and in CI they run in a dedicated job that builds the
  server image once and caches it.
- **Integration tests** are all the other tests that go beyond a single module in isolation:
  conformance to the shared LionWeb test suite and to the official JSON Schemas, a real Gradle
  build, a server and its clients running as separate processes. They need at most network access,
  and run in the main CI job.
- **Performance** covers both performance tests, which fail when a threshold is exceeded, and JMH
  benchmarks, which only produce measurements.

| Kind | Source set | Gradle task | What it verifies | Requires |
|------|------------|-------------|------------------|----------|
| Unit | `src/test` | `test` (part of `check`) | Single classes and small groups of classes, in isolation | Nothing |
| Integration | `src/integrationTest` | `integrationTest` | Conformance to shared test data and specification artifacts; end-to-end scenarios that do not need Docker | Network access (`core`, `client`) |
| Functional | `src/functionalTest` | `functionalTest` | The client API end to end, against a LionWeb Server in Docker (and, for comparison, the in-memory server) | Docker |
| Performance tests | `src/performanceTest` | `performanceTest` | Time and memory used by serialization stay below fixed thresholds | Nothing |
| Benchmarks | `src/performanceTest` | `jmhBenchmark` or the benchmark's `main` | Detailed measurements to guide optimization (no pass/fail) | Nothing |

The table below shows which kinds are present in each module:

| Module | Unit | Integration | Functional | Performance |
|--------|:----:|:-----------:|:----------:|:------------------------:|
| `core` | ✓ | ✓ | | ✓ |
| `emf` | ✓ | | | |
| `extensions` | ✓ | | ✓ | |
| `client` | ✓ | ✓ | ✓ | ✓ |
| `client-testing` | | | | |
| `gradle-plugin` | ✓ | ✓ | | |
| `kotlin-core` | ✓ | | | |
| `kotlin-client` | | | ✓ | |
| `server` | | ✓ | | |

`client-testing` has no tests of its own: it provides the base classes used by the functional
tests of the other modules (see [Functional Tests](#functional-tests)).

Frameworks:

- Java tests use **JUnit 5** (Jupiter), including parameterized tests.
- Kotlin unit tests use **kotlin-test** on JUnit 5; the `kotlin-client` functional tests also use
  **Kotest** assertions.
- Functional tests use **Testcontainers** to start the LionWeb Server and its PostgreSQL database.
- Benchmarks use **JMH** (Java Microbenchmark Harness).

---

## Unit Tests

**Purpose.** Unit tests verify the behavior of single classes, or of small groups of
collaborating classes, without any external dependency. They are the main safety net of the
project: they cover the LionCore (M2) and LionWeb (M1) model, validation, JSON and Protobuf
(de-)serialization, the LionWeb Archive format, the EMF conversion, the in-memory server, the
Java code generators of the Gradle plugin, and the Kotlin bindings.

**Location.** `src/test/java` or `src/test/kotlin` in each module, with test resources (for
example serialized languages and models) under `src/test/resources`.

**Characteristics.**

- Fast and deterministic: they need no network, no Docker and no running server.
- Run on every push, on every supported Java version.
- In `gradle-plugin`, some unit tests (such as `PluginFunctionalTest`) use Gradle TestKit to apply
  the plugin to a small temporary project; they still need nothing beyond the local build.

**Running them.**

```bash
./gradlew test                 # all modules
./gradlew :core:test           # a single module
./gradlew check                # unit tests plus formatting checks (Spotless, ktlint)
```

---

## Integration Tests

**Purpose.** Integration tests verify that our implementation works together with something
outside the module under test, without needing Docker. They are of two kinds:

- **Conformance tests** check our implementation against artifacts maintained outside this
  repository: the shared test data of the LionWeb initiative and the official JSON Schemas. They
  catch divergences that unit tests based on our own fixtures cannot catch.
- **End-to-end tests** exercise a complete scenario: the full Gradle plugin pipeline, or the
  server and its clients communicating over HTTP and WebSocket.

**Location.** `src/integrationTest` in `core`, `client`, `gradle-plugin` and `server`.

**What each module checks.**

- **`core`**
  - Runs the shared test suite from
    [lionweb-integration-testing](https://github.com/LionWeb-io/lionweb-integration-testing)
    (`io.lionweb.testset`): valid serialization chunks must be accepted, invalid ones rejected,
    with and without the corresponding language.
  - `SerializationSchemaValidationTest` validates the serialization chunks in
    `core/src/test/resources/serialization` against the official LionWeb JSON Schemas.
  - The task first runs `downloadIntegrationTestResources` (clones the integration-testing
    repository into `core/build/integrationTestResources`) and `downloadJsonSchemas`. Both are
    skipped when the files are already present; delete them from `core/build` to fetch them
    again.
- **`client`**
  - The `io.lionweb.client.delta` tests check that the delta protocol examples (commands, queries
    and events) from lionweb-integration-testing are deserialized to the expected Java classes.
    Message kinds not yet supported by the Java implementation are skipped, not failed.
- **`gradle-plugin`**
  - `Db2SqlCodeGenerationIntegrationTest` runs the full plugin pipeline: language JSON → generated
    Java sources → compilation → loading the generated classes and manipulating an AST through
    them.
- **`server`**
  - The `bulk` tests start the HTTP bulk server in the test JVM and exercise the bulk and
    inspection endpoints. Some of these classes are named `...FunctionalTest`, but they are
    integration tests: they do not need Docker.
  - The `integration` tests start the server and two clients as separate JVM processes, and check
    the WebSocket delta protocol (connections and partition synchronization between clients).

**Running them.**

```bash
./gradlew :core:integrationTest :client:integrationTest :server:integrationTest :gradle-plugin:integrationTest
```

The root `./gradlew integrationTest` task is a shortcut that runs only `:core:integrationTest`.

---

## Functional Tests

**Purpose.** Functional tests verify the client libraries from the point of view of a user: they
store, retrieve, inspect and delete nodes through the public API and check the results against the
[LionWeb Server](https://github.com/LionWeb-io/lionweb-server), the reference server implementation,
started in Docker. They ensure that the Java and Kotlin clients remain compatible with it, and
that the in-memory server behaves like it.

**Location.** `src/functionalTest` in `client`, `extensions` and `kotlin-client`.

**How they work.** The base classes are in the `client-testing` module, which is also published
so that other projects can write similar tests:

- `AbstractClientFunctionalTest` uses Testcontainers to start a PostgreSQL container and a LionWeb
  Server container. The server image is built from
  `client-testing/src/main/resources/clienttesting-lionweb-server-Dockerfile`, at the commit set
  by `lionwebServerCommitID` in `gradle.properties`. Containers are started once per LionWeb
  version and shared by all test classes in the same JVM.
- `AbstractClientInMemoryFunctionalTest` runs the same kind of tests against the `InMemoryServer`,
  without Docker. The `client` module runs its bulk, inspection and DB-admin tests against both.

**What they cover.**

- `client`: bulk API, history API, inspection API and DB-admin API.
- `extensions`: the additional client APIs (bulk import with JSON or Protobuf, with and without
  compression, and node trees), and uploading and downloading partitions as a ZIP or a
  directory.
- `kotlin-client`: the Kotlin `LionWebClient` with typed nodes, for both 2023.1 and 2024.1.

**Running them.** Docker must be running.

```bash
./gradlew functionalTest              # all modules
./gradlew :client:functionalTest      # a single module
```

Building the server image is the slowest step. To build it once and reuse it, build the image
yourself and set `LIONWEB_SERVER_TEST_IMAGE`, as CI does:

```bash
docker build -t lionweb-server-functional-test:local \
  --build-arg lionwebServerCommitID=$(grep '^lionwebServerCommitID=' gradle.properties | cut -d= -f2) \
  -f client-testing/src/main/resources/clienttesting-lionweb-server-Dockerfile \
  client-testing/src/main/resources
LIONWEB_SERVER_TEST_IMAGE=lionweb-server-functional-test:local ./gradlew functionalTest
```

The tests also set `TESTCONTAINERS_REUSE_ENABLE=true`, so containers can be reused between runs
when reuse is enabled in your Testcontainers configuration.

---

## Performance

Performance work uses two tools, which share the `performanceTest` source set:

- **Performance tests** guard against regressions: they are JUnit tests that fail when a
  measurement exceeds a threshold.
- **Benchmarks** support optimization work: they measure in detail, to compare before and after a
  change, and have no pass/fail outcome.

### Performance tests

**Location.** `core/src/performanceTest/java`, class `PerformanceTestOnSerialization` (tagged
`performance`).

**What they check.** JSON serialization and deserialization of a large language
(`LargeLanguage.json`) must stay under a time threshold, and deserialization must stay under a
memory allocation threshold. The thresholds are set from measured baselines with headroom for CI
variance; the baselines are written in the comments next to each threshold. When an intentional
change moves the baseline, update the comment and the threshold together.

**Running them.**

```bash
./gradlew :core:performanceTest
```

Since timing depends on the machine, a failure on a heavily loaded machine should be confirmed
by running the test again.

### Benchmarks

**Location.** JMH benchmarks:

- `core/src/performanceTest/java`
  - **DeserializeFromChunkBenchmark** — `deserializeSerializationChunk`, measuring both the core
    deserialization logic (excluding JSON parsing) and end-to-end deserialization from JSON.
  - **SerializeNodesToChunkBenchmark** — `serializeNodesToSerializationChunk`, comparing direct
    serialization of flat node collections with tree-based serialization including traversal.
  - **ProtoBufBytesBenchmark** — Protobuf binary serialization and deserialization via
    `DirectProtoBufSerializer` / `DirectProtoBufDeserializer`, measuring the cost of converting a
    `SerializationChunk` to bytes and back.
- `client/src/performanceTest/java`
  - **NodesByClassifierBenchmark** — `RepositoryData#nodesByClassifier`, the query behind the
    in-memory server's inspection API, on a small and a large repository.

The benchmarks use the JMH GC profiler, so they report memory allocation as well as time; this
is useful to find unnecessary allocations.

**Running them.**

```bash
./gradlew :core:jmhBenchmark    # runs ProtoBufBytesBenchmark with the GC profiler
```

To run any benchmark, execute the `main` method of its class from your IDE: each `main` configures
JMH (forks, warmup, iterations, profilers) for that benchmark.

Benchmarks take minutes and are not run in CI. Run them on a quiet machine, and compare results
obtained on the same machine and Java version.

---

## Code Coverage

Coverage is measured with **JaCoCo** in `core`, `client` and `extensions`.

```bash
./gradlew jacocoTestReport
```

The reports are generated for each module under `build/reports/jacoco`. In `core` the report is
produced automatically after `test`.

On each pull request, the *Measure code coverage* workflow runs the unit tests and comments on the
pull request with the coverage. It requires at least 40% overall coverage and 60% coverage of the
changed files.

---

## Continuous Integration

| Workflow | Trigger | What runs |
|----------|---------|-----------|
| `ci.yml` — Build and Test | Every push | `check` and the integration tests of `core`, `client`, `server` and `gradle-plugin`, on Java 17 and 25; Javadoc generation on Java 17 |
| `ci.yml` — Functional Tests | Every push | `functionalTest` on Java 17 and 25, with the LionWeb Server image built once and cached |
| `coverage.yml` | Pull requests | Unit tests with JaCoCo coverage report on the pull request |
| `performance.yml` | Nightly and on demand | Performance tests on Java 17 |

Test results are uploaded as workflow artifacts, so failures can be inspected without re-running
the build.

---

## Writing New Tests

- New public API should be accompanied by at least one unit test.
- Choose the kind of test by what it needs:
  - Only the code of the module → unit test in `src/test`.
  - A LionWeb Server in Docker → functional test in `src/functionalTest`, extending
    `AbstractClientFunctionalTest` (and, where useful, a twin test extending
    `AbstractClientInMemoryFunctionalTest`).
  - Anything else beyond a single module in isolation (shared test data, specification artifacts,
    a real Gradle build, separate processes) → integration test in `src/integrationTest`.
  - A guard against a performance regression → performance test with a threshold based on a
    measured baseline; to explore an optimization → JMH benchmark.
- Keep unit tests fast and independent of the environment: no network, no fixed ports, no
  dependency on the order in which tests run.
- Put test data in the `resources` directory of the matching source set.
