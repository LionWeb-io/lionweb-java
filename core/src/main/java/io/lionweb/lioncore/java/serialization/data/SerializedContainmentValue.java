package io.lionweb.lioncore.java.serialization.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** This represents the serialization of the values of a containment link in a Node. */
public class SerializedContainmentValue {
  private MetaPointer metaPointer;
  private final List<String> value;

  public SerializedContainmentValue() {
    this.value = new ArrayList<>();
  }

  public SerializedContainmentValue(MetaPointer metaPointer, List<String> value) {
    this.metaPointer = metaPointer;
    this.value = new ArrayList<>(value);
  }

  public MetaPointer getMetaPointer() {
    return metaPointer;
  }

  public void setMetaPointer(MetaPointer metaPointer) {
    this.metaPointer = metaPointer;
  }

  /** This returns the list of Node-IDs contained. */
  public List<String> getValue() {
    return Collections.unmodifiableList(value);
  }

  /** This expects the list of Node-IDs contained. */
  public void setValue(List<String> value) {
    this.value.clear();
    this.value.addAll(value);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof SerializedContainmentValue)) return false;
    SerializedContainmentValue that = (SerializedContainmentValue) o;
    return Objects.equals(metaPointer, that.metaPointer) && Objects.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(metaPointer, value);
  }

  @Override
  public String toString() {
    return "SerializedContainmentValue{" + "metaPointer=" + metaPointer + ", value=" + value + '}';
  }
}
