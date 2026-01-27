package io.lionweb.testset;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class Valid {
  @MethodSource("inputFiles")
  @ParameterizedTest(name = "[{index}] {0}")
  public void assertValid(Path path) {
    ATestset testset = new ATestset(path) {};
    testset.assertCanBeLoadedAtLowLevel(path);
  }

  public static Stream<Path> inputFiles() {
    Path integrationTests = ATestset.findIntegrationTests();
    Path basePath = integrationTests.resolve("valid");
    return Arrays.stream(ATestset.collectJsonFiles(basePath)).map(p -> (Path) p);
  }
}
