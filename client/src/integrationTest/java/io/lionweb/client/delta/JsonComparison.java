package io.lionweb.client.delta;

import static org.junit.jupiter.api.Assertions.fail;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

class JsonComparison {
  static void assertJSONEquivalence(String expected, String actual, String message) {
    List<String> diffs =
        findJsonDiffs(JsonParser.parseString(expected), JsonParser.parseString(actual), "<ROOT>");
    if (!diffs.isEmpty()) {
      fail(message + ": " + diffs);
    }
  }

  private static List<String> findJsonDiffs(
      JsonElement expected, JsonElement actual, String context) {
    if (expected.isJsonObject()) {
      if (actual.isJsonObject()) {
        Set<String> expectedKeys = expected.getAsJsonObject().keySet();
        Set<String> actualKeys = actual.getAsJsonObject().keySet();

        // Canonicize
        if (expectedKeys.contains("split")
            && !expected.getAsJsonObject().get("split").getAsBoolean()) {
          expectedKeys.remove("split");
        }
        if (expectedKeys.contains("distribute")
            && !expected.getAsJsonObject().get("distribute").getAsBoolean()) {
          expectedKeys.remove("distribute");
        }

        if (expectedKeys.equals(actualKeys)) {
          List<String> diffs = new LinkedList<>();
          for (String key : expectedKeys) {
            JsonElement expectedValue = expected.getAsJsonObject().get(key);
            JsonElement actualValue = actual.getAsJsonObject().get(key);
            diffs.addAll(findJsonDiffs(expectedValue, actualValue, context + "." + key));
          }
          return diffs;
        } else {
          String missingKeys =
              expectedKeys.stream()
                  .filter(k -> !actualKeys.contains(k))
                  .collect(java.util.stream.Collectors.joining(", "));
          String unexpectedKeys =
              actualKeys.stream()
                  .filter(k -> !expectedKeys.contains(k))
                  .collect(java.util.stream.Collectors.joining(", "));
          String msg = context + " expected keys " + expectedKeys + " but got " + actualKeys + ".";
          if (!missingKeys.isEmpty()) {
            msg = msg + " Missing keys: " + missingKeys;
          }
          if (!unexpectedKeys.isEmpty()) {
            msg = msg + " Unexpected keys: " + unexpectedKeys;
          }
          return List.of(msg);
        }
      } else {
        return List.of(
            context + " expected JsonObject but got " + actual.getClass().getSimpleName());
      }
    } else if (expected.isJsonPrimitive()) {
      if (actual.isJsonPrimitive()) {
        if (expected.getAsJsonPrimitive().equals(actual.getAsJsonPrimitive())) {
          return List.of();
        } else {
          return List.of(
              context
                  + " expected "
                  + expected.getAsJsonPrimitive()
                  + " but got "
                  + actual.getAsJsonPrimitive());
        }
      } else {
        return List.of(
            context + " expected JsonPrimitive but got " + actual.getClass().getSimpleName());
      }
    } else if (expected.isJsonArray()) {
      int expectedSize = expected.getAsJsonArray().size();
      int actualSize = actual.getAsJsonArray().size();
      if (expectedSize == actualSize) {
        List<String> diffs = new LinkedList<>();
        for (int i = 0; i < expectedSize; i++) {
          JsonElement expectedElement = expected.getAsJsonArray().get(i);
          JsonElement actualElement = actual.getAsJsonArray().get(i);
          diffs.addAll(findJsonDiffs(expectedElement, actualElement, context + "[" + i + "]"));
        }
        return diffs;
      } else {
        return List.of(context + " expected " + expectedSize + " elements but got " + actualSize);
      }
    } else if (expected.isJsonNull()) {
      if (actual.isJsonNull()) {
        return List.of();
      } else {
        return List.of(context + " expected null but got " + actual.getClass().getSimpleName());
      }
    } else {
      throw new UnsupportedOperationException(
          expected.getClass().getSimpleName() + " not supported yet");
    }
  }

  static String extractMessageKind(String json) {
    // Simple extraction without full parse overhead
    int idx = json.indexOf("\"messageKind\"");
    if (idx < 0) return "";
    int colon = json.indexOf(':', idx);
    int quote1 = json.indexOf('"', colon);
    int quote2 = json.indexOf('"', quote1 + 1);
    return json.substring(quote1 + 1, quote2);
  }
}
