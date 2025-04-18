---
sidebar_position: 2
---

# Getting Started with LionWeb Java

This guide will help you get started with LionWeb Java, from installation to creating your first project.

## Prerequisites

- Java 8 or later (Java 11 required for building)

## Installation

Note that even if you want to use the 2023.1 specs you can use a recent version of LionWeb Java. All versions supports also previous versions of the specs.

### Using Gradle

Add the following to your `build.gradle`:

```groovy
dependencies {
    implementation("io.lionweb.lionweb-java:lionweb-java-2024.1-core:$lionwebVersion")
}
```


### Using Maven

Add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>io.lionweb.lionweb-java</groupId>
    <artifactId>lionweb-java-2024.1-core</artifactId>
    <version>${lionwebVersion}</version>
</dependency>
```

## Basic Usage

### Creating a Model

```java
// Create a new node using DynamicNode for flexibility
DynamicNode node1 = new DynamicNode("my-model-id", LionCore.getModel());
node1.setPropertyValue(LionCore.getModel().getPropertyByName("version"), "1.0.0");

// Create a second node
DynamicNode node2 = new DynamicNode("my-node-id", MyLanguage.MY_CONCEPT);
node2.setPropertyValue(MyLanguage.MY_CONCEPT.getPropertyByName("name"), "My Node");

// Add the node to the model
node1.addChild(LionCore.getModel().getContainmentByName("elements"), node2);
```

### Serializing to JSON

```java
// Create a serializer with the appropriate version
JsonSerialization jsonSerialization = SerializationProvider.getStandardJsonSerialization(LionWebVersion.v2024_1);

// Register any custom languages you're using
jsonSerialization.getClassifierResolver().registerLanguage(MyLanguage.INSTANCE);

// Serialize the model
String json = jsonSerialization.serializeToJsonString(model);
```

### Deserializing from JSON

```java
// Create a deserializer with the appropriate version
JsonSerialization jsonSerialization = SerializationProvider.getStandardJsonSerialization(LionWebVersion.v2024_1);

// Register any custom languages you're using
jsonSerialization.getClassifierResolver().registerLanguage(MyLanguage.INSTANCE);

// Deserialize the model
List<Node> nodes = jsonSerialization.deserializeToNodes(json);
Node root = nodes.get(0);
```

## Working with EMF

### Converting to EMF

```java
// Create a converter with the appropriate version
LionWebToEMFConverter converter = new LionWebToEMFConverter(LionWebVersion.v2024_1);

// Register any custom languages you're using
converter.registerLanguage(MyLanguage.INSTANCE);

// Convert the model
Resource resource = converter.convert(model);
```

### Converting from EMF

```java
// Create a converter with the appropriate version
EMFToLionWebConverter converter = new EMFToLionWebConverter(LionWebVersion.v2024_1);

// Register any custom languages you're using
converter.registerLanguage(MyLanguage.INSTANCE);

// Convert the resource
Node root = converter.convert(resource);
```

## Repository Client

### Connecting to a Repository

```java
// Create a client with the appropriate version
LionWebRepoClient client = new LionWebRepoClient(
    LionWebVersion.v2024_1, 
    "localhost", 
    8080,  // port number
    "my-repository"
);

// Create a new repository if needed
client.createRepository(new RepositoryConfiguration(
    "my-repository",
    LionWebVersion.v2024_1,
    HistorySupport.DISABLED
));

// Register any custom languages you're using
client.getJsonSerialization().registerLanguage(MyLanguage.INSTANCE);
```

### Working with Partitions

```java
// Create a partition
DynamicNode partition = new DynamicNode("partition-1", MyLanguage.PARTITION);
client.createPartitions(client.getJsonSerialization().serializeNodesToJsonString(partition));

// List all partitions
List<Node> partitions = client.listPartitions();
List<String> partitionIds = client.listPartitionsIDs();

// Delete partitions
client.deletePartitions(Arrays.asList("partition-1"));
```

### Storing and Retrieving Nodes

```java
// Create nodes
DynamicNode node1 = new DynamicNode("node-1", MyLanguage.MY_CONCEPT);
node1.setPropertyValue(MyLanguage.MY_CONCEPT.getPropertyByName("name"), "First Node");

DynamicNode node2 = new DynamicNode("node-2", MyLanguage.MY_CONCEPT);
node2.setPropertyValue(MyLanguage.MY_CONCEPT.getPropertyByName("name"), "Second Node");

// Add nodes to partition
ClassifierInstanceUtils.addChild(partition, "elements", node1);
ClassifierInstanceUtils.addChild(partition, "elements", node2);

// Store nodes
client.store(Arrays.asList(partition));

// Retrieve nodes
List<Node> retrievedNodes = client.retrieve(Arrays.asList("partition-1"), 10);
```

### Querying Nodes

```java
// Get nodes by classifier
Map<ClassifierKey, ClassifierResult> nodesByClassifier = client.nodesByClassifier();

// Get nodes by language
Map<String, ClassifierResult> nodesByLanguage = client.nodesByLanguage();

// Generate new IDs
List<String> newIds = client.ids(5); // Generate 5 new IDs
```

