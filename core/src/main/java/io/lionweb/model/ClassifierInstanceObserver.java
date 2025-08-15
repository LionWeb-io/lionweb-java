package io.lionweb.model;

import io.lionweb.language.Containment;
import io.lionweb.language.Property;
import io.lionweb.language.Reference;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface ClassifierInstanceObserver {
  void propertyChanged(
      @Nonnull ClassifierInstance<?> classifierInstance,
      @Nonnull Property property,
      @Nullable Object oldValue,
      @Nullable Object newValue);

  void childAdded(
      @Nonnull ClassifierInstance<?> classifierInstance,
      @Nonnull Containment containment,
      int index,
      @Nonnull Node newChild);

  void childRemoved(
      @Nonnull ClassifierInstance<?> classifierInstance,
      @Nonnull Containment containment,
      int index,
      @Nonnull Node removedChild);

  void annotationAdded(@Nonnull Node node, int index, @Nonnull AnnotationInstance newAnnotation);

  void annotationRemoved(
      @Nonnull Node node, int index, @Nonnull AnnotationInstance removedAnnotation);

  void referenceValueAdded(
      @Nonnull ClassifierInstance<?> classifierInstance,
      @Nonnull Reference reference,
      @Nonnull ReferenceValue referenceValue);

  void referenceValueChanged(@Nonnull ClassifierInstance<?> classifierInstance,
                             @Nonnull Reference reference, int index, String oldReferred, String oldResolveInfo,
                             String newReferred, String newResolveInfo);

  void referenceValueRemoved(@Nonnull ClassifierInstance<?> classifierInstance);
}
