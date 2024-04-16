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
