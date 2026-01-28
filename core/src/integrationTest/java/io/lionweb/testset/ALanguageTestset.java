package io.lionweb.testset;

import io.lionweb.LionWebVersion;
import io.lionweb.language.Language;
import io.lionweb.serialization.JsonSerialization;
import io.lionweb.serialization.SerializationProvider;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;

public abstract class ALanguageTestset extends ATestset {
  protected Language language;

  public ALanguageTestset(Path path) {
    super(path);
  }

  @BeforeEach
  public void loadLanguage() {
    this.language =
        loadLanguage(
            findIntegrationTests()
                // .resolve("testset")
                .resolve("withLanguage")
                .resolve("myLang.language.json"));
  }

  protected JsonSerialization getSerialization() {
    JsonSerialization result =
        SerializationProvider.getStandardJsonSerialization(LionWebVersion.v2023_1);
    result.registerLanguage(this.language);
    result.enableDynamicNodes();
    return result;
  }
}
