package io.lionweb.lioncore.java.testset;

import static org.junit.Assert.assertThrows;

import io.lionweb.lioncore.java.model.Node;
import io.lionweb.lioncore.java.serialization.JsonSerialization;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class Invalid {
  private final Path path;

  @Parameterized.Parameters
  public static Object[] inputFiles() {
    Path integrationTests = findIntegrationTests();
    Path basePath = integrationTests.resolve("testset").resolve("invalid");
    try {
      Object[] result =
          Files.walk(basePath)
              .filter(Files::isRegularFile)
              .filter(f -> f.toString().endsWith(".json"))
              .toArray();
      return result;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public Invalid(Path path) {
    this.path = path;
  }

  @Test
  public void assertInvalid() {
    assertThrows(
        path.toString(),
        RuntimeException.class,
        () -> {
          List<Node> nodes =
              JsonSerialization.getStandardSerialization().unserializeToNodes(path.toFile());
          System.out.println(nodes);
        });
  }

  private static Path findIntegrationTests() {
    String propertyValue = System.getenv("integrationTestingDir");
    if (propertyValue != null) {
      return Path.of(propertyValue);
    }
    throw new IllegalArgumentException("environment variable integrationTestingDir not defined.");
  }
}
