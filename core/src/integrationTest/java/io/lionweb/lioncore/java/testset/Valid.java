package io.lionweb.lioncore.java.testset;

import static org.junit.Assert.fail;

import io.lionweb.lioncore.java.model.Node;
import io.lionweb.lioncore.java.utils.NodeTreeValidator;
import io.lionweb.lioncore.java.utils.ValidationResult;
import java.nio.file.Path;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class Valid extends ALanguageTestset {
  @Parameterized.Parameters
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
    List<Node> nodes = parse(path, getSerialization());
    nodes.forEach(n -> assertNodeIsValid(path, new NodeTreeValidator().validate(n)));
  }

  private void assertNodeIsValid(Path path, ValidationResult validationResult) {
    if (!validationResult.isSuccessful()) {
      fail("Fail processing a node in " + path + ": " + validationResult.getIssues());
    }
  }
}
