package io.lionweb.gradleplugin.tasks;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import io.lionweb.serialization.LowLevelJsonSerialization;
import io.lionweb.serialization.data.SerializationChunk;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.gradle.api.DefaultTask;
import org.gradle.api.artifacts.UnknownConfigurationException;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.*;
import org.gradle.api.tasks.Optional;

public abstract class AbstractGenerationTask extends DefaultTask {
  @InputDirectory
  @Optional
  @PathSensitive(PathSensitivity.RELATIVE)
  public abstract DirectoryProperty getLanguagesDirectory();

  @OutputDirectory
  public abstract DirectoryProperty getGenerationDirectory();

  @Input
  @Optional
  public abstract Property<String> getDefaultPackageName();

  @Input
  public abstract MapProperty<String, String> getPrimitiveTypes();

  @Input
  public abstract MapProperty<String, String> getLanguagesSpecificPackages();

  @Input
  public abstract MapProperty<String, String> getLanguagesClassNames();

  @Input
  @Optional
  public abstract SetProperty<String> getLanguagesToGenerate();

  @Input
  public abstract MapProperty<String, String> getMappings();

  protected List<SerializationChunk> loadDependenciesChunks() throws IOException {
    List<SerializationChunk> dependenciesChunks = new LinkedList<>();
    Set<File> classpath = Collections.emptySet();
    try {
      getProject().getConfigurations().getByName("compileClasspath").resolve();
    } catch (UnknownConfigurationException e) {
      getLogger()
          .warn("No compileClasspath configuration found, skipping LionWeb dependency scanning");
    }
    classpath.stream()
        .filter(f -> f.getName().endsWith(".jar"))
        .forEach(
            jar -> {
              try (JarFile jarFile = new JarFile(jar)) {
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                  JarEntry entry = entries.nextElement();
                  if (entry.getName().startsWith("META-INF/lionweb/")
                      && entry.getName().endsWith(".json")) {
                    try {
                      InputStream inputStream = jarFile.getInputStream(entry);
                      JsonElement je =
                          JsonParser.parseReader(new java.io.InputStreamReader(inputStream));
                      dependenciesChunks.add(
                          new LowLevelJsonSerialization().deserializeSerializationBlock(je));
                    } catch (Exception e) {
                      getLogger()
                          .error(
                              "Error reading jar file entry: "
                                  + jar.getAbsolutePath()
                                  + "#"
                                  + entry.getName(),
                              e);
                    }
                  }
                }
              } catch (IOException e) {
                getLogger().error("Error reading jar file: " + jar.getAbsolutePath(), e);
              }
            });
    return dependenciesChunks;
  }

  protected List<SerializationChunk> loadProjectChunks(File languagesDirectory) throws IOException {
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
      List<SerializationChunk> projectChunks =
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
      return projectChunks;
    }
  }
}
