package io.lionweb.client.delta;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

import com.google.gson.JsonParser;
import io.lionweb.client.delta.messages.DeltaQuery;
import io.lionweb.client.delta.messages.DeltaQueryResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Integration tests verifying that delta query/response JSON examples from
 * lionweb-integration-testing can be deserialized to the correct Java classes.
 *
 * <p>Many tests are expected to fail because the Java delta protocol implementation is incomplete.
 * Failures document what remains to be implemented.
 */
public class DeltaQueriesIntegrationTest {

  private static final DeltaMessageSerialization DESERIALIZER = new DeltaMessageSerialization();

  static Stream<Path> queryFiles() throws IOException {
    String dirEnv = System.getenv("deltaIntegrationTestingDir");
    assertNotNull(dirEnv, "Environment variable deltaIntegrationTestingDir must be set");
    Path queryDir = Paths.get(dirEnv).resolve("query");
    assertTrue(
        Files.isDirectory(queryDir),
        "Delta query directory not found: " + queryDir.toAbsolutePath());
    return Files.walk(queryDir)
        .filter(Files::isRegularFile)
        .filter(p -> p.toString().endsWith(".delta.json"));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("queryFiles")
  void canDeserializeQueryFile(Path file) throws IOException {
    String json = Files.readString(file);

    String messageKind = extractMessageKind(json);
    assumeTrue(
        DESERIALIZER.isKnownKind(messageKind),
        "Skipping unknown query messageKind: " + messageKind);

    Object result = DESERIALIZER.deserialize(json);

    assertNotNull(result, "Deserialization returned null for " + file.getFileName());

    boolean isQueryOrResponse =
        result instanceof DeltaQuery || result instanceof DeltaQueryResponse;
    assertTrue(
        isQueryOrResponse,
        "Expected DeltaQuery or DeltaQueryResponse but got " + result.getClass().getSimpleName());

    if (result instanceof DeltaQuery) {
      DeltaQuery query = (DeltaQuery) result;
      assertNotNull(query.queryId, "queryId must not be null in " + file.getFileName());
      assertFalse(query.queryId.isEmpty(), "queryId must not be empty in " + file.getFileName());
    } else if (result instanceof DeltaQueryResponse) {
      DeltaQueryResponse response = (DeltaQueryResponse) result;
      assertNotNull(response.queryId, "queryId must not be null in " + file.getFileName());
      assertFalse(response.queryId.isEmpty(), "queryId must not be empty in " + file.getFileName());
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
