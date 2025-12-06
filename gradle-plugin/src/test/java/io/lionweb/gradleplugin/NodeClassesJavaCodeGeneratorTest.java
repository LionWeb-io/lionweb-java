package io.lionweb.gradleplugin;

import io.lionweb.LionWebVersion;
import io.lionweb.gradleplugin.generators.LanguageJavaCodeGenerator;
import io.lionweb.gradleplugin.generators.NodeClassesJavaCodeGenerator;
import io.lionweb.language.Language;
import io.lionweb.language.LionCoreBuiltins;
import io.lionweb.lioncore.LionCore;
import io.lionweb.serialization.JsonSerialization;
import io.lionweb.serialization.LowLevelJsonSerialization;
import io.lionweb.serialization.SerializationProvider;
import io.lionweb.serialization.TopologicalLanguageSorter;
import io.lionweb.serialization.data.SerializationChunk;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class NodeClassesJavaCodeGeneratorTest extends AbstractGeneratorTest {

  @Test
  public void testStarlasuSpecsGeneration() throws IOException {
    //File destination = Files.createTempDirectory("gen").toFile();
      File destination = new File("src/test/java/");
      LanguageJavaCodeGenerator languagesGenerator = new LanguageJavaCodeGenerator(destination);
      NodeClassesJavaCodeGenerator nodeClassesGenerator = new NodeClassesJavaCodeGenerator(destination);

    Set<String> paths =
        new HashSet<>(
            Arrays.asList(
                "/ast.language.v1.json",
                "/ast.language.v2.json",
                "/codebase.language.v1.json",
                "/codebase.language.v2.json",
                "/comments.language.v1.json",
                "/migration.language.v1.json",
                "/pipeline.language.v1.json"));
    Set<SerializationChunk> chunks =
        paths.stream()
            .map(
                path -> {
                  try {
                    String json = read(this.getClass().getResourceAsStream(path));
                    return new LowLevelJsonSerialization().deserializeSerializationBlock(json);
                  } catch (IOException e) {
                    throw new RuntimeException(e);
                  }
                })
            .collect(Collectors.toSet());
    JsonSerialization serialization =
        SerializationProvider.getStandardJsonSerialization(LionWebVersion.v2023_1);
    Set<Language> languages =
        new TopologicalLanguageSorter(LionWebVersion.v2023_1)
            .topologicalSort(chunks).stream()
                .map(
                    chunk -> {
                      Language language =
                          (Language)
                              serialization.deserializeSerializationChunk(chunk).stream()
                                  .filter(n -> n.getParent() == null)
                                  .findFirst()
                                  .get();
                      serialization.registerLanguage(language);
                      return language;
                    })
                .collect(Collectors.toSet());
    Map<String, String> primitiveTypes = new HashMap<>();
    primitiveTypes.put("com-strumenta-StarLasu-TokensList-id", "dummy.com.strumenta.starlasu.TokensList");
    primitiveTypes.put("com-strumenta-Starlasu-v2-TokensList-2-id", "dummy.com.strumenta.starlasu.TokensList");
    primitiveTypes.put("com-strumenta-StarLasu-Position-id", "dummy.com.strumenta.starlasu.Position");
    primitiveTypes.put("com-strumenta-Starlasu-v2-Position-2-id", "dummy.com.strumenta.starlasu.Position");
      languagesGenerator.generate(languages, "my.pack");
    nodeClassesGenerator.generate(languages, "my.pack", Collections.emptyMap(), primitiveTypes);
    assertTrue(compileAllJavaFiles(destination));
  }
}
