package io.lionweb.serialization.data;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** This represents the serialization of the values of a reference link in a Node. */
public class SerializedReferenceValue {

  public static class Entry {
    private String resolveInfo;
    private String reference;

    public Entry() {}

    public Entry(@Nullable String reference, @Nullable String resolveInfo) {
      this.resolveInfo = resolveInfo;
      this.reference = reference;
    }

    public @Nullable String getResolveInfo() {
      return resolveInfo;
    }

    public void setResolveInfo(@Nullable String resolveInfo) {
      this.resolveInfo = resolveInfo;
    }

    public @Nullable String getReference() {
      return reference;
    }

    public void setReference(@Nullable String reference) {
      this.reference = reference;
    }

    @Override
    public String toString() {
      return "Entry{"
          + "resolveInfo='"
          + resolveInfo
          + '\''
          + ", reference='"
          + reference
          + '\''
          + '}';
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof Entry)) return false;
      Entry entry = (Entry) o;
      return Objects.equals(resolveInfo, entry.resolveInfo)
          && Objects.equals(reference, entry.reference);
    }

    @Override
    public int hashCode() {
      return Objects.hash(resolveInfo, reference);
    }
  }

  @SerializedName("reference")
  private final MetaPointer metaPointer;

  @SerializedName("targets")
  private final List<Entry> value;

  public SerializedReferenceValue(@Nullable MetaPointer metaPointer) {
    this.metaPointer = metaPointer;
    value = new ArrayList<>(1);
  }

  public SerializedReferenceValue(@Nullable MetaPointer metaPointer, @Nonnull List<Entry> value) {
    Objects.requireNonNull(value, "value cannot be null");
    this.metaPointer = metaPointer;
    this.value = new ArrayList<>(value);
  }

  public @Nullable MetaPointer getMetaPointer() {
    return metaPointer;
  }

  public @Nonnull List<Entry> getValue() {
    return Collections.unmodifiableList(value);
  }

  public void setValue(@Nonnull List<Entry> value) {
    Objects.requireNonNull(value, "value cannot be null");
    this.value.clear();
    this.value.addAll(value);
  }

  public void addValue(@Nonnull Entry value) {
    Objects.requireNonNull(value, "value cannot be null");
    this.value.add(value);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof SerializedReferenceValue)) return false;
    SerializedReferenceValue that = (SerializedReferenceValue) o;
    return Objects.equals(metaPointer, that.metaPointer) && Objects.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(metaPointer, value);
  }

  @Override
  public String toString() {
    return "SerializedReferenceValue{" + "metaPointer=" + metaPointer + ", value=" + value + '}';
  }
}
