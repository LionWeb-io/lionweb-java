package io.lionweb.gradleplugin.tasks;

import io.lionweb.LionWebVersion;
import io.lionweb.gradleplugin.generators.NodeClassesJavaCodeGenerator;
import io.lionweb.language.Language;
import io.lionweb.serialization.JsonSerialization;
import io.lionweb.serialization.SerializationProvider;
import io.lionweb.serialization.TopologicalLanguageSorter;
import io.lionweb.serialization.data.SerializationChunk;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;

public abstract class GenerateNodeClassesTask extends AbstractGenerationTask {

  @TaskAction
  public void run() {
    getLogger().info("GenerateNodeClassesTask - Starting");
    if (!getLanguagesDirectory().isPresent()) {
      throw new GradleException("Languages directory not specified");
    }
    if (!getGenerationDirectory().isPresent()) {
      throw new GradleException("Generation directory not specified");
    }
    File languagesDirectory = getLanguagesDirectory().getAsFile().get();
    if (!languagesDirectory.exists() || !languagesDirectory.isDirectory()) {
      getLogger()
          .error(
              "GenerateNodeClassesTask - Languages directory does not exist or is not a directory");
      throw new GradleException("Languages directory does not exist or is not a directory");
    }
    File generationDirectory = getGenerationDirectory().getAsFile().get();
    NodeClassesJavaCodeGenerator nodeClassesJavaCodeGenerator =
        new NodeClassesJavaCodeGenerator(generationDirectory);
    try {
      List<SerializationChunk> chunks = loadChunks(languagesDirectory);
      Arrays.stream(LionWebVersion.values())
          .forEach(
              lionWebVersion -> {
                generateNodeClassesForChunks(lionWebVersion, chunks, nodeClassesJavaCodeGenerator);
              });
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }

    getLogger().lifecycle("Java code for LionWeb Node Classes generated");
  }

  private void generateNodeClassesForChunks(
      LionWebVersion lionWebVersion,
      List<SerializationChunk> chunks,
      NodeClassesJavaCodeGenerator nodeClassesJavaCodeGenerator) {
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
                      (Language)
                          serialization.deserializeSerializationChunk(chunk).stream()
                              .filter(n -> n.getParent() == null)
                              .findFirst()
                              .get();
                  getLogger().info("LionWeb Language loaded: " + language.getName());
                  serialization.registerLanguage(language);
                  return language;
                })
            .collect(Collectors.toList());

      nodeClassesJavaCodeGenerator.generate(languages, getDefaultPackageName().getOrNull(),
              getLanguagesSpecificPackages().getOrElse(Collections.emptyMap()),
              getPrimitiveTypes().getOrElse(Collections.emptyMap()));
  }
}
