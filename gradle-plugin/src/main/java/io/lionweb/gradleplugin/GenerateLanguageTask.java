package io.lionweb.gradleplugin;

import io.lionweb.LionWebVersion;
import io.lionweb.language.Language;
import io.lionweb.serialization.JsonSerialization;
import io.lionweb.serialization.LowLevelJsonSerialization;
import io.lionweb.serialization.SerializationProvider;
import io.lionweb.serialization.TopologicalLanguageSorter;
import io.lionweb.serialization.data.SerializationChunk;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;

/**
 * The GenerateLanguageTask is an abstract Gradle task used for processing LionWeb Language files,
 * generating Java code representations for the defined languages. The task supports multiple
 * serialization formats and handles language dependencies using a topological sort mechanism.
 *
 * <p>This task works by reading serialized language definitions from a specified directory,
 * processing the provided files in the supported formats (currently JSON), and generating Java
 * classes in the specified output directory.
 *
 * <p>Users can configure the input directory for the language definitions, the target directory for
 * generated code, and the package name to be used for the generated classes. The task validates the
 * input directories, processes the files, and applies the appropriate serialization format for
 * language chunks.
 *
 * <p>Tasks performed include: - Loading serialized language chunks from files in the input
 * directory, filtered by `LionWebVersion`. - Deserializing language specifications and resolving
 * dependencies through a topological sort. - Generating Java code using the specified package name
 * and output directory.
 *
 * <p>If unsupported file formats (such as Protobuf) or issues with serialization are encountered
 * during execution, appropriate exception handling and logging are performed to assist in
 * debugging.
 *
 * <p>Gradle properties: - `languagesDirectory` - Optional directory containing serialized language
 * files. - `generationDirectory` - Optional output directory for generated Java files. -
 * `packageName` - Required property specifying the package for generated Java classes.
 */
public abstract class GenerateLanguageTask extends DefaultTask {
  @InputDirectory
  abstract DirectoryProperty getLanguagesDirectory();

  @OutputDirectory
  abstract DirectoryProperty getGenerationDirectory();

  @Input
  abstract Property<String> getPackageName();

  @TaskAction
  public void run() {
    getLogger().info("GenerateLanguageTask - Starting");
    if (!getLanguagesDirectory().isPresent()) {
      throw new GradleException("Languages directory not specified");
    }
    if (!getGenerationDirectory().isPresent()) {
      throw new GradleException("Generation directory not specified");
    }
    File languagesDirectory = getLanguagesDirectory().getAsFile().get();
    if (!languagesDirectory.exists() || !languagesDirectory.isDirectory()) {
      getLogger()
          .error("GenerateLanguageTask - Languages directory does not exist or is not a directory");
      throw new GradleException("Languages directory does not exist or is not a directory");
    }
    File generationDirectory = getGenerationDirectory().getAsFile().get();
    LanguageJavaCodeGenerator languageJavaCodeGenerator =
        new LanguageJavaCodeGenerator(generationDirectory);
    try {
      List<SerializationChunk> chunks = loadChunks(languagesDirectory);
      Arrays.stream(LionWebVersion.values())
          .forEach(
              lionWebVersion -> {
                generateLanguagesForChunks(lionWebVersion, chunks, languageJavaCodeGenerator);
              });
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }

    getLogger().lifecycle("Java code for LionWeb Languages generated");
  }

  private void generateLanguagesForChunks(
      LionWebVersion lionWebVersion,
      List<SerializationChunk> chunks,
      LanguageJavaCodeGenerator languageJavaCodeGenerator) {
    TopologicalLanguageSorter sorter = new TopologicalLanguageSorter(lionWebVersion);
    List<SerializationChunk> sortedChunks =
        sorter.topologicalSort(
            chunks.stream()
                .filter(
                    c ->
                        c.getSerializationFormatVersion().equals(lionWebVersion.getVersionString()))
                .collect(Collectors.toList()));
    JsonSerialization serialization =
        SerializationProvider.getStandardJsonSerialization(lionWebVersion);
    List<Language> languages =
        sortedChunks.stream()
            .map(
                chunk -> {
                  Language language =
                      (Language) serialization.deserializeSerializationChunk(chunk).get(0);
                  getLogger().info("LionWeb Language loaded: " + language.getName());
                  serialization.registerLanguage(language);
                  return language;
                })
            .collect(Collectors.toList());
    try {
      languageJavaCodeGenerator.generate(languages, getPackageName().get());
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }

  private List<SerializationChunk> loadChunks(File languagesDirectory) throws IOException {
    try (Stream<Path> stream = Files.walk(languagesDirectory.toPath())) {
      List<Path> files =
          stream
              .filter(Files::isRegularFile)
              .filter(f -> f.toString().endsWith(".json") || f.toString().endsWith(".pb"))
              .collect(Collectors.toList());
      if (files.isEmpty()) {
        getLogger().warn("GenerateLanguageTask - No files found");
        return Collections.emptyList();
      }
      getLogger().lifecycle("Language files found: " + files.size());
      List<SerializationChunk> chunks =
          files.stream()
              .map(
                  f -> {
                    try {
                      if (f.toString().endsWith(".json")) {
                        return new LowLevelJsonSerialization()
                            .deserializeSerializationBlock(f.toFile());
                      } else if (f.toString().endsWith(".pb")) {
                        throw new UnsupportedOperationException("Protobuf not yet supported");
                      } else {
                        throw new UnsupportedOperationException(
                            "Unsupported file extension: <" + f.toString() + ">");
                      }
                    } catch (IOException e) {
                      throw new RuntimeException(e);
                    }
                  })
              .collect(Collectors.toList());
      return chunks;
    }
  }
}
