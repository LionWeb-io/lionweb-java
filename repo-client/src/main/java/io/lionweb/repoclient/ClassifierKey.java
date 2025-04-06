package io.lionweb.repoclient;

import java.util.Objects;

public class ClassifierKey {
  private final String languageKey;
  private final String classifierKey;

  public ClassifierKey(String languageKey, String classifierKey) {
    this.languageKey = languageKey;
    this.classifierKey = classifierKey;
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
    return Objects.hash(languageKey, classifierKey);
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
