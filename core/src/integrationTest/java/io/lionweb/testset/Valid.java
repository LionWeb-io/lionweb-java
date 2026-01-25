package io.lionweb.testset;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class Valid extends ATestset {
  @Parameterized.Parameters(name = "[{index}] {0}")
  public static Object[] inputFiles() {
    Path integrationTests = findIntegrationTests();
    Path basePath = integrationTests.resolve("valid");
    Object[] result = collectJsonFiles(basePath);
    return result;
  }

  public Valid(Path path) {
    super(path);
  }

  @Test
  public void assertValid() {
    assertCanBeLoadedAtLowLevel(path);
  }
}
