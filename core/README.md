## Serialization

The standard way to serialize LionWeb _Chunks_ is by using JSON.

In addition to this format, there is the need to use more efficient formats to complete operations on very large amounts of nodes in a sustainable amount of time. For this reason also [Protocol Buffers](https://protobuf.dev/) is supported.

The format has been created by Google, and supported in many programming languages.

Protocol Buffers has wider support. For example, there is a gradle plugin that we use to generate the code from the format definition.
