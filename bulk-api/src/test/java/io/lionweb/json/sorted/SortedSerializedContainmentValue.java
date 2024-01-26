package io.lionweb.json.sorted;

import io.lionweb.lioncore.java.serialization.data.MetaPointer;
import io.lionweb.lioncore.java.serialization.data.SerializedContainmentValue;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * {@link Comparable } view of {@link SortedSerializedContainmentValue#delegate } with sorted {@link SortedSerializedContainmentValue#getValue() }s.
 * 
 * Comparison based on its {@link SortedSerializedContainmentValue#getMetaPointer() }.
 */
public class SortedSerializedContainmentValue extends SerializedContainmentValue implements Comparable<SerializedContainmentValue> {
  private final SerializedContainmentValue delegate;

  public SortedSerializedContainmentValue(SerializedContainmentValue delegate) {
    super();
    this.delegate = delegate;
  }

  @Override
  public List<String> getValue() {
    return delegate.getValue().stream().sorted().collect(Collectors.<String>toList());
  }

  @Override
  public int compareTo(SerializedContainmentValue other) {
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
  public void setValue(List<String> value) {
    delegate.setValue(value);
  }
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof SerializedContainmentValue)) return false;
    SerializedContainmentValue that = (SerializedContainmentValue) o;
    return Objects.equals(getMetaPointer(), that.getMetaPointer()) && Objects.equals(getValue(), that.getValue());
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
