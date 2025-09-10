package io.lionweb.serialization.data;

import io.lionweb.language.Feature;
import io.lionweb.language.IKeyed;
import io.lionweb.language.Language;
import io.lionweb.language.LanguageEntity;
import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A MetaPointer is the combination of the pair Language and Version with a Key, which identify one
 * element within that language.
 *
 * <p>We should never have multiple instances with the same value, so equality and identity
 * coincides for this class.
 */
public class MetaPointer {
  private static final Map<String, Map<String, WeakHashMap<String, MetaPointer>>>
      INSTANCES_BY_KEY_LANGUAGE_VERSION = new HashMap<>();

  /** Provide a MetaPointer with the given value, avoid allocations if unnecessary. */
  public static MetaPointer get(String language, String version, String key) {
    return INSTANCES_BY_KEY_LANGUAGE_VERSION
        .computeIfAbsent(key, k -> new HashMap<>())
        .computeIfAbsent(language, l -> new WeakHashMap<>())
        .computeIfAbsent(version, v -> new MetaPointer(language, version, key));
  }

  private final @Nullable String key;
  private final @Nullable String version;
  private final @Nullable String language;

  private MetaPointer(@Nullable String language, @Nullable String version, @Nullable String key) {
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

  public LanguageVersion getLanguageVersion() {
    return LanguageVersion.of(language, version);
  }

  @Override
  public boolean equals(Object o) {
    return this == o;
  }

  @Override
  public int hashCode() {
    return System.identityHashCode(this);
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

  /**
   * Retrieves the associated {@link LanguageVersion} instance for the current {@link MetaPointer}.
   *
   * @return a {@link LanguageVersion} instance representing the language and version associated
   *     with this {@link MetaPointer}
   */
  public @Nonnull LanguageVersion getUsedLanguage() {
    return LanguageVersion.fromMetaPointer(this);
  }
}
