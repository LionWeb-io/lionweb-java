package io.lionweb.serialization.data;

import io.lionweb.language.Feature;
import io.lionweb.language.IKeyed;
import io.lionweb.language.Language;
import io.lionweb.language.LanguageEntity;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A MetaPointer is the combination of the pair Language and Version with a Key, which identify one
 * element within that language.
 *
 * <p>We should never have multiple instances with the same value, so equality and identity coincide
 * for this class.
 */
public final class MetaPointer {
  private static final String NULL = "\u0000"; // sentinel for null since CHM forbids null keys

  private static String norm(@Nullable String s) {
    return s == null ? NULL : s;
  }

  private static final ConcurrentMap<
          String, // key
          ConcurrentMap<
              String, // language
              ConcurrentMap<
                  String, // version
                  MetaPointer>>>
      INSTANCES_BY_KEY_LANGUAGE_VERSION = new ConcurrentHashMap<>();

  /** Provide a MetaPointer with the given value, avoid allocations if unnecessary. */
  public static MetaPointer get(
      @Nullable String language, @Nullable String version, @Nullable String key) {
    final String kKey = norm(key);
    final String kLang = norm(language);
    final String kVer = norm(version);

    // 1st level: by key
    ConcurrentMap<String, ConcurrentMap<String, MetaPointer>> byLanguage =
        INSTANCES_BY_KEY_LANGUAGE_VERSION.computeIfAbsent(kKey, __ -> new ConcurrentHashMap<>());

    // 2nd level: by language
    ConcurrentMap<String, MetaPointer> byVersion =
        byLanguage.computeIfAbsent(kLang, __ -> new ConcurrentHashMap<>());

    // 3rd level: by version → canonical MetaPointer
    return byVersion.computeIfAbsent(kVer, __ -> new MetaPointer(language, version, key));
  }

  // Note that these three values are nullable solely because of fault-tolerance. Semantically they
  // should not be null, but when writing code we should expect these to be potentially null, in
  // case we are
  // representing an incorrect state
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
