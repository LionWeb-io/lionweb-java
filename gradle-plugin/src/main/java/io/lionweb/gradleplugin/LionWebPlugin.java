package io.lionweb.gradleplugin;

import io.lionweb.gradleplugin.tasks.GenerateLanguageTask;
import io.lionweb.gradleplugin.tasks.GenerateNodeClassesTask;
import java.io.File;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.bundling.Jar;

/**
 * The `LionWebPlugin` is a custom Gradle plugin for projects that utilize LionWeb for language
 * engineering. It provides configurations, sensible defaults, and tasks for working with LionWeb
 * language definitions and generating Java code representations of them.
 *
 * <p>The plugin performs the following responsibilities:
 *
 * <p>1. **Extension Registration**: - Registers an extension named `lionweb`. - Allows users to
 * customize configurations such as: - Directory for LionWeb language definitions
 * (`languagesDirectory`). - Directory for generated Java files (`generationDirectory`). - Target
 * package name for generated Java classes (`packageName`). - Provides defaults if configurations
 * are not explicitly defined: - `languagesDirectory` defaults to `src/main/lionweb` inside the
 * project directory. - `generationDirectory` defaults to `build/generated-lionweb` inside the build
 * directory.
 *
 * <p>2. **Task Registration**: - Adds a Gradle task named `generateLWLanguages`. - The task
 * leverages the `GenerateLanguageTask` for processing LionWeb language files. - Configures the task
 * with lazy connections from extension properties (`languagesDirectory`, `generationDirectory`, and
 * `packageName`) to task inputs. - This task can generate Java code for LionWeb language
 * definitions, streamlining the language engineering process.
 *
 * <p>The `LionWebPlugin` organizes the setup and execution of the LionWeb-based language
 * engineering pipeline, making it easier to integrate with Gradle build systems.
 */
public class LionWebPlugin implements Plugin<Project> {
  @Override
  public void apply(Project project) {
    project.getLogger().info("Applying LionWeb plugin");
    // Create extension and set sensible defaults (conventions)
    LionWebPluginExtension ext =
        project.getExtensions().create("lionweb", LionWebPluginExtension.class);
    if (!ext.getLanguagesDirectory().isPresent()) {
      ext.getLanguagesDirectory()
          .set(new java.io.File(project.getProjectDir(), "src/main/lionweb"));
    }
    if (!ext.getGenerationDirectory().isPresent()) {
      ext.getGenerationDirectory()
          .set(
              new java.io.File(
                  project.getLayout().getBuildDirectory().get().getAsFile(), "generated-lionweb"));
    }

    // We modify the Jar task to add the LionWeb languages in the Jar
    project
        .getTasks()
        .withType(Jar.class)
        .configureEach(
            jar -> {
              File lionwebDir = ext.getLanguagesDirectory().get().getAsFile();
              if (lionwebDir.exists()) {
                if (lionwebDir.exists()) {
                  jar.from(lionwebDir, copySpec -> copySpec.into("META-INF/lionweb"));
                }
              }
            });

    // Register task and map extension -> task inputs (lazy wiring)
    project
        .getTasks()
        .register(
            "generateLWLanguages",
            GenerateLanguageTask.class,
            task -> {
              if (ext.getDefaultPackageName().isPresent()) {
                task.getDefaultPackageName().set(ext.getDefaultPackageName());
              }
              if (ext.getLanguagesDirectory().get().getAsFile().exists()) {
                task.getLanguagesDirectory().set(ext.getLanguagesDirectory());
              }
              task.getGenerationDirectory().set(ext.getGenerationDirectory());
              task.getLanguagesSpecificPackages().set(ext.getLanguagesSpecificPackages());
              task.getLanguagesClassNames().set(ext.getLanguagesClassNames());
              task.getLanguagesToGenerate().set(ext.getLanguagesToGenerate());
              task.getLanguagesToGenerate().set(ext.getLanguagesToGenerate());
              task.setGroup("lionweb");
              task.setDescription("Generates LionWeb languages");
            });
    project
        .getTasks()
        .register(
            "generateLWNodeClasses",
            GenerateNodeClassesTask.class,
            task -> {
              if (ext.getDefaultPackageName().isPresent()) {
                task.getDefaultPackageName().set(ext.getDefaultPackageName());
              }
              if (ext.getLanguagesDirectory().get().getAsFile().exists()) {
                task.getLanguagesDirectory().set(ext.getLanguagesDirectory());
              }
              task.getGenerationDirectory().set(ext.getGenerationDirectory());
              task.getPrimitiveTypes().set(ext.getPrimitiveTypes());
              task.getLanguagesSpecificPackages().set(ext.getLanguagesSpecificPackages());
              task.getLanguagesClassNames().set(ext.getLanguagesClassNames());
              task.getLanguagesToGenerate().set(ext.getLanguagesToGenerate());
              task.getMappings().set(ext.getMappings());
              task.setGroup("lionweb");
              task.setDescription("Generates LionWeb node classes");
            });
  }
}
