package io.lionweb.client.api;

import java.util.Objects;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

public class ClassifierResult {
  private final @NotNull Set<String> ids;
  private final int size;

  public ClassifierResult(@NotNull Set<String> ids, int size) {
    Objects.requireNonNull(ids, "ids must not be null");
    if (size < 0) throw new IllegalArgumentException("size must not be negative");
    this.ids = ids;
    this.size = size;
  }

  public @NotNull Set<String> getIds() {
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
