package io.lionweb.lioncore.java.testset;

import static org.junit.Assert.assertFalse;

import io.lionweb.lioncore.java.model.Node;
import java.nio.file.Path;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class ValidWithLanguage extends ALanguageTestset {
  @Parameterized.Parameters
  public static Object[] inputFiles() {
    Path integrationTests = findIntegrationTests();
    Path basePath = integrationTests.resolve("withLanguage").resolve("valid");
    Object[] result = collectJsonFiles(basePath);
    return result;
  }

  public ValidWithLanguage(Path path) {
    super(path);
  }

  @Test
  public void assertValid() {
    List<Node> nodes = parse(path, getSerialization());
    assertFalse(path.toString(), nodes.isEmpty());
  }
}
