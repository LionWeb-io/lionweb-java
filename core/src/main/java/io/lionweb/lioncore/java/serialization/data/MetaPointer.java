package io.lionweb.lioncore.java.serialization.data;

import io.lionweb.lioncore.java.language.Feature;
import io.lionweb.lioncore.java.language.IKeyed;
import io.lionweb.lioncore.java.language.Language;
import io.lionweb.lioncore.java.language.LanguageEntity;
import java.util.Objects;

/**
 * A MetaPointer is the combination of the pair Language and Version with a Key, which identify one
 * element within that language.
 */
public class MetaPointer {
  private String key;
  private String version;
  private String language;

  public MetaPointer(String language, String version, String key) {
    this.key = key;
    this.version = version;
    this.language = language;
  }

  public MetaPointer() {}

  public static MetaPointer from(Feature<?, ?> feature) {
    return from(feature, feature.getDeclaringLanguage());
  }

  public static MetaPointer from(LanguageEntity<?, ?> languageEntity) {
    MetaPointer metaPointer = new MetaPointer();
    metaPointer.setKey(languageEntity.getKey());
    if (languageEntity.getLanguage() != null) {
      metaPointer.setLanguage(languageEntity.getLanguage().getKey());
      if (languageEntity.getLanguage().getVersion() != null) {
        metaPointer.setVersion(languageEntity.getLanguage().getVersion());
      }
    }
    return metaPointer;
  }

  public static MetaPointer from(IKeyed<?> elementWithKey, Language language) {
    MetaPointer metaPointer = new MetaPointer();
    metaPointer.setKey(elementWithKey.getKey());
    if (language != null) {
      metaPointer.setLanguage(language.getKey());
      if (language.getVersion() != null) {
        metaPointer.setVersion(language.getVersion());
      }
    }
    return metaPointer;
  }

  public String getLanguage() {
    return language;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getVersion() {
    return version;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof MetaPointer)) return false;
    MetaPointer that = (MetaPointer) o;
    return Objects.equals(key, that.key)
        && Objects.equals(version, that.version)
        && Objects.equals(language, that.language);
  }

  @Override
  public int hashCode() {
    return Objects.hash(key, version, language);
  }

  public void setVersion(String version) {
    this.version = version;
  }

  @Override
  public String toString() {
    return "MetaPointer{"
        + "key='"
        + key
        + '\''
        + ", version='"
        + version
        + '\''
        + ", language='"
        + language
        + '\''
        + '}';
  }
}
