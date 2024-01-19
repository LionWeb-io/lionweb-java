package io.lionweb.json.sorted;

import io.lionweb.lioncore.java.serialization.data.MetaPointer;
import io.lionweb.lioncore.java.serialization.data.SerializedPropertyValue;

import java.util.Objects;

/**
 * {@link Comparable } view of {@link SortedSerializedPropertyValue#delegate }, compared by its {@link SortedSerializedPropertyValue#getMetaPointer() }.
 */
public class SortedSerializedPropertyValue extends SerializedPropertyValue implements Comparable<SerializedPropertyValue> {
  private final SerializedPropertyValue delegate;

  public SortedSerializedPropertyValue(SerializedPropertyValue delegate) {
    this.delegate = delegate;
  }

  @Override
  public int compareTo(SerializedPropertyValue other) {
    return new MetaPointerComparator().compare(this.getMetaPointer(), other.getMetaPointer());
  }

  @Override
  public MetaPointer getMetaPointer() {
    return delegate.getMetaPointer();
  }
  @Override
  public void setMetaPointer(MetaPointer metaPointer) {
    delegate.setMetaPointer(metaPointer);
  }
  @Override
  public String getValue() {
    return delegate.getValue();
  }
  @Override
  public void setValue(String value) {
    delegate.setValue(value);
  }
  @Override
  public String toString() {
    return delegate.toString();
  }
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof SerializedPropertyValue)) return false;
    SerializedPropertyValue that = (SerializedPropertyValue) o;
    return Objects.equals(getMetaPointer(), that.getMetaPointer()) && Objects.equals(getValue(), that.getValue());
  }
  @Override
  public int hashCode() {
    return delegate.hashCode();
  }
}
