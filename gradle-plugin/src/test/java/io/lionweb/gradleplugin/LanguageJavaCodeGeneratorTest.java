package io.lionweb.gradleplugin;

import io.lionweb.LionWebVersion;
import io.lionweb.gradleplugin.generators.LanguageJavaCodeGenerator;
import io.lionweb.language.Language;
import io.lionweb.language.LionCoreBuiltins;
import io.lionweb.lioncore.LionCore;
import io.lionweb.serialization.JsonSerialization;
import io.lionweb.serialization.LowLevelJsonSerialization;
import io.lionweb.serialization.SerializationProvider;
import io.lionweb.serialization.TopologicalLanguageSorter;
import io.lionweb.serialization.data.SerializationChunk;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

public class LanguageJavaCodeGeneratorTest {

  @Test
  public void testLibraryGeneration() throws IOException {
    Language library =
        SerializationProvider.getStandardJsonSerialization(LionWebVersion.v2023_1)
            .loadLanguage(this.getClass().getResourceAsStream("/library-language.json"));
    File destination = Files.createTempDirectory("gen").toFile();
    LanguageJavaCodeGenerator generator = new LanguageJavaCodeGenerator(destination);
    generator.generate(library, "my.pack");

    Path javaFile =
        Files.walk(destination.toPath())
            .filter(
                f ->
                    "my/pack/LibraryLanguage.java"
                        .equals(destination.toPath().relativize(f).toString()))
            .findFirst()
            .get();
    String javaCode = new String(Files.readAllBytes(javaFile), StandardCharsets.UTF_8);
    System.out.println(javaCode);
  }

  @Test
  public void testLionCore2023Generation() throws IOException {
    File destination = Files.createTempDirectory("gen").toFile();
    LanguageJavaCodeGenerator generator = new LanguageJavaCodeGenerator(destination);
    generator.generate(LionCore.getInstance(LionWebVersion.v2023_1), "my.pack");

    Path javaFile =
        Files.walk(destination.toPath())
            .filter(
                f ->
                    "my/pack/LionCore_M3Language.java"
                        .equals(destination.toPath().relativize(f).toString()))
            .findFirst()
            .get();
    String javaCode = new String(Files.readAllBytes(javaFile), StandardCharsets.UTF_8);
    System.out.println(javaCode);
  }

  @Test
  public void testLionCore2024Generation() throws IOException {
    File destination = Files.createTempDirectory("gen").toFile();
    LanguageJavaCodeGenerator generator = new LanguageJavaCodeGenerator(destination);
    generator.generate(LionCore.getInstance(LionWebVersion.v2024_1), "my.pack");

    Path javaFile =
        Files.walk(destination.toPath())
            .filter(
                f ->
                    "my/pack/LionCore_M3Language.java"
                        .equals(destination.toPath().relativize(f).toString()))
            .findFirst()
            .get();
    String javaCode = new String(Files.readAllBytes(javaFile), StandardCharsets.UTF_8);
    System.out.println(javaCode);
  }

  @Test
  public void testLionCoreBuiltins2023Generation() throws IOException {
    File destination = Files.createTempDirectory("gen").toFile();
    LanguageJavaCodeGenerator generator = new LanguageJavaCodeGenerator(destination);
    generator.generate(LionCoreBuiltins.getInstance(LionWebVersion.v2023_1), "my.pack");

    Path javaFile =
        Files.walk(destination.toPath())
            .filter(
                f ->
                    "my/pack/LionCore_builtinsLanguage.java"
                        .equals(destination.toPath().relativize(f).toString()))
            .findFirst()
            .get();
    String javaCode = new String(Files.readAllBytes(javaFile), StandardCharsets.UTF_8);
    System.out.println(javaCode);
  }

  @Test
  public void testLionCoreBuiltins2024Generation() throws IOException {
    File destination = Files.createTempDirectory("gen").toFile();
    LanguageJavaCodeGenerator generator = new LanguageJavaCodeGenerator(destination);
    generator.generate(LionCoreBuiltins.getInstance(LionWebVersion.v2024_1), "my.pack");

    Path javaFile =
        Files.walk(destination.toPath())
            .filter(
                f ->
                    "my/pack/LionCore_builtinsLanguage.java"
                        .equals(destination.toPath().relativize(f).toString()))
            .findFirst()
            .get();
    String javaCode = new String(Files.readAllBytes(javaFile), StandardCharsets.UTF_8);
    System.out.println(javaCode);
  }

  @Test
  public void testStarlasuSpecsGeneration() throws IOException {
    File destination = Files.createTempDirectory("gen").toFile();
    LanguageJavaCodeGenerator generator = new LanguageJavaCodeGenerator(destination);

    List<String> paths =
        Arrays.asList(
            "/ast.language.v1.json",
            "/ast.language.v2.json",
            "/codebase.language.v1.json",
            "/codebase.language.v2.json",
            "/comments.language.v1.json",
            "/migration.language.v1.json",
            "/pipeline.language.v1.json");
    // TODO load languages together, in topological order
    // TODO generate for all the languages at once
    List<SerializationChunk> chunks =
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
            .collect(Collectors.toList());
    JsonSerialization serialization =
        SerializationProvider.getStandardJsonSerialization(LionWebVersion.v2023_1);
    List<Language> languages =
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
                .collect(Collectors.toList());
    generator.generate(languages, "my.pack");
  }

  private static String read(InputStream in) throws IOException {
    StringBuilder sb = new StringBuilder();
    try (BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
      char[] buf = new char[2048];
      int n;
      while ((n = r.read(buf)) != -1) {
        sb.append(buf, 0, n);
      }
    }
    return sb.toString();
  }
}
