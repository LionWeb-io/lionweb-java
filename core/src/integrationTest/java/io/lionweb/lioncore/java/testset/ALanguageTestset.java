package io.lionweb.lioncore.java.testset;

import io.lionweb.lioncore.java.language.Language;
import io.lionweb.lioncore.java.serialization.JsonSerialization;
import io.lionweb.lioncore.java.serialization.SerializationProvider;
import java.nio.file.Path;
import org.junit.Before;

public abstract class ALanguageTestset extends ATestset {
  protected Language language;

  public ALanguageTestset(Path path) {
    super(path);
  }

  @Before
  public void loadLanguage() {
    this.language =
        loadLanguage(
            findIntegrationTests()
                // .resolve("testset")
                .resolve("withLanguage")
                .resolve("myLang.language.json"));
  }

  protected JsonSerialization getSerialization() {
    JsonSerialization result = SerializationProvider.getStandardJsonSerialization();
    result.registerLanguage(this.language);
    result.enableDynamicNodes();
    return result;
  }
}
