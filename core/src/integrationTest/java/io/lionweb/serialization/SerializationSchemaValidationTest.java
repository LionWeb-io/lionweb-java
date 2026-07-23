package io.lionweb.serialization;

import static org.junit.jupiter.api.Assertions.fail;

import com.networknt.schema.Error;
import com.networknt.schema.InputFormat;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.SpecificationVersion;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Validates that the serialization chunk JSONs bundled as test resources conform to the official
 * LionWeb serialization JSON Schema.
 *
 * <p>The schema is not committed to this repository: it is downloaded from the LionWeb
 * specification repository by the {@code downloadJsonSchemas} Gradle task (see
 * core/build.gradle.kts). This test runs as part of the {@code integrationTest} task, which
 * provides the location of both the downloaded schema and the chunks to validate through
 * environment variables.
 */
public class SerializationSchemaValidationTest {

  @MethodSource("chunkFiles")
  @ParameterizedTest(name = "[{index}] {0}")
  public void chunkValidatesAgainstSchema(Path chunkFile) {
    Schema schema = loadSerializationSchema();
    List<Error> errors = schema.validate(readString(chunkFile), InputFormat.JSON);
    if (!errors.isEmpty()) {
      String details =
          errors.stream()
              .map(e -> "  - " + e.getInstanceLocation() + ": " + e.getMessage())
              .collect(Collectors.joining("\n"));
      fail(
          "Serialization chunk "
              + chunkFile.getFileName()
              + " does not validate against the LionWeb serialization schema:\n"
              + details);
    }
  }

  static Stream<Path> chunkFiles() {
    Path chunksDir = requireDir("serializationChunksDir");
    try (Stream<Path> files = Files.list(chunksDir)) {
      return files
          .filter(p -> p.getFileName().toString().endsWith(".json"))
          .sorted()
          .collect(Collectors.toList())
          .stream();
    } catch (IOException e) {
      throw new UncheckedIOException("Unable to list serialization chunks in " + chunksDir, e);
    }
  }

  private static Schema loadSerializationSchema() {
    Path schemaFile = requireDir("jsonSchemasDir").resolve("serialization.schema.json");
    SchemaRegistry registry = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12);
    try (InputStream is = Files.newInputStream(schemaFile)) {
      return registry.getSchema(is);
    } catch (IOException e) {
      throw new UncheckedIOException("Unable to load serialization schema from " + schemaFile, e);
    }
  }

  private static String readString(Path path) {
    try {
      return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new UncheckedIOException("Unable to read " + path, e);
    }
  }

  private static Path requireDir(String envVar) {
    String value = System.getenv(envVar);
    if (value == null) {
      throw new IllegalStateException("environment variable " + envVar + " not defined.");
    }
    return Paths.get(value);
  }
}
