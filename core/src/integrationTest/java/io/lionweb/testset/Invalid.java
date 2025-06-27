package io.lionweb.testset;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class Invalid extends ATestset {
  @Parameterized.Parameters(name = "[{index}] {0}")
  public static Object[] inputFiles() {
    Path integrationTests = findIntegrationTests();
    Path basePath = integrationTests.resolve("invalid");
    Object[] result =
        collectJsonFiles(
            basePath, ignored.stream().map(s -> Paths.get(s)).collect(Collectors.toSet()));
    return result;
  }

  public Invalid(Path path) {
    super(path);
  }

  @Test
  public void assertInvalid() {
    assertIsNotValid(path);
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
