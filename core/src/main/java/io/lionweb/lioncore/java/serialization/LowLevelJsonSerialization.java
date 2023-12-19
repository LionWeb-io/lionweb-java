package io.lionweb.lioncore.java.serialization;

import com.google.gson.*;
import io.lionweb.lioncore.java.serialization.data.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;

/**
 * This class is responsible for handling serialization and deserialization from JSON and the
 * low-level representation of models composed by SerializationBlock and the related classes.
 */
public class LowLevelJsonSerialization {

  /**
   * This will return a lower-level representation of the information stored in JSON. It is intended
   * to load broken models.
   *
   * <p>Possible usages: repair a broken model, extract a language from the model ("model
   * archeology"), etc.
   *
   * <p>This method follows a "best-effort" approach, try to limit exception thrown and return data
   * whenever is possible, in the measure that it is possible.
   */
  public SerializedChunk deserializeSerializationBlock(JsonElement jsonElement) {
    SerializedChunk serializedChunk = new SerializedChunk();
    if (jsonElement.isJsonObject()) {
      JsonObject topLevel = jsonElement.getAsJsonObject();
      checkNoExtraKeys(topLevel, Arrays.asList("nodes", "serializationFormatVersion", "languages"));
      readSerializationFormatVersion(serializedChunk, topLevel);
      readLanguages(serializedChunk, topLevel);
      deserializeClassifierInstances(serializedChunk, topLevel);
      return serializedChunk;
    } else {
      throw new IllegalArgumentException(
          "We expected a Json Object, we got instead: " + jsonElement);
    }
  }

  /**
   * This will return a lower-level representation of the information stored in JSON. It is intended
   * to load broken models.
   *
   * <p>Possible usages: repair a broken model, extract a language from the model ("model
   * archeology"), etc.
   *
   * <p>This method follows a "best-effort" approach, try to limit exception thrown and return data
   * whenever is possible, in the measure that it is possible.
   */
  public SerializedChunk deserializeSerializationBlock(String json) {
    return deserializeSerializationBlock(JsonParser.parseString(json));
  }

  /**
   * This will return a lower-level representation of the information stored in JSON. It is intended
   * to load broken models.
   *
   * <p>Possible usages: repair a broken model, extract a language from the model ("model
   * archeology"), etc.
   *
   * <p>This method follows a "best-effort" approach, try to limit exception thrown and return data
   * whenever is possible, in the measure that it is possible.
   */
  public SerializedChunk deserializeSerializationBlock(File file) throws FileNotFoundException {
    return deserializeSerializationBlock(JsonParser.parseReader(new FileReader(file)));
  }

  public JsonElement serializeToJsonElement(SerializedChunk serializedChunk) {
    JsonObject topLevel = new JsonObject();
    topLevel.addProperty(
        "serializationFormatVersion", serializedChunk.getSerializationFormatVersion());

    JsonArray languages = new JsonArray();
    serializedChunk.getLanguages().forEach(m -> languages.add(serializeToJsonElement(m)));
    topLevel.add("languages", languages);

    JsonArray nodes = new JsonArray();
    for (SerializedClassifierInstance node : serializedChunk.getClassifierInstances()) {
      JsonObject nodeJson = new JsonObject();
      nodeJson.addProperty("id", node.getID());

      nodeJson.add("classifier", serializeToJsonElement(node.getClassifier()));

      JsonArray properties = new JsonArray();
      for (SerializedPropertyValue propertyValue : node.getProperties()) {
        JsonObject property = new JsonObject();
        property.add("property", serializeToJsonElement(propertyValue.getMetaPointer()));
        property.addProperty("value", propertyValue.getValue());
        properties.add(property);
      }
      nodeJson.add("properties", properties);

      JsonArray containments = new JsonArray();
      for (SerializedContainmentValue childrenValue : node.getContainments()) {
        JsonObject children = new JsonObject();
        children.add("containment", serializeToJsonElement(childrenValue.getMetaPointer()));
        children.add("children", SerializationUtils.toJsonArray(childrenValue.getValue()));
        containments.add(children);
      }
      nodeJson.add("containments", containments);

      JsonArray references = new JsonArray();
      for (SerializedReferenceValue referenceValue : node.getReferences()) {
        JsonObject reference = new JsonObject();
        reference.add("reference", serializeToJsonElement(referenceValue.getMetaPointer()));
        reference.add(
            "targets", SerializationUtils.toJsonArrayOfReferenceValues(referenceValue.getValue()));
        references.add(reference);
      }
      nodeJson.add("references", references);

      JsonArray annotations = new JsonArray();
      for (String annotationID : node.getAnnotations()) {
        annotations.add(annotationID);
      }
      nodeJson.add("annotations", annotations);

      nodeJson.addProperty("parent", node.getParentNodeID());

      nodes.add(nodeJson);
    }
    topLevel.add("nodes", nodes);

    return topLevel;
  }

  public String serializeToJsonString(SerializedChunk serializedChunk) {
    return new GsonBuilder()
        .serializeNulls()
        .setPrettyPrinting()
        .create()
        .toJson(serializeToJsonElement(serializedChunk));
  }

  //
  // Private methods
  //

  private void readSerializationFormatVersion(
      SerializedChunk serializedChunk, JsonObject topLevel) {
    if (!topLevel.has("serializationFormatVersion")) {
      throw new IllegalArgumentException("serializationFormatVersion not specified");
    }
    JsonElement serializationFormatVersion = topLevel.get("serializationFormatVersion");
    requireIsString(serializationFormatVersion, "serializationFormatVersion");
    String serializationFormatVersionValue = serializationFormatVersion.getAsString();
    serializedChunk.setSerializationFormatVersion(serializationFormatVersionValue);
  }

  private void readLanguages(SerializedChunk serializedChunk, JsonObject topLevel) {
    if (!topLevel.has("languages")) {
      throw new IllegalArgumentException("languages not specified");
    }
    if (topLevel.get("languages").isJsonArray()) {
      topLevel.get("languages").getAsJsonArray().asList().stream()
          .forEach(
              element -> {
                try {
                  UsedLanguage languageKeyVersion = new UsedLanguage();
                  if (element.isJsonObject()) {
                    JsonObject jsonObject = element.getAsJsonObject();
                    checkNoExtraKeys(jsonObject, Arrays.asList("key", "version"));
                    if (!jsonObject.has("key") || !jsonObject.has("version")) {
                      throw new IllegalArgumentException(
                          "Language should have keys key and version. Found: " + element);
                    }
                    requireIsString(jsonObject.get("key"), "key");
                    requireIsString(jsonObject.get("version"), "key");
                    languageKeyVersion.setKey(jsonObject.get("key").getAsString());
                    languageKeyVersion.setVersion(jsonObject.get("version").getAsString());
                  } else {
                    throw new IllegalArgumentException(
                        "Language should be an object. Found: " + element);
                  }
                  serializedChunk.addLanguage(languageKeyVersion);
                } catch (Exception e) {
                  throw new RuntimeException("Issue while deserializing " + element, e);
                }
              });
    } else {
      throw new IllegalArgumentException(
          "We expected a Json Array, we got instead: " + topLevel.get("languages"));
    }
  }

  private void deserializeClassifierInstances(
      SerializedChunk serializedChunk, JsonObject topLevel) {
    if (!topLevel.has("nodes")) {
      throw new IllegalArgumentException("nodes not specified");
    }
    if (topLevel.get("nodes").isJsonArray()) {
      topLevel.get("nodes").getAsJsonArray().asList().stream()
          .forEach(
              element -> {
                try {
                  SerializedClassifierInstance instance = deserializeClassifierInstance(element);
                  serializedChunk.addClassifierInstance(instance);
                } catch (DeserializationException e) {
                  throw new DeserializationException(
                      "Issue while deserializing classifier instances", e);
                } catch (Exception e) {
                  throw new RuntimeException("Issue while deserializing " + element, e);
                }
              });
    } else {
      throw new IllegalArgumentException(
          "We expected a Json Array, we got instead: " + topLevel.get("nodes"));
    }
  }

  private JsonElement serializeToJsonElement(MetaPointer metapointer) {
    JsonObject jsonObject = new JsonObject();
    jsonObject.addProperty("language", metapointer.getLanguage());
    jsonObject.addProperty("version", metapointer.getVersion());
    jsonObject.addProperty("key", metapointer.getKey());
    return jsonObject;
  }

  private JsonElement serializeToJsonElement(UsedLanguage languageKeyVersion) {
    JsonObject jsonObject = new JsonObject();
    jsonObject.addProperty("key", languageKeyVersion.getKey());
    jsonObject.addProperty("version", languageKeyVersion.getVersion());
    return jsonObject;
  }

  @Nullable
  private SerializedClassifierInstance deserializeClassifierInstance(JsonElement jsonElement) {
    if (!jsonElement.isJsonObject()) {
      throw new IllegalArgumentException(
          "Malformed JSON. Object expected but found " + jsonElement);
    }
    try {
      JsonObject jsonObject = jsonElement.getAsJsonObject();

      SerializedClassifierInstance serializedClassifierInstance =
          new SerializedClassifierInstance();
      serializedClassifierInstance.setClassifier(
          SerializationUtils.tryToGetMetaPointerProperty(jsonObject, "classifier"));
      serializedClassifierInstance.setParentNodeID(
          SerializationUtils.tryToGetStringProperty(jsonObject, "parent"));

      serializedClassifierInstance.setID(
          SerializationUtils.tryToGetStringProperty(jsonObject, "id"));

      JsonArray properties = jsonObject.get("properties").getAsJsonArray();
      properties.forEach(
          property -> {
            JsonObject propertyJO = property.getAsJsonObject();
            serializedClassifierInstance.addPropertyValue(
                new SerializedPropertyValue(
                    SerializationUtils.tryToGetMetaPointerProperty(propertyJO, "property"),
                    SerializationUtils.tryToGetStringProperty(propertyJO, "value")));
          });

      JsonArray containments;
      if (jsonObject.has("children")) {
        containments = jsonObject.get("children").getAsJsonArray();
      } else if (jsonObject.has("containments")) {
        containments = jsonObject.get("containments").getAsJsonArray();
      } else {
        throw new UnsupportedOperationException(
            "Node is missing containments entry: " + jsonObject);
      }

      containments.forEach(
          containmentEntry -> {
            JsonObject containmentJO = containmentEntry.getAsJsonObject();
            serializedClassifierInstance.addContainmentValue(
                new SerializedContainmentValue(
                    SerializationUtils.tryToGetMetaPointerProperty(containmentJO, "containment"),
                    SerializationUtils.tryToGetArrayOfIDs(containmentJO, "children")));
          });

      JsonArray references = jsonObject.get("references").getAsJsonArray();
      references.forEach(
          referenceEntry -> {
            JsonObject referenceJO = referenceEntry.getAsJsonObject();
            serializedClassifierInstance.addReferenceValue(
                new SerializedReferenceValue(
                    SerializationUtils.tryToGetMetaPointerProperty(referenceJO, "reference"),
                    SerializationUtils.tryToGetArrayOfReferencesProperty(referenceJO, "targets")));
          });

      JsonElement annotationsJE = jsonObject.get("annotations");
      if (annotationsJE != null) {
        JsonArray annotations = annotationsJE.getAsJsonArray();
        serializedClassifierInstance.setAnnotations(
            StreamSupport.stream(annotations.spliterator(), false)
                .map(
                    annotationEntry -> {
                      JsonPrimitive annotationJP = annotationEntry.getAsJsonPrimitive();
                      return annotationJP.getAsString();
                    })
                .collect(Collectors.toList()));
      }

      return serializedClassifierInstance;
    } catch (DeserializationException e) {
      throw new DeserializationException("Issue occurred while deserializing " + jsonElement, e);
    }
  }

  private void checkNoExtraKeys(JsonObject jsonObject, Collection<String> expectedKeys) {
    Collection<String> extraKeys = new HashSet<>(jsonObject.keySet());
    extraKeys.removeAll(expectedKeys);
    if (!extraKeys.isEmpty()) {
      throw new RuntimeException(
          "Extra keys found: " + extraKeys + ". Expected keys: " + expectedKeys);
    }
  }

  private void requireIsString(JsonElement jsonElement, String desc) {
    if (jsonElement == null
        || !jsonElement.isJsonPrimitive()
        || !jsonElement.getAsJsonPrimitive().isString()) {
      throw new RuntimeException(desc + " should be present and be a string value");
    }
  }
}
