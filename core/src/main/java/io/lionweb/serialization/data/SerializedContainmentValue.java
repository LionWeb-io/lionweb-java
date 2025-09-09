package io.lionweb.serialization.data;

import java.util.*;
import javax.annotation.Nonnull;

/** This represents the serialization of the values of a containment link in a Node. */
public class SerializedContainmentValue {
  private final MetaPointer metaPointer;
  private final List<String> value;

  public SerializedContainmentValue(MetaPointer metaPointer, List<String> value) {
    this.metaPointer = metaPointer;
    this.value = new ArrayList<>(value);
  }

  public SerializedContainmentValue(MetaPointer metaPointer, String... values) {
    this.metaPointer = metaPointer;
    this.value = Arrays.asList(values);
  }

  public MetaPointer getMetaPointer() {
    return metaPointer;
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

  /**
   * Removes a childId from the list of Node-IDs contained.
   *
   * @param childId the identifier of the childId to be removed; must not be null
   * @return true if the childId was successfully removed, false otherwise
   * @throws NullPointerException if the childId is null
   */
  public boolean removeChild(@Nonnull String childId) {
    Objects.requireNonNull(childId, "childId must not be null");
    return value.remove(childId);
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
