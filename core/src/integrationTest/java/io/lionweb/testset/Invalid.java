package io.lionweb.testset;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class Invalid {
  @MethodSource("inputFiles")
  @ParameterizedTest(name = "[{index}] {0}")
  public void assertInvalid(Path path) {
    ATestset testset = new ATestset(path) {};
    testset.assertIsNotValid(path);
  }

  public static Stream<Path> inputFiles() {
    Path integrationTests = ATestset.findIntegrationTests();
    Path basePath = integrationTests.resolve("invalid");
    return Arrays.stream(
            ATestset.collectJsonFiles(
                basePath, ignored.stream().map(s -> Paths.get(s)).collect(Collectors.toSet())))
        .map(p -> (Path) p);
  }

  private static final Set<String> ignored =
      new HashSet<>(
          Arrays.asList(
              "json/serializationFormatVersion/duplicateKey.json",
              "json/wrongOrder.json",
              "json/languages/duplicateKey.json",
              "json/languages/key/duplicateKey.json",
              "json/languages/version/duplicateKey.json",
              "format/languages/key/empty.json", // this error would be caught when we do a node
              // validation, but that requires having the
              // language
              "format/languages/key/space.json", // this error would be caught when we do a node
              // validation, but that requires having the
              // language));
              "format/languages/duplicateValue.json",
              "format/languages/version/empty.json"));
}
