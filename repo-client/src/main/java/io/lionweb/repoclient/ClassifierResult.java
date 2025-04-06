package io.lionweb.repoclient;

import java.util.Objects;
import java.util.Set;

public class ClassifierResult {
  private final Set<String> ids;
  private final int size;

  public ClassifierResult(Set<String> ids, int size) {
    this.ids = ids;
    this.size = size;
  }

  public Set<String> getIds() {
    return ids;
  }

  public int getSize() {
    return size;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ClassifierResult)) return false;
    ClassifierResult that = (ClassifierResult) o;
    return size == that.size && Objects.equals(ids, that.ids);
  }

  @Override
  public int hashCode() {
    return Objects.hash(ids, size);
  }

  @Override
  public String toString() {
    return "ClassifierResult{" + "ids=" + ids + ", size=" + size + '}';
  }
}
