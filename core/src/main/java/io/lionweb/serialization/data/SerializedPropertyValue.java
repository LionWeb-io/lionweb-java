package io.lionweb.serialization.data;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This represents the serialization of the value of a property in a Node. This class is immutable
 * and multiple coinciding value share the same identity, but this is not guaranteed for all
 * instances.
 */
public class SerializedPropertyValue {
  private static final int THRESHOLD = 128;
  private static final Map<MetaPointer, Map<String, SerializedPropertyValue>>
      INSTANCES_BY_METAPOINTER = new ConcurrentHashMap<>();

  /** This will avoid most unnecessary duplicate instantiations, but this is not guaranteed. */
  public static SerializedPropertyValue get(MetaPointer metaPointer, String value) {
    // Large values are expected to be more rarely reused, therefore we do not prevent their
    // duplication.
    // We are interested in preventing the duplication of very common values like "false", "true",
    // "0", "1", etc.
    if (value != null && value.length() >= THRESHOLD) {
      return new SerializedPropertyValue(metaPointer, value);
    }
    Map<String, SerializedPropertyValue> valuesForMetaPointer =
        INSTANCES_BY_METAPOINTER.computeIfAbsent(
            metaPointer, k -> Collections.synchronizedMap(new WeakHashMap<>()));
    return valuesForMetaPointer.computeIfAbsent(
        value, v -> new SerializedPropertyValue(metaPointer, v));
  }

  private final MetaPointer metaPointer;
  private final String value;

  private SerializedPropertyValue(MetaPointer metaPointer, String value) {
    this.metaPointer = metaPointer;
    this.value = value != null ? value.intern() : null;
  }

  public MetaPointer getMetaPointer() {
    return metaPointer;
  }

  public String getValue() {
    return value;
  }

  @Override
  public String toString() {
    return "SerializedPropertyValue{"
        + "metaPointer="
        + metaPointer
        + ", value='"
        + value
        + '\''
        + '}';
  }

  @Override
  public boolean equals(Object o) {
    // Note that Identity will correspond to Equality for common values but not for all values,
    // so we need to inspect the actual values
    if (this == o) return true;
    if (!(o instanceof SerializedPropertyValue)) return false;
    SerializedPropertyValue that = (SerializedPropertyValue) o;
    return Objects.equals(metaPointer, that.metaPointer) && Objects.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    // Note that Identity will correspond to Equality for common values but not for all values,
    // therefore we cannot use System.identityHashCode()
    return Objects.hash(metaPointer, value);
  }
}
