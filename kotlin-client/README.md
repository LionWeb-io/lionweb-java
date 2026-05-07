# kotlin-client

Kotlin-idiomatic bindings on top of `lionweb-java-client` for connecting to a LionWeb Server.

## Features

- **`LionWebClient`** — high-level client that stores and retrieves typed nodes (works with
  `BaseNode` subclasses from `kotlin-core`)
- **`LowLevelRepoClient`** — low-level client that works with raw serialization chunks
- **`ClassifierResult`** — typed result wrapper returned by retrieval operations
- **`RetrievalMode`** — controls how deeply nodes are fetched (e.g. this node only vs. subtree)

## Usage

```kotlin
val client = LionWebClient(hostname = "localhost", port = 3005)

// Store nodes
client.store(listOf(myNode))

// Retrieve by id
val node: MyNode = client.retrieve("node-id")
```

## Dependency

```kotlin
dependencies {
    implementation("io.lionweb:lionweb-2024.1-kotlin-client:$lionwebVersion")
}
```

## Testing

Functional tests in this module require a running LionWeb Server (via Testcontainers with Docker).
Run them with:

```
./gradlew :kotlin-client:functionalTest
```
