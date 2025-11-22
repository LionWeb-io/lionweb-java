package io.lionweb.gradleplugin.tasks;

import io.lionweb.serialization.LowLevelJsonSerialization;
import io.lionweb.serialization.data.SerializationChunk;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.OutputDirectory;

public abstract class AbstractGenerationTask extends DefaultTask {
  @InputDirectory
  public abstract DirectoryProperty getLanguagesDirectory();

  @OutputDirectory
  public abstract DirectoryProperty getGenerationDirectory();

  @Input
  public abstract Property<String> getPackageName();

  @Input
  public abstract MapProperty<String, String> getPrimitiveTypes();

  protected List<SerializationChunk> loadChunks(File languagesDirectory) throws IOException {
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
