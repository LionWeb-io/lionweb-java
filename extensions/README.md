# extensions

This module contains a set of **serialization-related extensions** that are **not strictly required by the LionWeb specification**. They are intended as practical, interoperable additions that build on top of the standard LionWeb concepts (nodes, languages, partitions, chunks), but they are not part of the core spec itself.

### Protobuf-based serialization

The standard Protobuf serialization of LionWeb chunks is provided by `core` (`ProtoBufSerialization`). This module builds on it with `ExtraProtoBufSerialization`, which also serializes messages that are not part of the standard, such as bulk imports:

- LionWeb models are grouped into **serialization chunks**, which typically correspond to partitions or language definitions.
- These chunks are encoded as Protobuf messages and can be converted to and from `byte[]` for storage or transport.
- Some Protobuf messages go beyond the minimal LionWeb requirements (for example, supporting bulk import or additional index structures). These are convenience extensions and do **not** change the LionWeb data model itself.

Using Protobuf is entirely optional from the perspective of the LionWeb specification, but it is useful when you need compact, fast, and stream-friendly serialization.

### LionWeb Archive (`.lwa`)

The **LionWeb Archive** is another extension provided in this module. It defines a conventional way to package multiple LionWeb artifacts into a single file. A LionWeb Archive:

- Is technically a **ZIP file**, typically using the `.lwa` extension.
- Has a **fixed internal directory layout**:

  - `partitions/` — serialized LionWeb partitions (model content).
  - `languages/` — serialized language definitions required to interpret the partitions.
  - `metadata/` — metadata files describing the archive.

- Always includes a `metadata/metadata.properties` file that contains, among others, a `LionWeb-Version` property specifying which LionWeb format version the archive uses.
- Stores all payload files (languages and partitions) as **Protobuf-serialized** LionWeb chunks.

The LionWeb Archive format is **conventional and optional**: it is not mandated by the LionWeb specification, but it offers a practical, versioned, and self-contained way to:

- Export and import the contents of a LionWeb repository.
- Exchange larger sets of LionWeb data between tools.
- Store backups or snapshots of models and languages together with the exact LionWeb version they target.

### Extended client

`ExtendedLionWebClient` adds to the standard bulk API client the extension APIs offered by the
LionWeb Server (`AdditionalAPIClient`):

- **bulk import** of nodes, using JSON or Protobuf as transfer format, with or without compression;
- **node tree** inspection, returning the IDs, parents and depths of the nodes in a subtree.

## Dependency

```kotlin
dependencies {
    implementation("io.lionweb:lionweb-2024.1-extensions:$lionwebVersion")
}
```

## Testing

Unit tests run with `./gradlew :extensions:test`. Functional tests exercise the extended client
against a LionWeb Server started with Testcontainers, so they require Docker:

```
./gradlew :extensions:functionalTest
```

See [TESTING.md](../TESTING.md) for details.
