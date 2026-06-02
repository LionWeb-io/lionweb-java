package io.lionweb.client.api;

import java.util.Objects;

/**
 * Immutable key object used to uniquely identify a classifier in the context of a specific
 * language.
 */
public final class ClassifierKey {
  private final String languageKey;
  private final String classifierKey;
  private final int hashCode;

  public ClassifierKey(String languageKey, String classifierKey) {
    this.languageKey = languageKey;
    this.classifierKey = classifierKey;
    // This is equivalent to Objects.hash
    this.hashCode =
        31 * ((languageKey == null ? 0 : languageKey.hashCode()) + 31)
            + (classifierKey == null ? 0 : classifierKey.hashCode());
  }

  public String getLanguageKey() {
    return languageKey;
  }

  public String getClassifierKey() {
    return classifierKey;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ClassifierKey)) return false;
    ClassifierKey that = (ClassifierKey) o;
    return Objects.equals(languageKey, that.languageKey)
        && Objects.equals(classifierKey, that.classifierKey);
  }

  @Override
  public int hashCode() {
    return hashCode;
  }

  @Override
  public String toString() {
    return "ClassifierKey{"
        + "languageKey='"
        + languageKey
        + '\''
        + ", classifierKey='"
        + classifierKey
        + '\''
        + '}';
  }
}
