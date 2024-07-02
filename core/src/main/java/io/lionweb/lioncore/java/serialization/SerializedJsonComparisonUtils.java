package io.lionweb.lioncore.java.serialization;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SerializedJsonComparisonUtils {

  private SerializedJsonComparisonUtils() {}

  public static void assertEquivalentLionWebJson(JsonObject expected, JsonObject actual) {
    Set<String> keys =
        new HashSet<>(Arrays.asList("serializationFormatVersion", "nodes", "languages"));
    if (!expected.keySet().equals(keys)) {
      throw new RuntimeException("The expected object has irregular keys: " + expected.keySet());
    }
    if (!actual.keySet().equals(keys)) {
      throw new RuntimeException("The actual object has irregular keys: " + actual.keySet());
    }
    assertEquals(
        "serializationFormatVersion",
        expected.get("serializationFormatVersion"),
        actual.get("serializationFormatVersion"));
    assertEquivalentLionWebJsonNodes(
        expected.getAsJsonArray("nodes"), actual.getAsJsonArray("nodes"));
  }

  private static void assertEquivalentLionWebJsonNodes(JsonArray expected, JsonArray actual) {
    Map<String, JsonObject> expectedElements = new HashMap<>();
    Map<String, JsonObject> actualElements = new HashMap<>();
    Function<Map<String, JsonObject>, Consumer<JsonElement>> idCollector =
        collection ->
            e -> {
              String id = e.getAsJsonObject().get("id").getAsString();
              collection.put(id, e.getAsJsonObject());
            };
    expected.forEach(idCollector.apply(expectedElements));
    actual.forEach(idCollector.apply(actualElements));
    Set<String> unexpectedIDs = new HashSet<>(actualElements.keySet());
    unexpectedIDs.removeAll(expectedElements.keySet());
    Set<String> missingIDs = new HashSet<>(expectedElements.keySet());
    missingIDs.removeAll(actualElements.keySet());
    if (!unexpectedIDs.isEmpty()) {
      throw new AssertionError("Unexpected IDs found: " + unexpectedIDs);
    }
    if (!missingIDs.isEmpty()) {
      throw new AssertionError("Missing IDs found: " + missingIDs);
    }
    assertEquals("The number of nodes is different", expected.size(), actual.size());
    for (String id : expectedElements.keySet()) {
      JsonObject expectedElement = expectedElements.get(id);
      JsonObject actualElement = actualElements.get(id);
      assertEquivalentNodes(expectedElement, actualElement, "Node " + id);
    }
  }

  private static void assertEquivalentNodes(
      JsonObject expected, JsonObject actual, String context) {
    Set<String> actualMeaningfulKeys = actual.keySet();
    Set<String> expectedMeaningfulKeys = expected.keySet();
    if (actualMeaningfulKeys.contains("parent") && actual.get("parent") instanceof JsonNull) {
      actualMeaningfulKeys.remove("parent");
    }
    if (expectedMeaningfulKeys.contains("parent") && expected.get("parent") instanceof JsonNull) {
      expectedMeaningfulKeys.remove("parent");
    }

    Set<String> unexpectedKeys = new HashSet<>(actualMeaningfulKeys);
    unexpectedKeys.removeAll(expectedMeaningfulKeys);
    Set<String> missingKeys = new HashSet<>(expectedMeaningfulKeys);
    missingKeys.removeAll(actualMeaningfulKeys);
    if (!unexpectedKeys.isEmpty()) {
      throw new AssertionError("(" + context + ") Unexpected keys found: " + unexpectedKeys);
    }
    if (!missingKeys.isEmpty()) {
      throw new AssertionError("(" + context + ") Missing keys found: " + missingKeys);
    }
    for (String key : actualMeaningfulKeys) {
      switch (key) {
        case "parent":
          assertEquals(
              "(" + context + ") different parent", expected.get("parent"), actual.get("parent"));
          break;
        case "classifier":
          assertEquals(
              "(" + context + ") different classifier",
              expected.get("classifier"),
              actual.get("classifier"));
          break;
        case "id":
          assertEquals("(" + context + ") different id", expected.get("id"), actual.get("id"));
          break;
        case "references":
          assertEquivalentUnorderedArrays(
              expected.getAsJsonArray("references"),
              actual.getAsJsonArray("references"),
              "References of " + context);
          break;
        case "containments":
          assertEquivalentUnorderedArrays(
              expected.getAsJsonArray("containments"),
              actual.getAsJsonArray("containments"),
              "Children of " + context);
          break;
        case "properties":
          assertEquivalentUnorderedArrays(
              expected.getAsJsonArray("properties"),
              actual.getAsJsonArray("properties"),
              "Properties of " + context);
          break;
        case "annotations":
          assertEquivalentUnorderedArrays(
              expected.getAsJsonArray("annotations"),
              actual.getAsJsonArray("annotations"),
              "Annotations of " + context);
          break;
        default:
          throw new AssertionError("(" + context + ") unexpected top-level key found: " + key);
      }
    }
  }

  private static void assertEquivalentUnorderedArrays(
      JsonArray expected, JsonArray actual, String context) {
    if (expected.size() != actual.size()) {
      throw new AssertionError(
          "("
              + context
              + ") Arrays with different sizes: expected="
              + expected.size()
              + " and actual="
              + actual.size());
    }
    Set<Integer> consumedActual = new HashSet<>();
    for (int i = 0; i < expected.size(); i++) {
      JsonObject expectedElement = expected.get(i).getAsJsonObject();
      boolean matchFound = false;
      for (int j = 0; j < actual.size() && !matchFound; j++) {
        if (!consumedActual.contains(j)) {
          if (areEquivalentObjects(expectedElement, actual.get(j).getAsJsonObject())) {
            consumedActual.add(j);
            matchFound = true;
          }
        }
      }
      if (!matchFound) {
        fail(context + " element " + i + " : no equivalent to " + expectedElement + " found");
      }
    }
  }

  private static boolean areEquivalentObjects(JsonObject expected, JsonObject actual) {
    try {
      assertEquivalentObjects(expected, actual, "<IRRELEVANT>");
      return true;
    } catch (AssertionError e) {
      return false;
    }
  }

  private static void assertEquivalentObjects(
      JsonObject expected, JsonObject actual, String context) {
    Set<String> actualMeaningfulKeys =
        actual.keySet().stream()
            .filter(
                k ->
                    !actual.get(k).equals(new JsonObject())
                        && !actual.get(k).equals(new JsonArray())
                        && !actual.get(k).equals(JsonNull.INSTANCE))
            .collect(Collectors.toSet());
    Set<String> expectedMeaningfulKeys =
        expected.keySet().stream()
            .filter(
                k ->
                    !expected.get(k).equals(new JsonObject())
                        && !expected.get(k).equals(new JsonArray())
                        && !expected.get(k).equals(JsonNull.INSTANCE))
            .collect(Collectors.toSet());

    Set<String> unexpectedKeys = new HashSet<>(actualMeaningfulKeys);
    unexpectedKeys.removeAll(expectedMeaningfulKeys);
    Set<String> missingKeys = new HashSet<>(expectedMeaningfulKeys);
    missingKeys.removeAll(actualMeaningfulKeys);
    if (!unexpectedKeys.isEmpty()) {
      throw new AssertionError("(" + context + ") Unexpected keys found: " + unexpectedKeys);
    }
    if (!missingKeys.isEmpty()) {
      throw new AssertionError("(" + context + ") Missing keys found: " + missingKeys);
    }
    for (String key : actualMeaningfulKeys) {
      assertEquals(
          "(" + context + ") Different values for key " + key, expected.get(key), actual.get(key));
    }
  }

  private static void assertEquals(String message, Object expected, Object actual) {
    if (!Objects.equals(expected, actual)) {
      throw new AssertionError(message + ": expected " + expected + " but found" + actual);
    }
  }

  private static void fail(String message) {
    throw new AssertionError("Comparison failed. " + message);
  }
}
