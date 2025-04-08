# LionWeb Java

![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.lionweb.lionweb-java/lionweb-java-2024.1-core/badge.png)

This projects requires Java 8, to maximize compatibility.

We require Java 11 for building the project, while the artifacts published are compatible 
with Java 8.

## Status of the project

The project is actively maintained. Feel free to open issues to ask any question, help, 
or support.

For visibility on the future plans regarding this project refer to the [Roadmap](ROADMAP.md).

Currently the project is on-par with the LionWeb specifications.

In particular, this library supports both version 2023.1 and 2024.1 of the specs.

## Using the library

```
dependencies {
   implementation("io.lionweb.lionweb-java:lionweb-java-2024.1-core:$lionwebVersion")
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

The Changelog is [here](CHANGELOG.md).

## Testing

To ensure proper testing coverage we configured Jacoco.
You can run `./gradlew jacocoTestReport` and then find the report for each module, under `build/reports/jacoco`.

## Update FlatBuffers generated classes

Run from the root of the project:
```
flatc --java -o core/src/main/java core/src/main/flatbuffers/chunk.fbs
flatc --java -o extensions/src/main/java extensions/src/main/flatbuffers/bulkimport.fbs
```

## Contributors

This project is part of the [LionWeb](https://lionweb.io) initiative.

The project is currently maintained by Federico Tomassetti with contributions from:
* Ulyana Tikhonova
* Meinte Boersma
* Niko Stotz
* Tiago Baptista

Contributions include support in the design, code contributions, code reviews, 
and bug reports.
