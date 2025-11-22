package io.lionweb.gradleplugin;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Optional;

// TODO add possibility to override Class names for languages
public abstract class LionWebPluginExtension {

  public abstract Property<String> getPackageName();

  public abstract DirectoryProperty getLanguagesDirectory();

  public abstract DirectoryProperty getGenerationDirectory();

  public abstract MapProperty<String, String> getPrimitiveTypes();

    public abstract MapProperty<String, String> getLanguagesSpecificPackages();
}
