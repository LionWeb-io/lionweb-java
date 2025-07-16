package io.lionweb.serialization.data;

import io.lionweb.language.Feature;
import io.lionweb.language.IKeyed;
import io.lionweb.language.Language;
import io.lionweb.language.LanguageEntity;
import java.util.*;

/**
 * A MetaPointer is the combination of the pair Language and Version with a Key, which identify one
 * element within that language.
 */
public class MetaPointer {
  private static Map<String, MetaPointer> INSTANCES = new HashMap<>();

  public static MetaPointer get(String language, String version, String key) {
    String hashKey = language + ":" + version + ":" + key;
    if (!INSTANCES.containsKey(hashKey)) {
      INSTANCES.put(hashKey, new MetaPointer(language, version, key));
    }
    return INSTANCES.get(hashKey);
  }

  private String key;
  private String version;
  private String language;

  private MetaPointer(String language, String version, String key) {
    this.key = key;
    this.version = version;
    this.language = language;
  }

  public static MetaPointer from(Feature<?> feature) {
    return from(feature, feature.getDeclaringLanguage());
  }

  public static MetaPointer from(LanguageEntity<?> languageEntity) {
    return MetaPointer.get(
        languageEntity.getLanguage().getKey(),
        languageEntity.getLanguage().getVersion(),
        languageEntity.getKey());
  }

  public static MetaPointer from(IKeyed<?> elementWithKey, Language language) {

    return MetaPointer.get(
        language == null ? null : language.getKey(),
        language == null ? null : language.getVersion(),
        elementWithKey.getKey());
  }

  public String getLanguage() {
    return language;
  }

  public String getKey() {
    return key;
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
