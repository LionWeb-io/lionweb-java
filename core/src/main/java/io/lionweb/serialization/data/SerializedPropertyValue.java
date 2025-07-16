package io.lionweb.serialization.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/** This represents the serialization of the value of a property in a Node. */
public class SerializedPropertyValue {
  private static final int THRESHOLD = 128;
  private static final Map<String, SerializedPropertyValue> INSTANCES = new HashMap<>();
  private static final Map<MetaPointer, SerializedPropertyValue> NULL_INSTANCES = new HashMap<>();

  public static SerializedPropertyValue get(MetaPointer metaPointer, String value) {
    if (value == null) {
      if (!NULL_INSTANCES.containsKey(metaPointer)) {
        NULL_INSTANCES.put(metaPointer, new SerializedPropertyValue(metaPointer, null));
      }
      return NULL_INSTANCES.get(metaPointer);
    } else if (value.length() < THRESHOLD) {
      String key =
          metaPointer.getLanguage()
              + ":"
              + metaPointer.getVersion()
              + ":"
              + metaPointer.getKey()
              + ":"
              + value;
      if (!INSTANCES.containsKey(key)) {
        INSTANCES.put(key, new SerializedPropertyValue(metaPointer, value));
      }
      return INSTANCES.get(key);
    } else {
      return new SerializedPropertyValue(metaPointer, value);
    }
  }

  private final MetaPointer metaPointer;
  private final String value;

  private SerializedPropertyValue(MetaPointer metaPointer, String value) {
    this.metaPointer = metaPointer;
    this.value = value;
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
    if (this == o) return true;
    if (!(o instanceof SerializedPropertyValue)) return false;
    SerializedPropertyValue that = (SerializedPropertyValue) o;
    return Objects.equals(metaPointer, that.metaPointer) && Objects.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(metaPointer, value);
  }
}
