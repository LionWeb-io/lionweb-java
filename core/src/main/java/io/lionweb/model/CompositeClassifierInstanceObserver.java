package io.lionweb.model;

import io.lionweb.language.Containment;
import io.lionweb.language.Property;
import io.lionweb.language.Reference;
import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CompositeClassifierInstanceObserver implements ClassifierInstanceObserver {
  private static final int INTERN_LIMIT = 3;
  private static final Map<Set<ClassifierInstanceObserver>, ClassifierInstanceObserver>
      INTERN_CACHE = new WeakHashMap<>();

  private final Set<ClassifierInstanceObserver> elements;

  private CompositeClassifierInstanceObserver(Set<ClassifierInstanceObserver> elements) {
    this.elements = elements;
  }

  public static ClassifierInstanceObserver combine(
      ClassifierInstanceObserver a, ClassifierInstanceObserver b) {
    Set<ClassifierInstanceObserver> combined = new HashSet<>();

    // flatten left
    if (a instanceof CompositeClassifierInstanceObserver) {
      combined.addAll(((CompositeClassifierInstanceObserver) a).elements);
    } else {
      combined.add(a);
    }

    // flatten right
    if (b instanceof CompositeClassifierInstanceObserver) {
      combined.addAll(((CompositeClassifierInstanceObserver) b).elements);
    } else {
      combined.add(b);
    }

    return getInstance(combined);
  }

  private static ClassifierInstanceObserver getInstance(Set<ClassifierInstanceObserver> elements) {
    // intern if small, but don’t intern larger ones
    if (elements.size() <= INTERN_LIMIT) {
      Set<ClassifierInstanceObserver> key = Collections.unmodifiableSet(new HashSet<>(elements));
      return INTERN_CACHE.computeIfAbsent(
          key,
          k -> k.size() == 1 ? k.iterator().next() : new CompositeClassifierInstanceObserver(k));
    } else {
      return new CompositeClassifierInstanceObserver(Collections.unmodifiableSet(elements));
    }
  }

  public ClassifierInstanceObserver remove(ClassifierInstanceObserver observer) {
    if (!this.elements.contains(observer)) {
      throw new IllegalArgumentException("Cannot remove unknown observer: " + observer);
    }
    Set<ClassifierInstanceObserver> newElements = new HashSet<>(this.elements);
    newElements.remove(observer);
    return getInstance(newElements);
  }

  public void addElement(ClassifierInstanceObserver observer) {
    elements.add(observer);
  }

  public void removeElement(ClassifierInstanceObserver observer) {
    elements.remove(observer);
  }

  public Set<ClassifierInstanceObserver> getElements() {
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
