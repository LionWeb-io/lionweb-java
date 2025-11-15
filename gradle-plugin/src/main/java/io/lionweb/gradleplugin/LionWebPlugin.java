package io.lionweb.gradleplugin;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class LionWebPlugin implements Plugin<Project> {
  @Override
  public void apply(Project project) {
      project.getLogger().info("Applying LionWeb plugin");
    // Create extension and set sensible defaults (conventions)
    LionWebPluginExtension ext =
        project.getExtensions().create("lionweb", LionWebPluginExtension.class);
    if (!ext.getLanguagesDirectory().isPresent()) {
        ext.getLanguagesDirectory().set(new java.io.File(project.getProjectDir(), "src/main/lionweb"));
    }
    if (!ext.getGenerationDirectory().isPresent()) {
        ext.getGenerationDirectory().set(new java.io.File(project.getLayout().getBuildDirectory().get().getAsFile(), "generated-lionweb"));
    }

    // Register task and map extension -> task inputs (lazy wiring)
    project
        .getTasks()
        .register(
            "generateLWLanguages",
            GenerateLanguageTask.class,
            task -> {
                task.getPackageName().set(ext.getPackageName());
                task.getLanguagesDirectory().set(ext.getLanguagesDirectory());
                task.getGenerationDirectory().set(ext.getGenerationDirectory());
              task.setGroup("lionweb");
              task.setDescription("Generates LionWeb languages");
            });
  }
}
