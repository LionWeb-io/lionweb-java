package io.lionweb.lioncore.java.serialization.data;

import io.lionweb.lioncore.java.language.Language;
import java.util.Objects;
import javax.annotation.Nonnull;

/** The pair Language Key and Metamodel Version identify a specific version of a metamodel. */
public class LanguageKeyVersion {
  private String key;
  private String version;

  public LanguageKeyVersion() {}

  public LanguageKeyVersion(String key, String version) {
    this.key = key;
    this.version = version;
  }

  public static LanguageKeyVersion fromLanguage(@Nonnull Language language) {
    Objects.requireNonNull(language, "Language parameter should not be null");
    Objects.requireNonNull(language.getVersion(), "Language version should not be null");
    return new LanguageKeyVersion(language.getKey(), language.getVersion());
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
    if (!(o instanceof LanguageKeyVersion)) return false;
    LanguageKeyVersion that = (LanguageKeyVersion) o;
    return Objects.equals(key, that.key) && Objects.equals(version, that.version);
  }

  @Override
  public int hashCode() {
    return Objects.hash(key, version);
  }

  @Override
  public String toString() {
    return "LanguageKeyVersion{" + "key='" + key + '\'' + ", version='" + version + '\'' + '}';
  }
}
