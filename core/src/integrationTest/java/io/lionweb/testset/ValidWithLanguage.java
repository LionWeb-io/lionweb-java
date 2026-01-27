package io.lionweb.testset;

import static org.junit.jupiter.api.Assertions.assertFalse;

import io.lionweb.model.Node;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class ValidWithLanguage {
  @MethodSource("inputFiles")
  @ParameterizedTest(name = "[{index}] {0}")
  public void assertValid(Path path) {
    ALanguageTestset testset = new ALanguageTestset(path) {};
    testset.loadLanguage();
    try {
      List<Node> nodes = ATestset.parse(path, testset.getSerialization());
      assertFalse(nodes.isEmpty(), path.toString());
    } catch (RuntimeException e) {
      throw new RuntimeException("Issue while parsing " + path, e);
    }
  }

  public static Stream<Path> inputFiles() {
    Path integrationTests = ATestset.findIntegrationTests();
    Path basePath = integrationTests.resolve("withLanguage").resolve("valid");
    return Arrays.stream(
            ATestset.collectJsonFiles(
                basePath, ignored.stream().map(s -> Paths.get(s)).collect(Collectors.toSet())))
        .map(p -> (Path) p);
  }

  private static final Set<String> ignored =
      new HashSet<>(
          Arrays.asList(
              "properties/integer/positiveLong.json", "properties/integer/negativeLong.json"));
}
