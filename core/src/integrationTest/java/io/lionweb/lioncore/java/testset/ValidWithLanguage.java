package io.lionweb.lioncore.java.testset;

import static org.junit.Assert.assertFalse;

import io.lionweb.lioncore.java.model.Node;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class ValidWithLanguage extends ALanguageTestset {
  @Parameterized.Parameters(name="[{index}] {0}")
  public static Object[] inputFiles() {
    Path integrationTests = findIntegrationTests();
    Path basePath = integrationTests.resolve("withLanguage").resolve("valid");
    Object[] result =
        collectJsonFiles(
            basePath, ignored.stream().map(s -> Paths.get(s)).collect(Collectors.toSet()));
    return result;
  }

  public ValidWithLanguage(Path path) {
    super(path);
  }

  @Test
  public void assertValid() {
    try {
      List<Node> nodes = parse(path, getSerialization());
      assertFalse(path.toString(), nodes.isEmpty());
    } catch (RuntimeException e) {
      throw new RuntimeException("Issue while parsing " + path, e);
    }
  }

  private static final Set<String> ignored =
      new HashSet<>(
          Arrays.asList(
              "properties/integer/positiveLong.json", "properties/integer/negativeLong.json"));
}
