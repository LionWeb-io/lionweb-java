# Changelog

### Version 1.4.0 (in progress)

* Bump dependencies: protobuf 4.35.0, Guava 33.6.0-jre, Jetbrains annotations 26.1.0, testcontainers 1.21.4, emfjson 2.3.0, javapoet 0.15.0, protobuf Gradle plugin 0.10.0, vanniktech publish plugin 0.36.0
* Explicitly declare `jsr305` as `compileOnly`/`testCompileOnly` across all modules (fixes compilation after Guava removed its transitive JSR-305 dependency)
* Remove all generated protobuf message classes (`PBChunk`, `PBNode`, `PBBulkImport`, etc.) — `ProtoBufSerialization` now delegates entirely to `DirectProtoBufSerializer` / `DirectProtoBufDeserializer`; proto files and the protobuf codegen Gradle plugin are removed from `core` and `extensions`
* Add `DirectBulkImportSerializer` for bulk-import protobuf serialization without generated classes; `ExtraProtoBufSerialization.serializeBulkImportToBytes` now uses it
* Improve `DirectProtoBufDeserializer` to throw `DeserializationException` when a metapointer references an out-of-bounds language index

### Version 1.3.17

* Add end-to-end integration test for Gradle plugin code generation pipeline (db2sql language)
* Fix language topological sort ordering for cross-platform file system compatibility
* Consolidate GitHub CI workflows into a unified `ci.yml` with Java 17, 21, and 25 matrix
* Upgrade Gradle wrapper to 9.5.1
* Upgrade Spotless to 8.5.1 for Java 25 compatibility

### Version 1.3.16

* Fix issue with conflicting variables in classes generation

### Version 1.3.15

* Enable dynamic nodes in JSON serialization to support annotations
* Enhance `GenerationContext` with concept mappings and support for boolean fields
* Improve exception messages in code generation for unsupported concepts and interfaces
* Include instance ID in `Instantiator` exceptions for better diagnostics
* Introduce `InconsistentDataHandler` interface for customizable handling of inconsistent data during serialization/deserialization
* Extend `InconsistentDataHandler` to handle missing classifiers, in addition to missing properties
* Improve null and duplicate ID checks during deserialization

### Version 1.3.14

* Enable dynamic nodes in JSON serialization to support annotations
* Add `DirectProtoBufSerializer` and `DirectProtoBufDeserializer` for efficient binary serialization/deserialization
* Optimize `DirectProtoBufSerializer` with single-pass indexing and cached indexing for improved performance
* Introduce flat serialization plan in ProtoBuf serialization for improved cache locality and memory efficiency
* Extend subchunk logic to include annotations
* Extract ProtoBuf field numbers into a dedicated constants class for improved readability

### Version 1.3.13

* Refactor `Classifier`: replace stream-based feature lookups with enhanced for-loops for improved performance

### Version 1.3.12

* Optimize equality checks for `ClassifierInstances` and `ReferenceValues`
* Optimize `addAnnotation` method for improved readability and performance
* Bump org.jetbrains.dokka to 2.2.0

### Version 1.3.11

* Fix foojay toolchain resolver plugin compatibility with Gradle 9 (update to 1.0.0)
* Pre-size `SerializedClassifierInstance` feature lists from protobuf node counts to reduce allocations
* Make classifier index materialization optional in `RepositoryData` and `InMemoryServer` (default: enabled)
* Add `nodesByClassifier(limit, classifierKey)` for targeted single-classifier lookup
* Optimize `nodesByClassifier` limit slicing via `subList` instead of element-by-element iteration

### Version 1.3.10

* Upgrade Gradle wrapper to 9.4.1
* Fix encoding issues in file I/O by using explicit UTF-8 encoding instead of platform default charset
* Add support for custom charsets in JSON serialization/deserialization methods
* Fix Gradle plugin task validation errors for Gradle 9 compatibility (`@CacheableTask` on generation tasks)
* Fix code generation task to run on Java 11+ (MWE2 launcher requirement)
* Upgrade Kotlin to 2.3.20
* Upgrade org.eclipse.emf.ecore.xmi to 2.40.0
* Upgrade protobuf-java to 4.34.1
* Upgrade com.github.gmazzo.buildconfig to 6.0.9

### Version 1.3.9

* Fix null check for `registerPartitionObserver` parameter
* Fix integration test paths after lionweb-integration-testing restructure
* Performance optimization for `LanguageVersion.of` with fast path cache lookup
* Simplify `Containment` and `Property` construction by including ID in constructor
* Remove SpotBugs integration

### Version 1.3.8

* Further serialization and deserialization performance improvements
* Add classifier indexing to `RepositoryData` for efficient node retrieval
* Optimize `hashCode` caching in `ClassifierKey`
* Add O(1) `placeAt` method for efficient node sorting during deserialization

### Version 1.3.7

* Introducing benchmarks
* Tuning serialization and deserialization performance

### Version 1.3.6

* Improve performance by computing expensive `requireNonNull` exception message lazily

### Version 1.3.5

* Improvements to InMemoryServer concurrency

### Version 1.3.4

* Performance improvements to `InMemoryServer`
* Performance improvements to language model
* Performance improvements to `DynamicClassidierInstance`

### Version 1.3.3

* Performance improvements because of features caching

### Version 1.3.2
* Serializes empty features by default ([#306](https://github.com/LionWeb-io/lionweb-jvm/issues/306))
* Omit unset properties same as other unset features

### Version 1.3.1

* Fix issues in generation of classes related to conflicts with Java keywords
* Make gradle plugin configure dependencies and source sets automatically

### Version 1.3.0

* Combine LW Java and LW Kotlin to create LW JVM
* Move to Junit 5, consistently

### Version 1.2.7

* Complete classes generation from the Gradle Plugin
* Initial support for delta protocol

### Version 1.2.6

* Improve classes generation from the Gradle Plugin

### Version 1.2.5

* In generated language class, add getters for primitive types and enumerations
* Rename PrimitiveValuesSerialization to DataTypeValuesSerialization and the internal interfaces as-well: 
  PrimitiveSerializer, PrimitiveDeserializer, and PrimitiveValueSerializerAndDeserializer

### Version 1.2.4

* Refactor standardInitialization to support custom LionWeb versions

### Version 1.2.3

* Fixing and completing LanguageJavaCodeGenerator

### Version 1.2.2

* Do not register types introduced in LW 2024.1 when using LW 2023
* When comparing Chunks, check also used languages

### Version 1.2.1

* Improve generation of Language Classes from the Gradle Plugin
* Adding generation of AST Classes from the Gradle Plugin

### Version 1.2.0

* Preliminary work to support delta protocol
* Added Gradle Plugin
* Added support for LionWeb Archive

### Version 1.1.4

* Improved protobuf serialization and removed FlatBuffers support
* Introduced concurrency tests and thread-safe caching
* Extended Classifier and Annotation APIs with chaining and multiplicity validation
* Enhanced validation in ChunkValidator and RepositoryData
* Improved test coverage and documentation

### Version 1.1.3

* Added consistency checks in RepositoryData and InMemoryServer
* Enhanced error reporting and node movement handling
* Improved validation of annotations and parent relationships

### Version 1.1.2

* Refactored ChunkValidator for duplicate metapointer detection and streamlined checks
* Introduced setPropertyValue and clarified unsafe serialization operations
* Added tests for ChunkValidator and serialization logic

### Version 1.1.1

* Updated LionWeb Server dependency
* Improved validation and consistency logic across repository components
* Refined domain model handling for libraries and missing nodes

### Version 1.1.0

* Renamed SerializedChunk into SerializationChunk
* Added support for improved protobuf serialization
* Removed support for FlatBuffers serialization

### Version 1.0.7

* Add support for partition observers

### Version 1.0.6

* Considering annotation in InMemoryStorage

### Version 1.0.5

* Ensure that RepositoryData do not propose IDs of existing nodes

### Version 1.0.4

* Fixed bug in RepositoryData.ids

### Version 1.0.3

* Fixed bug in SerializedChunk.equals

### Version 1.0.2

* Internaling MetaPointers and most of SerializedPropertyValues

### Version 1.0.1

* DynamicNode: uniformly use getID() to access node IDs

### Version 1.0.0

* Reorganization of all packages
* Added In-Memory Server
* LanguageValidator now verifies consistency of LionWeb Version usages
* Made creation of languages more convenient
* Added AbstractSerialization.setAllUnavailabilityPolicies
* Added Language.getAnnotationByName
* Revised how we calculate languages to list as used languages
* Reduction of memory footprint for storing nodes

### Version 0.4.7

* Supporting limit parameter in inspection APIs
* Add possibility to remove feature from concept
* Improve support for multiple LionWeb versions in ProtobufSerialization and FlatBuffersSerialization

### Version 0.4.6

* Adding support for serializing and deserializing entire repositories

### Version 0.4.5

* Implement support for history APIs
* Adding documentation

### Version 0.4.4

* Bug fix for ClientForBulkAPIs
* Added retrieve(Node) method in LionWebRepoClient
* Introducing documentation

### Version 0.4.3

* Bug fix for ClientForBulkAPIs.retrieve

### Version 0.4.2

* Performance improvements for serialization

### Version 0.4.1

* Add support for more APIs in the repo client: DB Admin APIs and Inspection APIs

### Version 0.4.0

* Introduction of the `repo-client` and `repo-client-testing` modules

### Version 0.3.5

* Minor bug fixing

### Version 0.3.4

* Permit to load languages depending on other languages

### Version 0.3.3

* EMF conversion: various improvements
* Improvements to DynamicNode.equals

### Version 0.3.2

* Allow annotations to have children
* Remove Partition and Experimental
* EMF conversion: handle abstract flag
* EMF conversion: handle attributes with high multiplicity

## lionweb-java-2024.1-* - Version 0.3.0

Introducing support for LionWeb 2024.1

### Version 0.2.18

* Introduce support for serialization based on ProtoBuffer and FlatBuffers
* Introduce SerializationProvider

### Version 0.2.16

* Support presence of multiple references to the same proxied node during serialization

### Version 0.2.15

* Revise support for homogeneous APIs in Node, especially for references
* Introduced ClassifierInstanceUtils

### Version 0.2.14

* Improve support for annotations, in particular for M3 Nodes

### Version 0.2.13

* Minor improvements to DynamicNode
* Fixes in serialization around null parents

### Version 0.2.12

* Introducing HasSettableParent

### Version 0.2.11

* Correct serialization of enums

### Version 0.2.10

* Introducing Proxy Nodes

### Version 0.2.5

* Model is renamed in Partition
* Node.getModel is renamed in Node.getPartition

### Version 0.2.2

* Convergence of Serialized{Annotation&Node}Instance into SerializedClassifierInstance

### Version 0.2.1

* The JSON serialization is changed so that the meta-pointer under `"classifier"` appears directly after the `"id"` key-value pair.
* The internal representation of serialized nodes was simplified.


## lionweb-java-2023.1-* - Version 0.2.0

This is the first release based on the new artifact ids (which now include the specifications version), which indicate support for the specification of LionWeb released under version 2023.1.

At this stage support for the M1 and M2 APIs is relatively complete. Support for annotations may need refinements. Some constraints may not yet been verified.
Import and export from and to EMF is a work in progress.
