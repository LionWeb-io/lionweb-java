package io.lionweb.model;

import io.lionweb.language.Containment;
import io.lionweb.language.Property;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MockNodeObserver implements NodeObserver {

  public abstract static class Record {
    public final @Nonnull Node node;

    public Record(@Nonnull Node node) {
      this.node = node;
    }
  }

  public static class PropertyChangedRecord extends Record {
    public final @Nonnull Property property;
    public final @Nullable Object oldValue;
    public final @Nullable Object newValue;

    public PropertyChangedRecord(
        @Nonnull Node node,
        @Nonnull Property property,
        @Nullable Object oldValue,
        @Nullable Object newValue) {
      super(node);
      this.property = property;
      this.oldValue = oldValue;
      this.newValue = newValue;
    }

    @Override
    public boolean equals(Object o) {
      if (o == null || getClass() != o.getClass()) return false;
      PropertyChangedRecord that = (PropertyChangedRecord) o;
      return Objects.equals(property, that.property)
          && Objects.equals(oldValue, that.oldValue)
          && Objects.equals(newValue, that.newValue);
    }

    @Override
    public int hashCode() {
      return Objects.hash(property, oldValue, newValue);
    }
  }

  public static class ChildAddedRecord extends Record {
    public final @Nonnull Containment containment;
    public final int index;
    public final @Nonnull Node newChild;

    public ChildAddedRecord(
        @Nonnull Node node, @Nonnull Containment containment, int index, @Nonnull Node newChild) {
      super(node);
      this.containment = containment;
      this.index = index;
      this.newChild = newChild;
    }

    @Override
    public boolean equals(Object o) {
      if (o == null || getClass() != o.getClass()) return false;
      ChildAddedRecord that = (ChildAddedRecord) o;
      return index == that.index
          && Objects.equals(containment, that.containment)
          && Objects.equals(newChild, that.newChild);
    }

    @Override
    public int hashCode() {
      return Objects.hash(containment, index, newChild);
    }

    @Override
    public String toString() {
      return "ChildAddedRecord{"
          + "containment="
          + containment
          + ", index="
          + index
          + ", newChild="
          + newChild
          + '}';
    }
  }

  public static class ChildRemovedRecord extends Record {
    public final @Nonnull Containment containment;
    public final int index;
    public final @Nonnull Node newChild;

    public ChildRemovedRecord(
        @Nonnull Node node, @Nonnull Containment containment, int index, @Nonnull Node newChild) {
      super(node);
      this.containment = containment;
      this.index = index;
      this.newChild = newChild;
    }

    @Override
    public boolean equals(Object o) {
      if (o == null || getClass() != o.getClass()) return false;
      ChildRemovedRecord that = (ChildRemovedRecord) o;
      return index == that.index
          && Objects.equals(containment, that.containment)
          && Objects.equals(newChild, that.newChild);
    }

    @Override
    public int hashCode() {
      return Objects.hash(containment, index, newChild);
    }
  }

  public static class AnnotationAddedRecord extends Record {
    public final int index;
    public final @Nonnull AnnotationInstance newAnnotation;

    public AnnotationAddedRecord(
        @Nonnull Node node, int index, @Nonnull AnnotationInstance newAnnotation) {
      super(node);
      this.index = index;
      this.newAnnotation = newAnnotation;
    }

    @Override
    public boolean equals(Object o) {
      if (o == null || getClass() != o.getClass()) return false;
      AnnotationAddedRecord that = (AnnotationAddedRecord) o;
      return index == that.index && Objects.equals(newAnnotation, that.newAnnotation);
    }

    @Override
    public int hashCode() {
      return Objects.hash(index, newAnnotation);
    }

    @Override
    public String toString() {
      return "AnnotationAddedRecord{" + "index=" + index + ", newAnnotation=" + newAnnotation + '}';
    }
  }

  private List<Record> records = new ArrayList<>();

  public void clearRecords() {
    records.clear();
  }

  public List<Record> getRecords() {
    return records;
  }

  @Override
  public void propertyChanged(
      @Nonnull Node node,
      @Nonnull Property property,
      @Nullable Object oldValue,
      @Nullable Object newValue) {
    records.add(new PropertyChangedRecord(node, property, oldValue, newValue));
  }

  @Override
  public void childAdded(
      @Nonnull Node node, @Nonnull Containment containment, int index, @Nonnull Node newChild) {
    records.add(new ChildAddedRecord(node, containment, index, newChild));
  }

  @Override
  public void childRemoved(
      @Nonnull Node node, @Nonnull Containment containment, int index, @Nonnull Node removedChild) {
    records.add(new ChildRemovedRecord(node, containment, index, removedChild));
  }

  @Override
  public void annotationAdded(
      @Nonnull Node node, int index, @Nonnull AnnotationInstance newAnnotation) {
    records.add(new AnnotationAddedRecord(node, index, newAnnotation));
  }

  @Override
  public void annotationRemoved(@Nonnull Node node) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void referenceValueAdded(@Nonnull Node node) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void referenceValueChanged(@Nonnull Node node) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void referenceValueRemoved(@Nonnull Node node) {
    throw new UnsupportedOperationException();
  }
}
