# Changelog

## lionweb-java-2023.1-* - Version 0.2.0

This is the first release based on the new artifact ids (which now include the specifications version), which indicate support for the specification of LionWeb released under version 2023.1.

At this stage support for the M1 and M2 APIs is relatively complete. Support for annotations may need refinements. Some constraints may not yet been verified.
Import and export from and to EMF is a work in progress.

### Version 0.2.1

* The JSON serialization is changed so that the meta-pointer under `"classifier"` appears directly after the `"id"` key-value pair.
* The internal representation of serialized nodes was simplified.

### Version 0.2.2

* Convergence of Serialized{Annotation&Node}Instance into SerializedClassifierInstance

### Version 0.2.5

* Model is renamed in Partition
* Node.getModel is renamed in Node.getPartition

### Version 0.2.10

* Introducing Proxy Nodes

### Version 0.2.11

* Correct serialization of enums

### Version 0.2.12

* Introducing HasSettableParent

### Version 0.2.13

* Minor improvements to DynamicNode
* Fixes in serialization around null parents

### Version 0.2.14

* Improve support for annotations, in particular for M3 Nodes

### Version 0.2.15

* Revise support for homogeneous APIs in Node, especially for references
* Introduced ClassifierInstanceUtils

### Version 0.2.16

* Support presence of multiple references to the same proxied node during serialization

### Version 0.2.18

* Introduce support for serialization based on ProtoBuffer and FlatBuffers
* Introduce SerializationProvider

## lionweb-java-2024.1-* - Version 0.3.0

Introducing support for LionWeb 2024.1

### Version 0.3.2

* Allow annotations to have children
* Remove Partition and Experimental
* EMF conversion: handle abstract flag
* EMF conversion: handle attributes with high multiplicity

### Version 0.3.3

* EMF conversion: various improvements
* Improvements to DynamicNode.equals

### Version 0.3.4

* Permit to load languages depending on other languages

### Version 0.3.5

* Minor bug fixing

### Version 0.4.0

* Introduction of the `repo-client` and `repo-client-testing` modules

### Version 0.4.1

* Add support for more APIs in the repo client: DB Admin APIs and Inspection APIs

### Version 0.4.2

* Performance improvements for serialization

### Version 0.4.3

* Bug fix for ClientForBulkAPIs.retrieve

### Version 0.4.4

* Bug fix for ClientForBulkAPIs
* Added retrieve(Node) method in LionWebRepoClient
* Introducing documentation

### Version 0.4.5

* Implement support for history APIs
* Adding documentation

### Version 0.4.6

* Adding support for serializing and deserializing entire repositories

### Version 0.4.7

* Supporting limit parameter in inspection APIs
* Add possibility to remove feature from concept
* Improve support for multiple LionWeb versions in ProtobufSerialization and FlatBuffersSerialization 

### Version 0.5.0

* Reorganization of all packages
