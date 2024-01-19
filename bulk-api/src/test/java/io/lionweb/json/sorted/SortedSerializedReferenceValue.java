package io.lionweb.json.sorted;

import io.lionweb.lioncore.java.serialization.data.MetaPointer;
import io.lionweb.lioncore.java.serialization.data.SerializedReferenceValue;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * {@link Comparable } view of {@link SortedSerializedReferenceValue#delegate } with sorted {@link SortedSerializedReferenceValue#getValue() }.
 * 
 * Comparison based on its {@link SortedSerializedReferenceValue#getMetaPointer() }.
 */
public class SortedSerializedReferenceValue extends SerializedReferenceValue implements Comparable<SerializedReferenceValue> {
  private final SerializedReferenceValue delegate;

  public SortedSerializedReferenceValue(SerializedReferenceValue delegate) {
    this.delegate = delegate;
  }

  @Override
  public List<SerializedReferenceValue.Entry> getValue() {
    return delegate.getValue().stream().map(new Function<SerializedReferenceValue.Entry, Entry>() {
      public Entry apply(SerializedReferenceValue.Entry it) {
        return new Entry(it);
      }
    }).sorted().collect(Collectors.<SerializedReferenceValue.Entry>toList());
  }

  @Override
  public int compareTo(SerializedReferenceValue other) {
    return new MetaPointerComparator().compare(this.getMetaPointer(), other.getMetaPointer());
  }

  /**
   * {@link Comparable } view of {@link SortedSerializedReferenceValue.Entry#delegate }, based on its {@link SortedSerializedReferenceValue.Entry#getReference() } and then {@link SortedSerializedReferenceValue.Entry#getResolveInfo() } (in that order).
   */
  public static class Entry extends SerializedReferenceValue.Entry implements Comparable<SerializedReferenceValue.Entry> {
    private final SerializedReferenceValue.Entry delegate;

    public Entry(SerializedReferenceValue.Entry delegate) {
      this.delegate = delegate;
    }

    @Override
    public int compareTo(SerializedReferenceValue.Entry other) {
      int reference = Objects.compare(this.getReference(), other.getReference(), Comparator.nullsLast(Comparator.naturalOrder()));
      if (reference != 0) {
        return reference;
      }
      return Objects.compare(this.getResolveInfo(), other.getResolveInfo(), Comparator.nullsLast(Comparator.naturalOrder()));
    }


    @Override
    public String getResolveInfo() {
      return delegate.getResolveInfo();
    }
    @Override
    public void setResolveInfo(String resolveInfo) {
      delegate.setResolveInfo(resolveInfo);
    }
    @Override
    public String getReference() {
      return delegate.getReference();
    }
    @Override
    public void setReference(String reference) {
      delegate.setReference(reference);
    }
    @Override
    public String toString() {
      return delegate.toString();
    }
    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof SerializedReferenceValue.Entry)) return false;
      SerializedReferenceValue.Entry entry = (SerializedReferenceValue.Entry) o;
      return Objects.equals(getResolveInfo(), entry.getResolveInfo())
              && Objects.equals(getReference(), entry.getReference());
    }
    @Override
    public int hashCode() {
      return delegate.hashCode();
    }
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
  public void setValue(List<SerializedReferenceValue.Entry> value) {
    delegate.setValue(value);
  }
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof SerializedReferenceValue)) return false;
    SerializedReferenceValue that = (SerializedReferenceValue) o;
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
