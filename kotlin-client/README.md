# kotlin-client

Kotlin-idiomatic bindings on top of `client` for connecting to a LionWeb Server.

## Features

- **`LionWebClient`** — high-level client for a LionWeb Server repository, built on the Java
  `ExtendedLionWebClient`. It covers repositories, partitions, storing and retrieving trees,
  incremental changes (appending trees and annotations, setting properties and references),
  inspection queries (`nodesByClassifier`, `nodeTree`) and bulk import. It works with any `Node`,
  including the `BaseNode` subclasses defined with `kotlin-core`.
- **`RetrievalMode`** — controls whether `retrieve` fetches the entire subtree or only the
  requested node

## Usage

```kotlin
val client = LionWebClient(hostname = "localhost", port = 3005, repository = "default")

// Let the client deserialize nodes of our language
client.registerLanguage(myLanguage)

// Store a partition and its content
client.createPartition(myPartition)
client.storeTree(myPartition)

// Retrieve by id
val node = client.retrieve("node-id")
val onlyTheNode = client.retrieve("node-id", RetrievalMode.SINGLE_NODE)
```

## Dependency

```kotlin
dependencies {
    implementation("io.lionweb:lionweb-2024.1-kotlin-client:$lionwebVersion")
}
```

## Testing

Functional tests in this module require a running LionWeb Server, which is started with
Testcontainers, so Docker must be running. Run them with:

```
./gradlew :kotlin-client:functionalTest
```

See [TESTING.md](../TESTING.md) for details.
