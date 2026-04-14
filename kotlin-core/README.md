# kotlin-core

Kotlin-idiomatic bindings on top of `lionweb-java-core`.

## Features

- **`BaseNode`** — open Kotlin class that implements `ClassifierInstance`, making it easy to define
  typed node classes with Kotlin properties
- **`BaseAnnotation`** — equivalent base class for annotation instances
- **`ContainmentList`** — a `MutableList` wrapper that keeps parent/child containment in sync
- **`SerializationUtils`** — extension functions for serializing/deserializing LionWeb chunks
- **`SerializationChunkUtils` / `SerializedClassifierInstanceUtils`** — helpers for working with
  raw serialization chunks from Kotlin

## Usage

```kotlin
// Define a typed node class
class MyNode : BaseNode() {
    var name: String by property(MyLanguage.NAME)
    val children: MutableList<MyNode> by containment(MyLanguage.CHILDREN)
}

// Serialize to JSON
val json = SerializationUtils.serializeNodesToJson(listOf(myNode))
```

## Dependency

```kotlin
dependencies {
    implementation("io.lionweb:lionweb-2024.1-kotlin-core:$lionwebVersion")
}
```
