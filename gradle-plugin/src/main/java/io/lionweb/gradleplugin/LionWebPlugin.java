package io.lionweb.gradleplugin;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class LionWebPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        // Create extension and set sensible defaults (conventions)
        LionWebPluginExtension ext = project.getExtensions().create("lionweb", LionWebPluginExtension.class);
        // TODO configure

        // Register task and map extension -> task inputs (lazy wiring)
        project.getTasks().register("generateLWLanguages", GenerateLanguageTask.class, task -> {
            task.setGroup("lionweb");
            task.setDescription("Generates LionWeb languages");
        });
    }
}
