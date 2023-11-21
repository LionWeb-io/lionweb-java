package io.lionweb.lioncore.java.serialization.data;

import io.lionweb.lioncore.java.language.Language;
import java.util.Objects;
import javax.annotation.Nonnull;

/**
 * The pair Language Key and Language Version identify a specific version of a language. This pair
 * is defined UsedLanguage (see https://github.com/LionWeb-io/specification/issues/129).
 */
public class UsedLanguage {
  private String key;
  private String version;

  public UsedLanguage() {}

  public UsedLanguage(String key, String version) {
    this.key = key;
    this.version = version;
  }

  public static UsedLanguage fromLanguage(@Nonnull Language language) {
    Objects.requireNonNull(language, "Language parameter should not be null");
    Objects.requireNonNull(language.getVersion(), "Language version should not be null");
    return new UsedLanguage(language.getKey(), language.getVersion());
  }

  public static UsedLanguage fromMetaPointer(@Nonnull MetaPointer metaPointer) {
    Objects.requireNonNull(metaPointer, "metaPointer parameter should not be null");
    Objects.requireNonNull(metaPointer.getLanguage(), "metaPointer language should not be null");
    Objects.requireNonNull(metaPointer.getVersion(), "metaPointer version should not be null");
    return new UsedLanguage(metaPointer.getLanguage(), metaPointer.getVersion());
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

  public void setVersion(String version) {
    this.version = version;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof UsedLanguage)) return false;
    UsedLanguage that = (UsedLanguage) o;
    return Objects.equals(key, that.key) && Objects.equals(version, that.version);
  }

  @Override
  public int hashCode() {
    return Objects.hash(key, version);
  }

  @Override
  public String toString() {
    return "UsedLanguage{" + "key='" + key + '\'' + ", version='" + version + '\'' + '}';
  }
}
