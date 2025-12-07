package io.lionweb.gradleplugin;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;

public abstract class LionWebPluginExtension {

  public abstract Property<String> getDefaultPackageName();

  public abstract DirectoryProperty getLanguagesDirectory();

  public abstract DirectoryProperty getGenerationDirectory();

  public abstract MapProperty<String, String> getPrimitiveTypes();

  public abstract MapProperty<String, String> getLanguagesSpecificPackages();

  public abstract MapProperty<String, String> getLanguagesClassNames();
}
