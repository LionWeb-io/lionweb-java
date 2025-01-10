package io.lionweb.lioncore.java.self;

import static io.lionweb.lioncore.java.serialization.SerializationProvider.getStandardJsonSerialization;
import static org.junit.Assert.assertTrue;

import io.lionweb.lioncore.java.LionWebVersion;
import io.lionweb.lioncore.java.language.Language;
import io.lionweb.lioncore.java.language.LionCoreBuiltins;
import io.lionweb.lioncore.java.model.Node;
import io.lionweb.lioncore.java.serialization.JsonSerialization;
import io.lionweb.lioncore.java.utils.ModelComparator;
import java.io.*;
import java.net.URL;
import java.util.List;
import org.junit.Test;

public class CorrespondanceWithDocumentationTest {

  private static final String SPECIFICATION_2023_1_COMMIT_CONSIDERED =
      "cf17074aad9552a78100b7ccead395a1eeba5fca";

  private static final String SPECIFICATION_2024_1_COMMIT_CONSIDERED =
      "cf17074aad9552a78100b7ccead395a1eeba5fca";

  private static final String SPECIFICATION_LIONCORE_2023_1_PATH =
      "/2023.1/metametamodel/lioncore.json";
  private static final String SPECIFICATION_LIONCORE_2024_1_PATH =
      "/2024.1/metametamodel/lioncore.json";
  private static final String SPECIFICATION_LIONCOREBUILTINS_2023_1_PATH =
      "/2023.1/metametamodel/builtins.json";
  private static final String SPECIFICATION_LIONCOREBUILTINS_2024_1_PATH =
      "/2024.1/metametamodel/builtins.json";

  @Test
  public void lioncoreIsTheSameAsInTheOrganizationRepo2023_1() throws IOException {
    JsonSerialization jsonSer = getStandardJsonSerialization(LionWebVersion.v2023_1);

    URL url =
        new URL(
            "https://raw.githubusercontent.com/LionWeb-io/specification/"
                + SPECIFICATION_2023_1_COMMIT_CONSIDERED
                + SPECIFICATION_LIONCORE_2023_1_PATH);
    List<Node> nodes = jsonSer.deserializeToNodes(url);

    Language deserializedLioncore = (Language) nodes.get(0);
    ModelComparator.ComparisonResult comparison =
        new ModelComparator()
            .compare(deserializedLioncore, LionCore.getInstance(LionWebVersion.v2023_1));
    System.out.println("Differences " + comparison.getDifferences().size());
    for (String difference : comparison.getDifferences()) {
      System.out.println(" - " + difference);
    }
    assertTrue(comparison.toString(), comparison.areEquivalent());
  }

  // This is failing pending the resolution of
  // https://github.com/LionWeb-io/specification/issues/324
  @Test
  public void lioncoreIsTheSameAsInTheOrganizationRepo2024_1() throws IOException {
    JsonSerialization jsonSer = getStandardJsonSerialization(LionWebVersion.v2024_1);

    URL url =
        new URL(
            "https://raw.githubusercontent.com/LionWeb-io/specification/"
                + SPECIFICATION_2024_1_COMMIT_CONSIDERED
                + SPECIFICATION_LIONCORE_2024_1_PATH);
    List<Node> nodes = jsonSer.deserializeToNodes(url);

    Language deserializedLioncore = (Language) nodes.get(0);
    ModelComparator.ComparisonResult comparison =
        new ModelComparator()
            .compare(deserializedLioncore, LionCore.getInstance(LionWebVersion.v2024_1));
    System.out.println("Differences " + comparison.getDifferences().size());
    for (String difference : comparison.getDifferences()) {
      System.out.println(" - " + difference);
    }
    assertTrue(comparison.toString(), comparison.areEquivalent());
  }

  @Test
  public void builtInIsTheSameAsInTheOrganizationRepo2023_1() throws IOException {
    JsonSerialization jsonSer = getStandardJsonSerialization(LionWebVersion.v2023_1);

    URL url =
        new URL(
            "https://raw.githubusercontent.com/LionWeb-io/specification/"
                + SPECIFICATION_2023_1_COMMIT_CONSIDERED
                + SPECIFICATION_LIONCOREBUILTINS_2023_1_PATH);
    List<Node> nodes = jsonSer.deserializeToNodes(url);

    Language deserializedBuiltins = (Language) nodes.get(0);
    ModelComparator.ComparisonResult comparison =
        new ModelComparator()
            .compare(deserializedBuiltins, LionCoreBuiltins.getInstance(LionWebVersion.v2023_1));
    System.out.println("Differences " + comparison.getDifferences().size());
    for (String difference : comparison.getDifferences()) {
      System.out.println(" - " + difference);
    }
    assertTrue(comparison.toString(), comparison.areEquivalent());
  }

  @Test
  public void builtInIsTheSameAsInTheOrganizationRepo2024_1() throws IOException {
    JsonSerialization jsonSer = getStandardJsonSerialization(LionWebVersion.v2024_1);

    URL url =
        new URL(
            "https://raw.githubusercontent.com/LionWeb-io/specification/"
                + SPECIFICATION_2024_1_COMMIT_CONSIDERED
                + SPECIFICATION_LIONCOREBUILTINS_2024_1_PATH);
    List<Node> nodes = jsonSer.deserializeToNodes(url);

    Language deserializedBuiltins = (Language) nodes.get(0);
    ModelComparator.ComparisonResult comparison =
        new ModelComparator()
            .compare(deserializedBuiltins, LionCoreBuiltins.getInstance(LionWebVersion.v2024_1));
    System.out.println("Differences " + comparison.getDifferences().size());
    for (String difference : comparison.getDifferences()) {
      System.out.println(" - " + difference);
    }
    assertTrue(comparison.toString(), comparison.areEquivalent());
  }
}
