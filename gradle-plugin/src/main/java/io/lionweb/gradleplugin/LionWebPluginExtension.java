package io.lionweb.gradleplugin;

import org.gradle.api.file.RegularFileProperty;

public abstract class LionWebPluginExtension {
    abstract RegularFileProperty getLanguagesDirectory();
    abstract RegularFileProperty getGenerationDirectory();
}
