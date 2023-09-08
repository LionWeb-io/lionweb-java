package io.lionweb.lioncore.java.testset;

import static org.junit.Assert.assertThrows;

import io.lionweb.lioncore.java.serialization.JsonSerialization;
import java.nio.file.Path;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class Invalid extends ATestset {
  @Parameterized.Parameters
  public static Object[] inputFiles() {
    Path integrationTests = findIntegrationTests();
    Path basePath = integrationTests.resolve("invalid");
    Object[] result = collectJsonFiles(basePath);
    return result;
  }

  public Invalid(Path path) {
    super(path);
  }

  @Test
  public void assertInvalid() {
    assertThrows(
        path.toString(),
        RuntimeException.class,
        () -> System.out.println(parse(path, JsonSerialization.getStandardSerialization())));
  }
}
