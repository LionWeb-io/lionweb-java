package io.lionweb.testset;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class InvalidWithLanguage {
  @MethodSource("inputFiles")
  @ParameterizedTest(name = "[{index}] {0}")
  public void assertInvalid(Path path) {
    ALanguageTestset testset = new ALanguageTestset(path) {};
    testset.loadLanguage();
    testset.assertIsNotValid(path);
  }

  public static Stream<Path> inputFiles() {
    Path integrationTests = ATestset.findIntegrationTests();
    Path basePath = integrationTests.resolve("withLanguage").resolve("invalid");
    return Arrays.stream(ATestset.collectJsonFiles(basePath)).map(p -> (Path) p);
  }
}
