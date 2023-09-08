package io.lionweb.lioncore.java.testset;

import static org.junit.Assert.assertTrue;

import io.lionweb.lioncore.java.language.Language;
import io.lionweb.lioncore.java.model.Node;
import io.lionweb.lioncore.java.serialization.JsonSerialization;
import io.lionweb.lioncore.java.utils.LanguageValidator;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public abstract class ATestset {
  protected final Path path;

  public ATestset(Path path) {
    this.path = path;
  }

  protected static Language loadLanguage(Path path) {
    Node firstNode = parse(path, JsonSerialization.getStandardSerialization()).iterator().next();
    assertTrue(firstNode.getClass().toString(), firstNode instanceof Language);
    Language result = (Language) firstNode;
    LanguageValidator.ensureIsValid(result);
    return result;
  }

  protected static List<Node> parse(Path path, JsonSerialization serialization) {
    try {
      File myLangFile = path.toFile();
      List<Node> nodes = serialization.unserializeToNodes(myLangFile);
      return nodes;
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  protected static Object[] collectJsonFiles(Path basePath) {
    try (Stream<Path> files = Files.walk(basePath)) {
      Object[] result =
          files.filter(Files::isRegularFile).filter(f -> f.toString().endsWith(".json")).toArray();
      return result;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected static Path findIntegrationTests() {
    String propertyValue = System.getenv("integrationTestingDir");
    if (propertyValue != null) {
      return Paths.get(propertyValue);
    }
    throw new IllegalArgumentException("environment variable integrationTestingDir not defined.");
  }
}
