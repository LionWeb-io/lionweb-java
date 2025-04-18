---
sidebar_position: 2
---

# Getting Started with LionWeb Java

This guide will help you get started with LionWeb Java, from installation to creating your first project.

## Prerequisites

- Java 8 or later (Java 11 required for building)
- Gradle 8.5 or later
- Git

## Installation

### Using Maven

Add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>io.lionweb.lionweb-java</groupId>
    <artifactId>lionweb-java-2024.1-core</artifactId>
    <version>${lionwebVersion}</version>
</dependency>
```

### Using Gradle

Add the following to your `build.gradle`:

```groovy
dependencies {
    implementation("io.lionweb.lionweb-java:lionweb-java-2024.1-core:$lionwebVersion")
}
```

## Basic Usage

### Creating a Model

```java
// Create a new model
Model model = new Model();
model.setId("my-model");
model.setVersion("1.0.0");

// Create a node
Node node = new Node();
node.setId("my-node");
node.setConcept("MyConcept");

// Add the node to the model
model.addNode(node);
```

### Serializing to JSON

```java
// Create a serializer
JsonSerialization jsonSerialization = new JsonSerialization();

// Serialize the model
String json = jsonSerialization.serializeToString(model);
```

### Deserializing from JSON

```java
// Create a deserializer
JsonSerialization jsonSerialization = new JsonSerialization();

// Deserialize the model
Model model = jsonSerialization.deserializeToModel(json);
```

## Working with EMF

### Converting to EMF

```java
// Create a converter
LionWebToEMFConverter converter = new LionWebToEMFConverter();

// Convert the model
Resource resource = converter.convert(model);
```

### Converting from EMF

```java
// Create a converter
EMFToLionWebConverter converter = new EMFToLionWebConverter();

// Convert the resource
Model model = converter.convert(resource);
```

## Repository Client

### Connecting to a Repository

```java
// Create a client
RepositoryClient client = new RepositoryClient("https://repository.lionweb.io");

// Authenticate
client.authenticate("username", "password");
```

### Working with Models

```java
// Get a model
Model model = client.getModel("model-id");

// Update a model
client.updateModel(model);

// Delete a model
client.deleteModel("model-id");
```

## Best Practices

1. **Version Management**
   - Always specify the version of LionWeb you're using
   - Keep dependencies up to date
   - Test with multiple versions when possible

2. **Error Handling**
   - Use try-catch blocks for operations that might fail
   - Log errors appropriately
   - Provide meaningful error messages

3. **Performance**
   - Use batch operations when possible
   - Cache frequently accessed data
   - Monitor memory usage 