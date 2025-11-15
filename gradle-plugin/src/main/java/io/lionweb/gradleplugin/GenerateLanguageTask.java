package io.lionweb.gradleplugin;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.*;

import org.gradle.api.DefaultTask;

public abstract class GenerateLanguageTask extends DefaultTask {
    @Input
    abstract RegularFileProperty getLanguagesDirectory();
    @Input
    abstract RegularFileProperty getGenerationDirectory();

    @TaskAction
    public void run() {
        getLogger().lifecycle("Java code for LionWeb Languages generated");
    }
}
