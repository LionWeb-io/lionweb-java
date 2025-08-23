package io.lionweb.model;

import io.lionweb.language.Containment;
import io.lionweb.language.Property;
import io.lionweb.language.Reference;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MockPartitionObserver implements PartitionObserver {

  public abstract static class Record {
    public final @Nonnull ClassifierInstance<?> node;

    public Record(@Nonnull ClassifierInstance<?> node) {
      this.node = node;
    }
  }

  public static class PropertyChangedRecord extends Record {
    public final @Nonnull Property property;
    public final @Nullable Object oldValue;
    public final @Nullable Object newValue;

    public PropertyChangedRecord(
        @Nonnull ClassifierInstance<?> node,
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
        @Nonnull ClassifierInstance<?> node,
        @Nonnull Containment containment,
        int index,
        @Nonnull Node newChild) {
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
    public final @Nonnull Node removedChild;

    public ChildRemovedRecord(
        @Nonnull ClassifierInstance<?> node,
        @Nonnull Containment containment,
        int index,
        @Nonnull Node removedChild) {
      super(node);
      this.containment = containment;
      this.index = index;
      this.removedChild = removedChild;
    }

    @Override
    public boolean equals(Object o) {
      if (o == null || getClass() != o.getClass()) return false;
      ChildRemovedRecord that = (ChildRemovedRecord) o;
      return index == that.index
          && Objects.equals(containment, that.containment)
          && Objects.equals(removedChild, that.removedChild);
    }

    @Override
    public int hashCode() {
      return Objects.hash(containment, index, removedChild);
    }
  }

  public static class AnnotationAddedRecord extends Record {
    public final int index;
    public final @Nonnull AnnotationInstance newAnnotation;

    public AnnotationAddedRecord(
        @Nonnull ClassifierInstance<?> node, int index, @Nonnull AnnotationInstance newAnnotation) {
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

  public static class AnnotationRemovedRecord extends Record {
    public final int index;
    public final @Nonnull AnnotationInstance removedAnnotation;

    public AnnotationRemovedRecord(
        @Nonnull ClassifierInstance<?> node,
        int index,
        @Nonnull AnnotationInstance removedAnnotation) {
      super(node);
      this.index = index;
      this.removedAnnotation = removedAnnotation;
    }

    @Override
    public boolean equals(Object o) {
      if (o == null || getClass() != o.getClass()) return false;
      AnnotationRemovedRecord that = (AnnotationRemovedRecord) o;
      return index == that.index && Objects.equals(removedAnnotation, that.removedAnnotation);
    }

    @Override
    public int hashCode() {
      return Objects.hash(index, removedAnnotation);
    }
  }

  public static class ReferenceAddedRecord extends Record {
    public final @Nonnull Reference reference;
    public final @Nonnull ReferenceValue referenceValue;

    public ReferenceAddedRecord(
        @Nonnull ClassifierInstance<?> node,
        @Nonnull Reference reference,
        @Nonnull ReferenceValue referenceValue) {
      super(node);
      this.reference = reference;
      this.referenceValue = referenceValue;
    }

    @Override
    public boolean equals(Object o) {
      if (o == null || getClass() != o.getClass()) return false;
      ReferenceAddedRecord that = (ReferenceAddedRecord) o;
      return Objects.equals(reference, that.reference)
          && Objects.equals(referenceValue, that.referenceValue);
    }

    @Override
    public int hashCode() {
      return Objects.hash(reference, referenceValue);
    }
  }

  public static class ReferenceChangedRecord extends Record {
    public final @Nonnull Reference reference;
    public final int index;
    public final String oldReferred;
    public final String oldResolveInfo;
    public final String newReferred;
    public final String newResolveInfo;

    public ReferenceChangedRecord(
        @Nonnull ClassifierInstance<?> node,
        @Nonnull Reference reference,
        int index,
        String oldReferred,
        String oldResolveInfo,
        String newReferred,
        String newResolveInfo) {
      super(node);
      this.reference = reference;
      this.index = index;
      this.oldReferred = oldReferred;
      this.oldResolveInfo = oldResolveInfo;
      this.newReferred = newReferred;
      this.newResolveInfo = newResolveInfo;
    }

    @Override
    public boolean equals(Object o) {
      if (o == null || getClass() != o.getClass()) return false;
      ReferenceChangedRecord that = (ReferenceChangedRecord) o;
      return index == that.index
          && Objects.equals(reference, that.reference)
          && Objects.equals(oldReferred, that.oldReferred)
          && Objects.equals(oldResolveInfo, that.oldResolveInfo)
          && Objects.equals(newReferred, that.newReferred)
          && Objects.equals(newResolveInfo, that.newResolveInfo);
    }

    @Override
    public int hashCode() {
      return Objects.hash(
          reference, index, oldReferred, oldResolveInfo, newReferred, newResolveInfo);
    }

    @Override
    public String toString() {
      return "ReferenceChangedRecord{"
          + "reference="
          + reference
          + ", index="
          + index
          + ", oldReferred='"
          + oldReferred
          + '\''
          + ", oldResolveInfo='"
          + oldResolveInfo
          + '\''
          + ", newReferred='"
          + newReferred
          + '\''
          + ", newResolveInfo='"
          + newResolveInfo
          + '\''
          + '}';
    }
  }

  public static class ReferenceRemovedRecord extends Record {
    public final @Nonnull Reference reference;
    public final int index;
    public final String referred;
    public final String resolveInfo;

    public ReferenceRemovedRecord(
        @Nonnull ClassifierInstance<?> node,
        @Nonnull Reference reference,
        int index,
        String referred,
        String resolveInfo) {
      super(node);
      this.reference = reference;
      this.index = index;
      this.referred = referred;
      this.resolveInfo = resolveInfo;
    }

    @Override
    public boolean equals(Object o) {
      if (o == null || getClass() != o.getClass()) return false;
      ReferenceRemovedRecord that = (ReferenceRemovedRecord) o;
      return index == that.index
          && Objects.equals(reference, that.reference)
          && Objects.equals(referred, that.referred)
          && Objects.equals(resolveInfo, that.resolveInfo);
    }

    @Override
    public int hashCode() {
      return Objects.hash(reference, index, referred, resolveInfo);
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
      @Nonnull ClassifierInstance<?> node,
      @Nonnull Property property,
      @Nullable Object oldValue,
      @Nullable Object newValue) {
    records.add(new PropertyChangedRecord(node, property, oldValue, newValue));
  }

  @Override
  public void childAdded(
      @Nonnull ClassifierInstance<?> node,
      @Nonnull Containment containment,
      int index,
      @Nonnull Node newChild) {
    records.add(new ChildAddedRecord(node, containment, index, newChild));
  }

  @Override
  public void childRemoved(
      @Nonnull ClassifierInstance<?> node,
      @Nonnull Containment containment,
      int index,
      @Nonnull Node removedChild) {
    records.add(new ChildRemovedRecord(node, containment, index, removedChild));
  }

  @Override
  public void annotationAdded(
      @Nonnull ClassifierInstance<?> node, int index, @Nonnull AnnotationInstance newAnnotation) {
    records.add(new AnnotationAddedRecord(node, index, newAnnotation));
  }

  @Override
  public void annotationRemoved(
      @Nonnull ClassifierInstance<?> node,
      int index,
      @Nonnull AnnotationInstance removedAnnotation) {
    records.add(new AnnotationRemovedRecord(node, index, removedAnnotation));
  }

  @Override
  public void referenceValueAdded(
      @Nonnull ClassifierInstance<?> node,
      @Nonnull Reference reference,
      @Nonnull ReferenceValue referenceValue) {
    records.add(new ReferenceAddedRecord(node, reference, referenceValue));
  }

  @Override
  public void referenceValueChanged(
      @Nonnull ClassifierInstance<?> classifierInstance,
      @Nonnull Reference reference,
      int index,
      String oldReferred,
      String oldResolveInfo,
      String newReferred,
      String newResolveInfo) {
    records.add(
        new ReferenceChangedRecord(
            classifierInstance,
            reference,
            index,
            oldReferred,
            oldResolveInfo,
            newReferred,
            newResolveInfo));
  }

  @Override
  public void referenceValueRemoved(
      @Nonnull ClassifierInstance<?> classifierInstance,
      @Nonnull Reference reference,
      int index,
      @Nonnull ReferenceValue referenceValue) {
    records.add(
        new ReferenceRemovedRecord(
            classifierInstance,
            reference,
            index,
            referenceValue.getReferredID(),
            referenceValue.getResolveInfo()));
  }
}
