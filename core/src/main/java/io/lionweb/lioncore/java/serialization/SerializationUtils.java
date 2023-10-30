package io.lionweb.lioncore.java.serialization;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.lionweb.lioncore.java.serialization.data.MetaPointer;
import io.lionweb.lioncore.java.serialization.data.SerializedReferenceValue;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

/** Collection of utility methods to simplify serialization and deserialization to JSON. */
class SerializationUtils {

  private SerializationUtils() {
    // Prevent instantiation
  }

  @Nullable
  static String getAsStringOrNull(JsonElement element) {
    if (element == null || element.isJsonNull()) {
      return null;
    } else {
      return element.getAsString();
    }
  }

  @Nullable
  static String tryToGetStringProperty(JsonObject jsonObject, String propertyName) {
    if (!jsonObject.has(propertyName)) {
      return null;
    }
    JsonElement value = jsonObject.get(propertyName);
    if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()) {
      return value.getAsJsonPrimitive().getAsString();
    } else {
      return null;
    }
  }

  @Nullable
  static MetaPointer tryToGetMetaPointerProperty(JsonObject jsonObject, String propertyName) {
    if (!jsonObject.has(propertyName)) {
      return null;
    }
    JsonElement value = jsonObject.get(propertyName);
    if (value.isJsonObject()) {
      JsonObject valueJO = value.getAsJsonObject();
      return new MetaPointer(
          tryToGetStringProperty(valueJO, "language"),
          tryToGetStringProperty(valueJO, "version"),
          tryToGetStringProperty(valueJO, "key"));
    } else {
      return null;
    }
  }

  @Nullable
  static List<String> tryToGetArrayOfIDs(JsonObject jsonObject, String propertyName) {
    if (!jsonObject.has(propertyName)) {
      return null;
    }
    JsonElement value = jsonObject.get(propertyName);
    if (value.isJsonArray()) {
      JsonArray valueJA = value.getAsJsonArray();
      return valueJA.asList().stream()
          .map(
              e -> {
                if (e.isJsonNull()) {
                  throw new DeserializationException(
                      "Unable to deserialize child identified by Null ID");
                }
                return e.getAsString();
              })
          .collect(Collectors.toList());
    } else {
      return null;
    }
  }

  @Nullable
  static List<SerializedReferenceValue.Entry> tryToGetArrayOfReferencesProperty(
      JsonObject jsonObject, String propertyName) {
    if (!jsonObject.has(propertyName)) {
      return null;
    }
    JsonElement value = jsonObject.get(propertyName);
    if (value.isJsonArray()) {
      JsonArray valueJA = value.getAsJsonArray();
      return valueJA.asList().stream()
          .map(
              e ->
                  new SerializedReferenceValue.Entry(
                      tryToGetStringProperty(e.getAsJsonObject(), "reference"),
                      tryToGetStringProperty(e.getAsJsonObject(), "resolveInfo")))
          .collect(Collectors.toList());
    } else {
      return null;
    }
  }

  static JsonArray toJsonArray(List<String> stringList) {
    JsonArray jsonArray = new JsonArray();
    stringList.forEach(s -> jsonArray.add(s));
    return jsonArray;
  }

  static JsonArray toJsonArrayOfReferenceValues(List<SerializedReferenceValue.Entry> entries) {
    JsonArray jsonArray = new JsonArray();
    entries.forEach(
        e -> {
          JsonObject entryJson = new JsonObject();
          entryJson.addProperty("resolveInfo", e.getResolveInfo());
          entryJson.addProperty("reference", e.getReference());
          jsonArray.add(entryJson);
        });
    return jsonArray;
  }
}
