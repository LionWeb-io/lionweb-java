# LionWeb Java

![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.lionweb.lionweb-java/lionweb-java-core/badge.png)

This projects requires Java 8, to maximize compatibility.

We require Java 11 exclusively for the Javadoc tasks, as it needs to add references to
libraries not available for Java 8. This does not affect runtime usages of the library.

## Using the library

```
dependencies {
   implementation("io.lionweb.lionweb-java:lionweb-java-core:$lionwebVersion")
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

## lionweb-java-2023.1-* - Version 0.2.0

This is the first release based on the new artifact ids (which now include the specifications version), which indicate support for the specification of LionWeb released under version 2023.1.

At this stage support for the M1 and M2 APIs is relatively complete. Support for annotations may need refinements. Some constraints may not yet been verified.
Import and export from and to EMF is a work in progress.

### Version 0.2.1

* The JSON serialization is changed so that the meta-pointer under `"classifier"` appears directly after the `"id"` key-value pair.
* The internal representation of serialized nodes was simplified.

