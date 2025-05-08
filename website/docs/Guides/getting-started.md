---
sidebar_position: 40
---

# Getting Started with LionWeb Java

This guide will help you get started with LionWeb Java, from installation to creating your first project.

## Prerequisites

- Java 8 or later (Java 11 required for building)

## Installation

Note that even if you want to use the 2023.1 specs you can use a recent version of LionWeb Java. All versions supports also previous versions of the specs.

### Using Gradle

Add the following to your `build.gradle` or `build.gradle.kts`:

```kotlin
dependencies {
    implementation("io.lionweb.lionweb-java:lionweb-java-2024.1-core:$lionwebVersion")
}
```

### Using Maven

Add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>io.lionweb.lionweb-java</groupId>
    <artifactId>lionweb-java-2024.1-core</artifactId>
    <version>${lionwebVersion}</version>
</dependency>
```

## Usage Schema

The typical usage consists in:
* Authoring a new Language or retrieving it
* Build models (i.e., trees of LionWeb Nodes) according to the Language(s) defined or used
* Serialize/De-serialize models for storage and for interoperability with other tools
* Relay on a LionWeb-compliant repository for storing models and for real-time collaboration with other clients
