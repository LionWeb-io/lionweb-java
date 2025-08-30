package io.lionweb.model;

import io.lionweb.language.Containment;
import io.lionweb.language.Property;
import io.lionweb.language.Reference;
import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CompositePartitionObserver implements PartitionObserver {

  private final Set<PartitionObserver> elements;

  private CompositePartitionObserver(Set<PartitionObserver> elements) {
    this.elements = elements;
  }

  public static PartitionObserver combine(PartitionObserver a, PartitionObserver b) {
    Set<PartitionObserver> combined = new HashSet<>();

    // flatten left
    if (a instanceof CompositePartitionObserver) {
      combined.addAll(((CompositePartitionObserver) a).elements);
    } else {
      combined.add(a);
    }

    // flatten right
    if (b instanceof CompositePartitionObserver) {
      combined.addAll(((CompositePartitionObserver) b).elements);
    } else {
      combined.add(b);
    }

    return getInstance(combined);
  }

  private static PartitionObserver getInstance(Set<PartitionObserver> elements) {
    if (elements.size() == 1) {
      return elements.iterator().next();
    }
    return new CompositePartitionObserver(Collections.unmodifiableSet(elements));
  }

  public PartitionObserver remove(PartitionObserver observer) {
    if (!this.elements.contains(observer)) {
      throw new IllegalArgumentException("Cannot remove unknown observer: " + observer);
    }
    Set<PartitionObserver> newElements = new HashSet<>(this.elements);
    newElements.remove(observer);
    return getInstance(newElements);
  }

  public void addElement(PartitionObserver observer) {
    elements.add(observer);
  }

  public void removeElement(PartitionObserver observer) {
    elements.remove(observer);
  }

  public Set<PartitionObserver> getElements() {
    return elements;
  }

  @Override
  public void propertyChanged(
      @Nonnull ClassifierInstance<?> classifierInstance,
      @Nonnull Property property,
      @Nullable Object oldValue,
      @Nullable Object newValue) {
    elements.forEach(e -> e.propertyChanged(classifierInstance, property, oldValue, newValue));
  }

  @Override
  public void childAdded(
      @Nonnull ClassifierInstance<?> classifierInstance,
      @Nonnull Containment containment,
      int index,
      @Nonnull Node newChild) {
    elements.forEach(e -> e.childAdded(classifierInstance, containment, index, newChild));
  }

  @Override
  public void childRemoved(
      @Nonnull ClassifierInstance<?> classifierInstance,
      @Nonnull Containment containment,
      int index,
      @Nonnull Node removedChild) {
    elements.forEach(e -> e.childRemoved(classifierInstance, containment, index, removedChild));
  }

  @Override
  public void annotationAdded(
      @Nonnull ClassifierInstance<?> node, int index, @Nonnull AnnotationInstance newAnnotation) {
    elements.forEach(e -> e.annotationAdded(node, index, newAnnotation));
  }

  @Override
  public void annotationRemoved(
      @Nonnull ClassifierInstance<?> node,
      int index,
      @Nonnull AnnotationInstance removedAnnotation) {
    elements.forEach(e -> e.annotationRemoved(node, index, removedAnnotation));
  }

  @Override
  public void referenceValueAdded(
      @Nonnull ClassifierInstance<?> classifierInstance,
      @Nonnull Reference reference,
      @Nonnull ReferenceValue referenceValue) {
    elements.forEach(e -> e.referenceValueAdded(classifierInstance, reference, referenceValue));
  }

  @Override
  public void referenceValueChanged(
      @Nonnull ClassifierInstance<?> classifierInstance,
      @Nonnull Reference reference,
      int index,
      @Nullable String oldReferred,
      @Nullable String oldResolveInfo,
      @Nullable String newReferred,
      @Nullable String newResolveInfo) {
    elements.forEach(
        e ->
            e.referenceValueChanged(
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
    elements.forEach(
        e -> e.referenceValueRemoved(classifierInstance, reference, index, referenceValue));
  }
}
