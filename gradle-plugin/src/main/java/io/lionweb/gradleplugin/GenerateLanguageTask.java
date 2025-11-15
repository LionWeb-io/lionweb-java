package io.lionweb.gradleplugin;

import io.lionweb.LionWebVersion;
import io.lionweb.language.Language;
import io.lionweb.serialization.JsonSerialization;
import io.lionweb.serialization.LowLevelJsonSerialization;
import io.lionweb.serialization.SerializationProvider;
import io.lionweb.serialization.data.SerializationChunk;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFile;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class GenerateLanguageTask extends DefaultTask {
  @InputDirectory
  @Optional
  abstract DirectoryProperty getLanguagesDirectory();

  @OutputDirectory
  @Optional
  abstract DirectoryProperty getGenerationDirectory();

  @Input
  abstract Property<String> getPackageName();

  @TaskAction
  public void run() {

      getLogger().info("GenerateLanguageTask - Starting");
      File languagesDirectory = getLanguagesDirectory().isPresent() ? getLanguagesDirectory().getAsFile().get() : new java.io.File(getProject().getProjectDir(), "src/main/lionweb");
      if (!languagesDirectory.exists() || !languagesDirectory.isDirectory()) {
          getLogger().error("GenerateLanguageTask - Languages directory does not exist or is not a directory");
          throw new GradleException("Languages directory does not exist or is not a directory");
      }
      File generationDirectory = getGenerationDirectory().isPresent() ? getGenerationDirectory().getAsFile().get() : new java.io.File(getProject().getLayout().getBuildDirectory().get().getAsFile(), "generated-lionweb");
      LanguageJavaCodeGenerator languageJavaCodeGenerator = new LanguageJavaCodeGenerator(generationDirectory);
      try (Stream<Path> stream = Files.walk(languagesDirectory.toPath())) {
          List<Path> files = stream
                  .filter(Files::isRegularFile)
                  .filter(f -> f.toString().endsWith(".json") || f.toString().endsWith(".pb"))
                  .collect(Collectors.toList());
          if (files.isEmpty()) {
              getLogger().warn("GenerateLanguageTask - No files found");
              return;
          }
          getLogger().lifecycle("Language files found: " + files.size());
          // TODO consider topological order of languages
          files.forEach(f -> {
              try {
                  if (f.toString().endsWith(".json")) {
                      SerializationChunk chunk = new LowLevelJsonSerialization().deserializeSerializationBlock(f.toFile());
                      LionWebVersion lionWebVersion = LionWebVersion.fromValue(chunk.getSerializationFormatVersion());
                      JsonSerialization serialization = SerializationProvider.getStandardJsonSerialization(lionWebVersion);
                      Language language = serialization.loadLanguage(f.toFile());
                      getLogger().lifecycle("LionWeb Language loaded: " + language.getName());
                      languageJavaCodeGenerator.generate(language, getPackageName().get());
                  } else if (f.toString().endsWith(".pb")) {
                    throw new UnsupportedOperationException("Protobuf not yet supported");
                  } else {
                      throw new UnsupportedOperationException("Unsupported file extension: <" + f.toString() + ">");
                  }
              }catch (IOException e) {
                  throw new RuntimeException(e);
              }
          });
      } catch (IOException e) {
          throw new RuntimeException(e);
      }
      getLogger().lifecycle("Java code for LionWeb Languages generated");
  }
}
