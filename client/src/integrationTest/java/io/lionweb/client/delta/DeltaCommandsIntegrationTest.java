package io.lionweb.client.delta;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import io.lionweb.client.delta.messages.DeltaCommand;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Integration tests verifying that delta command JSON examples from lionweb-integration-testing can
 * be deserialized to the correct Java classes.
 *
 * <p>Many tests are expected to fail because the Java delta protocol implementation is incomplete.
 * Failures document what remains to be implemented.
 */
public class DeltaCommandsIntegrationTest {

  private static final DeltaMessageSerialization DESERIALIZER = new DeltaMessageSerialization();

  static Stream<Path> commandFiles() throws IOException {
    String dirEnv = System.getenv("deltaIntegrationTestingDir");
    assertNotNull(dirEnv, "Environment variable deltaIntegrationTestingDir must be set");
    Path commandDir = Paths.get(dirEnv).resolve("command");
    assertTrue(
        Files.isDirectory(commandDir),
        "Delta command directory not found: " + commandDir.toAbsolutePath());
    return Files.walk(commandDir)
        .filter(Files::isRegularFile)
        .filter(p -> p.toString().endsWith(".delta.json"));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("commandFiles")
  void canDeserializeCommandFile(Path file) throws IOException {
    String json = Files.readString(file);

    // Extract messageKind from JSON to skip unknown kinds
    String messageKind = extractMessageKind(json);
    assumeTrue(
        DESERIALIZER.isKnownKind(messageKind),
        "Skipping unknown command messageKind: " + messageKind);

    Object result = DESERIALIZER.deserialize(json);

    assertNotNull(result, "Deserialization returned null for " + file.getFileName());
    assertInstanceOf(
        DeltaCommand.class,
        result,
        "Expected DeltaCommand but got " + result.getClass().getSimpleName());

    DeltaCommand command = (DeltaCommand) result;
    assertNotNull(command.commandId, "commandId must not be null in " + file.getFileName());
    assertFalse(
        command.commandId.isEmpty(), "commandId must not be empty in " + file.getFileName());

    assertJSONEquivalence(
        json, DESERIALIZER.serialize(result), "Round-trip mismatch for " + file.getFileName());
  }

  private static void assertJSONEquivalence(String expected, String actual, String message) {
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
    } else {
      throw new UnsupportedOperationException(
          expected.getClass().getSimpleName() + " not supported yet");
    }
  }

  private static String extractMessageKind(String json) {
    // Simple extraction without full parse overhead
    int idx = json.indexOf("\"messageKind\"");
    if (idx < 0) return "";
    int colon = json.indexOf(':', idx);
    int quote1 = json.indexOf('"', colon);
    int quote2 = json.indexOf('"', quote1 + 1);
    return json.substring(quote1 + 1, quote2);
  }
}
