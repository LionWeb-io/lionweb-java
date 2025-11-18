package io.lionweb.gradleplugin.generators;

import io.lionweb.language.Language;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import org.jetbrains.annotations.NotNull;

public class NodeClassesJavaCodeGenerator extends AbstractJavaCodeGenerator {
  /**
   * Constructs a NodeClassesJavaCodeGenerator with a specified destination directory.
   *
   * @param destinationDir the directory where the generated code will be stored; must not be null
   * @throws NullPointerException if the destinationDir is null
   */
  public NodeClassesJavaCodeGenerator(@NotNull File destinationDir) {
    super(destinationDir);
  }

  public void generate(@Nonnull Language language, @Nonnull String packageName) throws IOException {
    generate(
        language,
        packageName,
        new LanguageContext(packageName, Collections.singletonList(language)));
  }

  private void generate(
      @Nonnull Language language,
      @Nonnull String packageName,
      @Nonnull LanguageContext languageContext)
      throws IOException {
    throw new UnsupportedOperationException("Not yet implemented");
  }

  public void generate(@Nonnull List<Language> languages, @Nonnull String packageName)
      throws IOException {
    Objects.requireNonNull(languages, "languages should not be null");
    Objects.requireNonNull(packageName, "packageName should not be null");
    if (languages.isEmpty()) {
      return;
    }
    LanguageContext languageContext = new LanguageContext(packageName, languages);
    languages.forEach(
        language -> {
          try {
            generate(language, packageName, languageContext);
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        });
  }
}
