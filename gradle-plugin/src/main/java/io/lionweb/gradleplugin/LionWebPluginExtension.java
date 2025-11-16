package io.lionweb.gradleplugin;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;

public abstract class LionWebPluginExtension {
  public abstract Property<String> getPackageName();

  public abstract DirectoryProperty getLanguagesDirectory();

  public abstract DirectoryProperty getGenerationDirectory();
}
