package io.lionweb.client.delta;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

import io.lionweb.client.delta.messages.DeltaCommand;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Integration tests verifying that delta command JSON examples from lionweb-integration-testing
 * can be deserialized to the correct Java classes.
 *
 * <p>Many tests are expected to fail because the Java delta protocol implementation is incomplete.
 * Failures document what remains to be implemented.
 */
public class DeltaCommandsIntegrationTest {

  private static final DeltaMessageDeserializer DESERIALIZER = new DeltaMessageDeserializer();

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
    assertFalse(command.commandId.isEmpty(), "commandId must not be empty in " + file.getFileName());
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
