# core

Java implementation of LionCore (M2) and LionWeb (M1), including (de-)serializers.

## Dependency

```kotlin
dependencies {
    implementation("io.lionweb:lionweb-2024.1-core:$lionwebVersion")
}
```

## Serialization

The standard way to serialize LionWeb _Chunks_ is by using JSON.

In addition to this format, there is the need to use more efficient formats to complete
operations on very large amounts of nodes in a sustainable amount of time. For this reason
[Protocol Buffers](https://protobuf.dev/) is also supported. The format has been created by
Google, and is supported in many programming languages.

Both formats are obtained from `SerializationProvider`:

```java
JsonSerialization json = SerializationProvider.getStandardJsonSerialization(LionWebVersion.v2024_1);
String serialized = json.serializeTreesToJsonString(root);

ProtoBufSerialization protoBuf = SerializationProvider.getStandardProtoBufSerialization(LionWebVersion.v2024_1);
byte[] bytes = protoBuf.serializeTreesToByteArray(root);
```

The Protobuf encoding is written and read directly by `DirectProtoBufSerializer` and
`DirectProtoBufDeserializer`, with the field numbers defined in `ProtoBufFieldNumbers`, so no code
generation is needed.

## Testing

This module has unit tests (`src/test`), integration tests against the shared
[LionWeb integration test suite](https://github.com/LionWeb-io/lionweb-integration-testing) and
the official JSON Schemas (`src/integrationTest`), and performance tests and JMH benchmarks for
serialization (`src/performanceTest`):

```bash
./gradlew :core:test
./gradlew :core:integrationTest
./gradlew :core:performanceTest
```

See [TESTING.md](../TESTING.md) for details.
