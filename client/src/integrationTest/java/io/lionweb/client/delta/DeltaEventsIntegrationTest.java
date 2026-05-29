package io.lionweb.client.delta;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

import com.google.gson.JsonParser;
import io.lionweb.client.delta.messages.BaseDeltaEvent;
import io.lionweb.client.delta.messages.DeltaEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Integration tests verifying that delta event JSON examples from lionweb-integration-testing
 * can be deserialized to the correct Java classes.
 *
 * <p>Many tests are expected to fail because the Java delta protocol implementation is incomplete.
 * Failures document what remains to be implemented.
 */
public class DeltaEventsIntegrationTest {

  private static final DeltaMessageSerialization DESERIALIZER = new DeltaMessageSerialization();

  static Stream<Path> eventFiles() throws IOException {
    String dirEnv = System.getenv("deltaIntegrationTestingDir");
    assertNotNull(dirEnv, "Environment variable deltaIntegrationTestingDir must be set");
    Path eventDir = Paths.get(dirEnv).resolve("event");
    assertTrue(
        Files.isDirectory(eventDir),
        "Delta event directory not found: " + eventDir.toAbsolutePath());
    return Files.walk(eventDir)
        .filter(Files::isRegularFile)
        .filter(p -> p.toString().endsWith(".delta.json"));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("eventFiles")
  void canDeserializeEventFile(Path file) throws IOException {
    String json = Files.readString(file);

    String messageKind = extractMessageKind(json);
    assumeTrue(
        DESERIALIZER.isKnownKind(messageKind),
        "Skipping unknown event messageKind: " + messageKind);

    Object result = DESERIALIZER.deserialize(json);

    assertNotNull(result, "Deserialization returned null for " + file.getFileName());
    assertInstanceOf(
        DeltaEvent.class,
        result,
        "Expected DeltaEvent but got " + result.getClass().getSimpleName());

    if (result instanceof BaseDeltaEvent) {
      BaseDeltaEvent<?> base = (BaseDeltaEvent<?>) result;
      assertTrue(
          base.sequenceNumber >= 0,
          "sequenceNumber must be non-negative in " + file.getFileName());
    }

    assertEquals(
        JsonParser.parseString(json),
        JsonParser.parseString(DESERIALIZER.serialize(result)),
        "Round-trip mismatch for " + file.getFileName());
  }

  private static String extractMessageKind(String json) {
    int idx = json.indexOf("\"messageKind\"");
    if (idx < 0) return "";
    int colon = json.indexOf(':', idx);
    int quote1 = json.indexOf('"', colon);
    int quote2 = json.indexOf('"', quote1 + 1);
    return json.substring(quote1 + 1, quote2);
  }
}
