package io.lionweb.lioncore.java.serialization.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** This represents the serialization of the values of a reference link in a Node. */
public class SerializedReferenceValue {

  public static class Entry {
    private String resolveInfo;
    private String reference;

    public Entry() {}

    public Entry(String reference, String resolveInfo) {
      this.resolveInfo = resolveInfo;
      this.reference = reference;
    }

    public String getResolveInfo() {
      return resolveInfo;
    }

    public void setResolveInfo(String resolveInfo) {
      this.resolveInfo = resolveInfo;
    }

    public String getReference() {
      return reference;
    }

    public void setReference(String reference) {
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

  private MetaPointer metaPointer;
  private final List<Entry> value;

  public SerializedReferenceValue() {
    value = new ArrayList<>();
  }

  public SerializedReferenceValue(MetaPointer metaPointer, List<Entry> value) {
    this.metaPointer = metaPointer;
    this.value = new ArrayList<>(value);
  }

  public MetaPointer getMetaPointer() {
    return metaPointer;
  }

  public void setMetaPointer(MetaPointer metaPointer) {
    this.metaPointer = metaPointer;
  }

  public List<Entry> getValue() {
    return Collections.unmodifiableList(value);
  }

  public void setValue(List<Entry> value) {
    this.value.clear();
    this.value.addAll(value);
  }

  public void addValue(Entry value) {
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
