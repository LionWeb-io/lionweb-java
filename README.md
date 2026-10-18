# LionWeb JVM

![Maven Central Version](https://img.shields.io/maven-central/v/io.lionweb/lionweb-2024.1-core)
![Build](https://github.com/LionWeb-io/lionweb-java/actions/workflows/ci.yml/badge.svg)

LionWeb JVM is the JVM implementation of the [LionWeb](https://lionweb.io) specification, providing libraries for Java and Kotlin.

The published libraries require Java 11 or higher (the Gradle plugin requires Java 17 or higher).

Building the project requires Java 17 or higher. We test on Java 17 and 25.

## Documentation

Take a look at the [Documentation](https://lionweb.io/lionweb-java).

## Status of the project

While new features are being added, and the project is evolving, the core features are solidly implemented. 
The library is mature and used in production.

The project is actively maintained. Feel free to open issues to ask questions or to request help 
or support.

For visibility on the future plans regarding this project refer to the [Roadmap](ROADMAP.md).

This library supports versions 2023.1 and 2024.1 of the specification.
Known divergences from 2024.1 are tracked in [#458](https://github.com/LionWeb-io/lionweb-jvm/issues/458),
and support for 2026.1 in [#450](https://github.com/LionWeb-io/lionweb-jvm/issues/450).

## Using the library

```kotlin
dependencies {
    implementation("io.lionweb:lionweb-2024.1-core:$lionwebVersion")
}
```

## Development

Before opening in IDEA run:

```
./gradlew setup
```

This will generate necessary classes.

See [CONTRIBUTING.md](CONTRIBUTING.md) for how to build the project, the code conventions, and how
to submit changes.

## Core
Contains LionCore (M2) and LionWeb (M1) implementations in Java, including (de-)serializers from/to JSON and Protocol Buffers.

## Emf
Contains im/exporters to convert models between LionCore &harr; Ecore and LionWeb &harr; EMF.

## Emf-builtins
Some of LionCore's built-in elements have no direct representation in Ecore.
This sub-project is an Eclipse project that defines an _EPackage_ `builtins` to host the equivalent elements in Ecore.
The language's _nsURI_ is `http://lionweb.io/lionweb-java/emf/core/builtins/2023.1`.

## Client
Functionalities to connect to the [LionWeb Server](https://github.com/LionWeb-io/lionweb-server). It also
includes an in-memory server, paired with a Client exposing the same interface as the interface for connecting to 
the LionWeb Server.

## Client-testing
Facilities to write functional tests against the LionWeb Server, started in Docker via Testcontainers, or against the in-memory server.

## Extensions
Serialization-related extensions that go beyond the core LionWeb specification:

- **Protobuf extensions** — Protobuf encoding of non-standard messages, such as bulk imports, and a client for the corresponding LionWeb Server APIs
- **LionWeb Archive (`.lwa`)** — a ZIP-based packaging format that bundles partitions, language definitions, and metadata into a single versioned file

## Kotlin-core
Kotlin-idiomatic bindings on top of `core`. Provides `BaseNode` and `BaseAnnotation` open classes, a `ContainmentList` that keeps parent/child links in sync, and extension functions for serialization.

```kotlin
dependencies {
    implementation("io.lionweb:lionweb-2024.1-kotlin-core:$lionwebVersion")
}
```

## Kotlin-client
Kotlin-idiomatic bindings on top of `client` for connecting to a LionWeb Server. Provides a high-level `LionWebClient` that works with any node, including typed `BaseNode` subclasses defined with `kotlin-core`.

```kotlin
dependencies {
    implementation("io.lionweb:lionweb-2024.1-kotlin-client:$lionwebVersion")
}
```

## Gradle Plugin
A Gradle plugin (`io.lionweb`) that generates Java source code from LionWeb language definition files and packages language JSON files into your JAR. Apply it in your build:

```kotlin
plugins {
    id("io.lionweb") version "$lionwebVersion"
}
```

The plugin exposes tasks `generateLWLanguages` and `generateLWNodeClasses`, and a `lionweb { }` extension block for configuring input/output directories, package names, primitive type mappings, and more.

## Server
A standalone LionWeb repository server backed by an in-memory store. It exposes two protocols simultaneously:

- **HTTP bulk API** (default port 9239) — standard LionWeb bulk endpoints (`/bulk/store`, `/bulk/retrieve`, `/bulk/listPartitions`, etc.) plus inspection endpoints (`/inspection/nodesByClassifier`, `/inspection/nodesByLanguage`). Accepts GZIP-compressed request bodies.
- **WebSocket delta protocol** (default port 9240) — real-time change propagation between clients.

An optional Svelte-based **web UI** (default port 9241) can be enabled for inspecting the repository in a browser.

Run via Gradle:

```
./gradlew :server:run                  # HTTP bulk + WebSocket delta
./gradlew :server:runServerWeb         # also enable the web UI
```

Or build a fat JAR and run directly:

```
./gradlew :server:shadowJar
java -jar server/build/libs/server-*-all.jar [options]
```

Key CLI options:

| Option | Default | Description |
|---|---|---|
| `--http-port` | 9239 | HTTP bulk API port |
| `--ws-port` | 9240 | WebSocket delta protocol port |
| `--web-port` | 9241 | Web UI port (requires `--web-ui`) |
| `--web-ui` | false | Enable the browser-based dashboard |
| `--repository` | `repo` | Name of the repository created on startup |

## Changelog

The Changelog is [here](CHANGELOG.md).

## Testing

The project uses unit, integration, functional and performance tests, plus JMH benchmarks.
See [TESTING.md](TESTING.md) for what each kind of test verifies and how to run it.

## Contributors

This project is part of the [LionWeb](https://lionweb.io) initiative.

The project is currently maintained by [Federico Tomassetti](https://tomassetti.me) with contributions from:
* Ulyana Tikhonova
* Meinte Boersma
* Niko Stotz
* Tiago Baptista
* Alessio Stalla

Contributions include support in the design, code contributions, code reviews, 
and bug reports.
