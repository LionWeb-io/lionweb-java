# kotlin-core

Kotlin-idiomatic bindings on top of `core`.

## Features

- **`BaseNode`** — abstract class (built on `DynamicNode`) for defining typed node classes with
  Kotlin properties, delegated to `property`, `singleContainment`, `multipleContainment`,
  `singleReference` and `multipleReference`
- **`BaseAnnotation`** — equivalent base class for annotation instances
- **`lwLanguage`** — derives a LionWeb `Language` from a set of Kotlin classes, registering the
  mapping between classes and classifiers in `DefaultMetamodelRegistry`
- **Metamodel extension functions** — `createConcept`, `createProperty`, `createContainment`,
  `createReference`, `addLiteral` and others, to define languages programmatically
- **Containment lists** — the lists returned by `multipleContainment` keep parent/child links in
  sync
- **Serialization helpers** — `Node.toChunk()`, `String.toChunk()`, and extension functions on
  `SerializationChunk` and `SerializedClassifierInstance` for working with raw chunks

## Usage

```kotlin
class Directory : BaseNode() {
    var name: String? by property("name")
    val files = multipleContainment<File>("files")
}

class File : BaseNode() {
    var name: String? by property("name")
}

// Derive the language from the classes
val language = lwLanguage("fileSystem", Directory::class, File::class)

val dir = Directory()
dir.setID("dir-1")
dir.name = "docs"
val file = File()
file.setID("file-1")
file.name = "README.md"
dir.files.add(file) // file.parent is now dir

// Serialize to a chunk
val chunk = dir.toChunk()
```

## Dependency

```kotlin
dependencies {
    implementation("io.lionweb:lionweb-2024.1-kotlin-core:$lionwebVersion")
}
```

## Testing

Unit tests are in `src/test/kotlin` and run with `./gradlew :kotlin-core:test`. See
[TESTING.md](../TESTING.md) for an overview of the tests in the project.
