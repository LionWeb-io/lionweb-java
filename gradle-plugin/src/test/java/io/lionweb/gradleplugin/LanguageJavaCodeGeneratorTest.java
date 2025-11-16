package io.lionweb.gradleplugin;

import io.lionweb.LionWebVersion;
import io.lionweb.language.Language;
import io.lionweb.language.LionCoreBuiltins;
import io.lionweb.lioncore.LionCore;
import io.lionweb.serialization.SerializationProvider;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

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


      List<String> paths = Arrays.asList("ast.language.v1.json",
              "ast.language.v2.json", "codebase.language.v1.json",
              "codebase.language.v2.json", "comments.language.v1.json",
              "migration.language.v1.json", "pipeline.language.v1.json");
      // TODO load languages together, in topological order
      // TODO generate for all the languages at once
      paths.forEach(path -> {
          try {
              Language language =
                      SerializationProvider.getStandardJsonSerialization(LionWebVersion.v2023_1)
                              .loadLanguage(this.getClass().getResourceAsStream("/" + path));
              generator.generate(language, "my.pack");
          } catch (IOException e) {
              throw new RuntimeException(e);
          }
      });
  }

}
