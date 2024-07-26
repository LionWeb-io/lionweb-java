# LionWeb Java

![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.lionweb.lionweb-java/lionweb-java-2023.1-core/badge.png)

This projects requires Java 8, to maximize compatibility.

We require Java 11 for building the project, while the artifacts published are compatible with Java 8.

## Using the library

```
dependencies {
   implementation("io.lionweb.lionweb-java:lionweb-java-2023.1-core:$lionwebVersion")
}
```

## Development

Before opening in IDEA run:

```
./gradlew setup
```

This will generate necessary classes.

## Core
Contains LionCore (M2) and LionWeb (M1) implementations in Java, including (de-)serializers from/to JSON.

## Emf
Contains im/exporters to convert models between LionCore &harr; Ecore and LionWeb &harr; EMF.

## Emf-builtins
Some of LionCore's built-in elements have no direct representation in Ecore.
This sub-project is an Eclipse project that defines an _EPackage_ `builtins` to host the equivalent elements in Ecore.
The language's _nsURI_ is `http://lionweb.io/lionweb-java/emf/core/builtins/2023.1`.

## Changelog

The Chancelog is [here](CHANGELOG.md).

## Testing

To ensure proper testing coverage we configured Jacoco.
You can run `./gradlew jacocoTestReport` and then find the report for each module, under `build/reports/jacoco`.

## Update FlatBuffers generated classes

Run from the root of the project:
```
flatc --java -o core/src/main/java core/src/main/flatbuffers/chunk.fbs
flatc --java -o extensions/src/main/java extensions/src/main/flatbuffers/bulkimport.fbs
```
