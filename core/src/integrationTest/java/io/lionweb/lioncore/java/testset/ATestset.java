package io.lionweb.lioncore.java.testset;

import static org.junit.Assert.*;

import io.lionweb.lioncore.java.language.Language;
import io.lionweb.lioncore.java.model.Node;
import io.lionweb.lioncore.java.serialization.JsonSerialization;
import io.lionweb.lioncore.java.serialization.LowLevelJsonSerialization;
import io.lionweb.lioncore.java.serialization.SerializationProvider;
import io.lionweb.lioncore.java.utils.LanguageValidator;
import io.lionweb.lioncore.java.utils.NodeTreeValidator;
import io.lionweb.lioncore.java.utils.ValidationResult;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Set;
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
    Node firstNode =
        parse(path, SerializationProvider.getStandardJsonSerialization()).iterator().next();
    assertTrue(firstNode.getClass().toString(), firstNode instanceof Language);
    Language result = (Language) firstNode;
    LanguageValidator.ensureIsValid(result);
    return result;
  }

  protected static List<Node> parse(Path path, JsonSerialization serialization) {
    try {
      File myLangFile = path.toFile();
      List<Node> nodes = serialization.deserializeToNodes(myLangFile);
      return nodes;
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  protected static Object[] collectJsonFiles(Path basePath) {
    return collectJsonFiles(basePath, Collections.emptySet());
  }

  protected static Object[] collectJsonFiles(Path basePath, Set<Path> pathsToIgnore) {
    try (Stream<Path> files = Files.walk(basePath)) {
      Object[] result =
          files
              .filter(Files::isRegularFile)
              .filter(f -> f.toString().endsWith(".json"))
              .filter(f -> !pathsToIgnore.contains(basePath.relativize(f)))
              .toArray();
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

  protected void assertNodeIsValid(Path path, ValidationResult validationResult) {
    if (!validationResult.isSuccessful()) {
      fail("Fail processing a node in " + path + ": " + validationResult.getIssues());
    }
  }

  protected void assertCanBeLoadedAtLowLevel(Path path) {
    try {
      new LowLevelJsonSerialization().deserializeSerializationBlock(path.toFile());
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  protected void assertIsNotValid(Path path) {
    try {
      List<Node> nodes = parse(path, SerializationProvider.getStandardJsonSerialization());
      for (Node n : nodes) {
        if (!new NodeTreeValidator().isValid(n)) {
          // Good, at least a node is not valid
          return;
        }
      }
      fail("All nodes are valid in " + path);
    } catch (RuntimeException e) {
      // failed, ok, it is invalid
    }
  }
}
