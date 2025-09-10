package io.lionweb.serialization.data;

import io.lionweb.language.Language;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * The pair Language Key and Language Version identify a specific version of a language. This pair
 * is defined as UsedLanguage in the specifications (see https://github.com/LionWeb-io/specification/issues/129).
 * In this implementation, the pair has a more general name as UsedLanguage is just one role or use we can attribute
 * to the pair language key and version.
 */
public class LanguageVersion {
  private @Nullable String key;
  private @Nullable String version;

  // Interning support: canonical instances per (key, version) pair.
  private static final ConcurrentMap<String, LanguageVersion> INTERN = new ConcurrentHashMap<>();

  private static String makeInternKey(@Nullable String key, @Nullable String version) {
    return (key == null ? "\u0000" : key) + "\u0000" + (version == null ? "\u0000" : version);
  }

  public static LanguageVersion of(@Nullable String key, @Nullable String version) {
    final String internKey = makeInternKey(key, version);
    return INTERN.computeIfAbsent(internKey, k -> new LanguageVersion(key, version));
  }

  // Returns the canonical instance equivalent to the provided UsedLanguage.
  public static LanguageVersion intern(@Nonnull LanguageVersion languageVersion) {
    Objects.requireNonNull(languageVersion, "usedLanguage should not be null");
    return of(languageVersion.key, languageVersion.version);
  }

  private LanguageVersion(@Nullable String key, @Nullable String version) {
    this.key = key;
    this.version = version;
  }

  public static LanguageVersion fromLanguage(@Nonnull Language language) {
    Objects.requireNonNull(language, "Language parameter should not be null");
    Objects.requireNonNull(language.getVersion(), "Language version should not be null");
      Objects.requireNonNull(language.getKey(), "Language key should not be null");
    return of(language.getKey(), language.getVersion());
  }

  public static LanguageVersion fromMetaPointer(@Nonnull MetaPointer metaPointer) {
    Objects.requireNonNull(metaPointer, "metaPointer parameter should not be null");
    Objects.requireNonNull(metaPointer.getLanguage(), "metaPointer language should not be null");
    Objects.requireNonNull(metaPointer.getVersion(), "metaPointer version should not be null");
    return of(metaPointer.getLanguage(), metaPointer.getVersion());
  }

  public @Nullable String getKey() {
    return key;
  }

  public @Nullable String getVersion() {
    return version;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    // Instances are canonicalized via interning, so reference equality suffices
    return this == o;
  }

  @Override
  public int hashCode() {
    // Must be consistent with equals' reference semantics
    return System.identityHashCode(this);
  }

  @Override
  public String toString() {
    return "UsedLanguage{" + "key='" + key + '\'' + ", version='" + version + '\'' + '}';
  }
}
