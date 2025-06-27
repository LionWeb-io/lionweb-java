package io.lionweb.testset;

import java.nio.file.Path;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class InvalidWithLanguage extends ALanguageTestset {
  @Parameterized.Parameters(name = "[{index}] {0}")
  public static Object[] inputFiles() {
    Path integrationTests = findIntegrationTests();
    Path basePath = integrationTests.resolve("withLanguage").resolve("invalid");
    Object[] result = collectJsonFiles(basePath);
    return result;
  }

  public InvalidWithLanguage(Path path) {
    super(path);
  }

  @Test
  public void assertInvalid() {
    assertIsNotValid(path);
  }
}
