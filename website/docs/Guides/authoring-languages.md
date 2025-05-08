---
sidebar_position: 41
---

# Authoring Languages for LionWeb

LionWeb is an open initiative to enable **interoperability among language engineering tools**. 

Therefore, typically one would:
* Use one of the tools compatible with LionWeb to author a language
* Export the language into LionWeb format and import it in other tools

Alternatively, a language can also be defined programmatically using the API provided by LionWeb Java.

## Using LionWeb-compatible tools to author languages

In most real-world use cases, **language definitions (or metamodels)** are created using **dedicated language workbenches or modeling tools**. These tools provide expressive, user-friendly environments to author, maintain, and evolve languages.

You may want to consider

- [**JetBrains MPS**](https://www.jetbrains.com/mps/): A powerful projectional editor with LionWeb export support provided through [LionWeb MPS](http://github.com/lionweb-io/lionweb-mps).
- [**Freon**](https://www.freon4dsl.dev/): A lightweight web-based projectional editor, with support for LionWeb provided through [LionWeb-Freon-M3](https://github.com/LionWeb-io/lionweb-freon-m3).
- [**StarLasu**](https://starlasu.strumenta.com/): A cross-platform framework for language engineering framework developed by [Strumenta](https://strumenta.com).

These tools allow engineers to create languages using their built-in mechanisms and then **export them to LionWeb-compatible formats**. Once exported, these languages can be:

- Used in other LionWeb-aware tools.
- Serialized to formats like **JSON**, **FlatBuffer**, or **BroadBuffer**.
- Re-imported across the ecosystem.

This workflow maximizes **interoperability and reuse**, allowing language definitions to move seamlessly across platforms.

---

## Authoring Languages Programmatically

While most users rely on external tools, **it is also possible to author languages programmatically** using LionWeb-Java.

Using the API in the `core` module, you can define metamodels directly in Java code. This gives you the flexibility to:

- Build metamodels dynamically.
- Serialize and persist them.
- Use them in JVM-Based libraries and programs.
- Export them to LionWeb formats for use elsewhere.

### Supported Serialization Formats

The LionWeb Java implementation supports serialization in:

- **JSON** (standard and human-readable)
- **ProtoBuf** and **FlatBuffers** (compact binary format)

---

## Example: Defining a Language Programmatically

The following example shows how to define a minimal language with a single concept `Task` that has a `name` property.

```java
package com.example;

import io.lionweb.lioncore.java.language.Concept;
import io.lionweb.lioncore.java.language.Language;
import io.lionweb.lioncore.java.language.LionCoreBuiltins;
import io.lionweb.lioncore.java.language.Property;
import io.lionweb.lioncore.java.serialization.JsonSerialization;
import io.lionweb.lioncore.java.serialization.SerializationProvider;
import io.lionweb.lioncore.java.utils.LanguageValidator;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class LanguageAuthoringExample {
    public static void main(String[] args) throws Exception {
        // Define the 'Task' concept
        Concept taskConcept = new Concept("Task");
        taskConcept.setID("Task-id");
        taskConcept.setName("Task");
        taskConcept.setKey("Task");
        taskConcept.setAbstract(false);

        // Add a 'name' property
        Property nameProperty = new Property();
        nameProperty.setID("task-name-id");
        nameProperty.setName("name");
        nameProperty.setKey("task-name");
        nameProperty.setType(LionCoreBuiltins.getString());
        taskConcept.addFeature(nameProperty);

        // Define the language container
        Language taskLanguage = new Language();
        taskLanguage.setID("task-id");
        taskLanguage.setKey("task");
        taskLanguage.setName("Task Language");
        taskLanguage.setVersion("1.0");
        taskLanguage.addElement(taskConcept);

        LanguageValidator.ensureIsValid(taskLanguage);

        // Serialize to JSON
        JsonSerialization serialization = SerializationProvider.getStandardJsonSerialization();
        String json = serialization.serializeTreesToJsonString(taskLanguage);
        Files.write(Paths.get("task-language.json"), json.getBytes(StandardCharsets.UTF_8));
    }
}
```
