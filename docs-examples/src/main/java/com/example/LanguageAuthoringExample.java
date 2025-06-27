package com.example;

import io.lionweb.language.Concept;
import io.lionweb.language.Language;
import io.lionweb.language.LionCoreBuiltins;
import io.lionweb.language.Property;
import io.lionweb.serialization.JsonSerialization;
import io.lionweb.serialization.SerializationProvider;
import io.lionweb.utils.LanguageValidator;
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
