package io.lionweb.lioncore.java.serialization;

import com.google.gson.*;
import io.lionweb.lioncore.java.serialization.data.*;
import javax.annotation.Nullable;

/**
 * This class is responsible for handling serialization and unserialization from JSON and the
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
  public SerializedChunk unserializeSerializationBlock(JsonElement jsonElement) {
    SerializedChunk serializedChunk = new SerializedChunk();
    if (jsonElement.isJsonObject()) {
      JsonObject topLevel = jsonElement.getAsJsonObject();
      readSerializationFormatVersion(serializedChunk, topLevel);
      readLanguages(serializedChunk, topLevel);
      unserializeNodes(serializedChunk, topLevel);
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
  public SerializedChunk unserializeSerializationBlock(String json) {
    return unserializeSerializationBlock(JsonParser.parseString(json));
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

      JsonArray properties = new JsonArray();
      for (SerializedPropertyValue propertyValue : node.getProperties()) {
        JsonObject property = new JsonObject();
        property.add("property", serializeToJsonElement(propertyValue.getMetaPointer()));
        property.addProperty("value", propertyValue.getValue());
        properties.add(property);
      }
      nodeJson.add("properties", properties);

      JsonArray children = new JsonArray();
      for (SerializedContainmentValue childrenValue : node.getContainments()) {
        JsonObject childrenJ = new JsonObject();
        childrenJ.add("containment", serializeToJsonElement(childrenValue.getMetaPointer()));
        childrenJ.add("children", SerializationUtils.toJsonArray(childrenValue.getValue()));
        children.add(childrenJ);
      }
      nodeJson.add("children", children);

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

      if (node instanceof SerializedNodeInstance) {
        SerializedNodeInstance serializedNodeInstance = (SerializedNodeInstance)node;
        nodeJson.addProperty("parent", serializedNodeInstance.getParentNodeID());
        nodeJson.add("concept", serializeToJsonElement(node.getClassifier()));
      } else if (node instanceof SerializedAnnotationInstance) {
        SerializedAnnotationInstance serializedAnnotationInstance = (SerializedAnnotationInstance)node;
        nodeJson.addProperty("annotated", serializedAnnotationInstance.getAnnotated());
        nodeJson.add("annotation", serializeToJsonElement(node.getClassifier()));
      } else {
        throw new UnsupportedOperationException();
      }

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
    String serializationFormatVersion = topLevel.get("serializationFormatVersion").getAsString();
    serializedChunk.setSerializationFormatVersion(serializationFormatVersion);
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
                    if (!jsonObject.has("key") || !jsonObject.has("version")) {
                      throw new IllegalArgumentException(
                          "Language should have keys key and version. Found: " + element);
                    }
                    languageKeyVersion.setKey(jsonObject.get("key").getAsString());
                    languageKeyVersion.setVersion(jsonObject.get("version").getAsString());
                  } else {
                    throw new IllegalArgumentException(
                        "Language should be an object. Found: " + element);
                  }
                  serializedChunk.addLanguage(languageKeyVersion);
                } catch (Exception e) {
                  throw new RuntimeException("Issue while unserializing " + element, e);
                }
              });
    } else {
      throw new IllegalArgumentException(
          "We expected a Json Array, we got instead: " + topLevel.get("languages"));
    }
  }

  private void unserializeNodes(SerializedChunk serializedChunk, JsonObject topLevel) {
    if (!topLevel.has("nodes")) {
      throw new IllegalArgumentException("nodes not specified");
    }
    if (topLevel.get("nodes").isJsonArray()) {
      topLevel.get("nodes").getAsJsonArray().asList().stream()
          .forEach(
              element -> {
                try {
                  SerializedClassifierInstance node = unserializeNode(element);
                  serializedChunk.addClassifierInstance(node);
                } catch (UnserializationException e) {
                  throw new UnserializationException("Issue while unserializing nodes", e);
                } catch (Exception e) {
                  throw new RuntimeException("Issue while unserializing " + element, e);
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
    jsonObject.addProperty("version", languageKeyVersion.getVersion());
    jsonObject.addProperty("key", languageKeyVersion.getKey());
    return jsonObject;
  }

  @Nullable
  private SerializedClassifierInstance unserializeNode(JsonElement jsonElement) {
    if (!jsonElement.isJsonObject()) {
      throw new IllegalArgumentException(
          "Malformed JSON. Object expected but found " + jsonElement);
    }
    try {
      JsonObject jsonObject = jsonElement.getAsJsonObject();

      SerializedClassifierInstance serializedClassifierInstance;
      if (jsonObject.has("parent") || jsonObject.has("concept")) {
        SerializedNodeInstance serializedNodeInstance = new SerializedNodeInstance();
        serializedNodeInstance.setClassifier(
                SerializationUtils.tryToGetMetaPointerProperty(jsonObject, "concept"));
        serializedNodeInstance.setParentNodeID(
                SerializationUtils.tryToGetStringProperty(jsonObject, "parent"));
        serializedClassifierInstance = serializedNodeInstance;
      } else if (jsonObject.has("annotated") || jsonObject.has("annotation")) {
        SerializedAnnotationInstance serializedAnnotationInstance = new SerializedAnnotationInstance();
        serializedAnnotationInstance.setClassifier(
                SerializationUtils.tryToGetMetaPointerProperty(jsonObject, "annotation"));
        serializedAnnotationInstance.setAnnotated(
                SerializationUtils.tryToGetStringProperty(jsonObject, "annotated"));
        serializedClassifierInstance = serializedAnnotationInstance;
      } else {
        throw new UnsupportedOperationException("Classifier instance which does not look like a node or an annotation instance: " + jsonObject);
      }

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

      JsonArray children = jsonObject.get("children").getAsJsonArray();
      children.forEach(
          childrenEntry -> {
            JsonObject childrenJO = childrenEntry.getAsJsonObject();
            serializedClassifierInstance.addContainmentValue(
                new SerializedContainmentValue(
                    SerializationUtils.tryToGetMetaPointerProperty(childrenJO, "containment"),
                    SerializationUtils.tryToGetArrayOfIDs(childrenJO, "children")));
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

      return serializedClassifierInstance;
    } catch (UnserializationException e) {
      throw new UnserializationException("Issue occurred while unserializing " + jsonElement, e);
    }
  }
}
