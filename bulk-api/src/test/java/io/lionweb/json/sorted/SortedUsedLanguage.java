package io.lionweb.json.sorted;

import io.lionweb.lioncore.java.serialization.data.UsedLanguage;

import java.util.Comparator;
import java.util.Objects;

/**
 * {@link Comparable } view of {@link SortedUsedLanguage#delegate }, based on first {@link SortedUsedLanguage#getKey() }, then {@link SortedUsedLanguage#getVersion() } (in that order).
 */
public class SortedUsedLanguage extends UsedLanguage implements Comparable<UsedLanguage> {
  private final UsedLanguage delegate;

  public SortedUsedLanguage(UsedLanguage delegate) {
    this.delegate = delegate;
  }

  @Override
  public int compareTo(UsedLanguage other) {
    int key = Objects.compare(this.getKey(), other.getKey(), Comparator.nullsLast(Comparator.naturalOrder()));
    if (key != 0) {
      return key;
    }
    return Objects.compare(this.getVersion(), other.getVersion(), Comparator.nullsLast(Comparator.naturalOrder()));
  }

  @Override
  public String getKey() {
    return delegate.getKey();
  }
  @Override
  public void setKey(String key) {
    delegate.setKey(key);
  }
  @Override
  public String getVersion() {
    return delegate.getVersion();
  }
  @Override
  public void setVersion(String version) {
    delegate.setVersion(version);
  }
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof UsedLanguage)) return false;
    UsedLanguage that = (UsedLanguage) o;
    return Objects.equals(getKey(), that.getKey()) && Objects.equals(getVersion(), that.getVersion());
  }
  @Override
  public int hashCode() {
    return delegate.hashCode();
  }
  @Override
  public String toString() {
    return delegate.toString();
  }
}
