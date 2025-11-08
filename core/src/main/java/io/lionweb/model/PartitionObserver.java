package io.lionweb.model;

import io.lionweb.language.Containment;
import io.lionweb.language.Property;
import io.lionweb.language.Reference;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * The PartitionObserver interface provides methods to observe changes within a partition, including
 * property changes, child modifications, and alterations to annotations and references.
 */
public interface PartitionObserver {
  /**
   * This method is invoked to notify that a property of a {@link ClassifierInstance} has changed.
   *
   * @param classifierInstance the {@link ClassifierInstance} containing the property that changed.
   *     Cannot be null.
   * @param property the {@link Property} that has been changed. Cannot be null.
   * @param oldValue the old value of the property before the change. Can be null if the property
   *     previously had no value or if the property does not support a defined value.
   * @param newValue the new value of the property after the change. Can be null if the property was
   *     set to no value or does not support a defined value.
   */
  void propertyChanged(
      @Nonnull ClassifierInstance<?> classifierInstance,
      @Nonnull Property property,
      @Nullable Object oldValue,
      @Nullable Object newValue);

  /**
   * Called to notify that a child node has been added to a {@link ClassifierInstance}.
   *
   * @param classifierInstance the {@link ClassifierInstance} to which the child node has been
   *     added. Cannot be null.
   * @param containment the {@link Containment} that describes the containment relationship of the
   *     new child node. Cannot be null.
   * @param index the index at which the new child node has been added within the parent's children.
   * @param newChild the {@link Node} representing the newly added child node. Cannot be null.
   */
  void childAdded(
      @Nonnull ClassifierInstance<?> classifierInstance,
      @Nonnull Containment containment,
      int index,
      @Nonnull Node newChild);

  /**
   * Notifies that a child node has been removed from a {@link ClassifierInstance}.
   *
   * @param classifierInstance the {@link ClassifierInstance} from which the child node has been
   *     removed. Cannot be null.
   * @param containment the {@link Containment} describing the containment relationship of the
   *     removed child node. Cannot be null.
   * @param index the index from which the child node was removed within the parent's children.
   * @param removedChild the {@link Node} representing the removed child node. Cannot be null.
   */
  void childRemoved(
      @Nonnull ClassifierInstance<?> classifierInstance,
      @Nonnull Containment containment,
      int index,
      @Nonnull Node removedChild);

  /**
   * Notifies that a new annotation has been added to the specified {@link ClassifierInstance}.
   *
   * @param node the {@link ClassifierInstance} to which the annotation has been added. Cannot be
   *     null.
   * @param index the index within the list of annotations at which the new annotation has been
   *     added.
   * @param newAnnotation the {@link AnnotationInstance} representing the added annotation. Cannot
   *     be null.
   */
  void annotationAdded(
      @Nonnull ClassifierInstance<?> node, int index, @Nonnull AnnotationInstance newAnnotation);

  /**
   * Notifies that an annotation has been removed from a {@link ClassifierInstance}.
   *
   * @param node the {@link ClassifierInstance} from which the annotation was removed. Cannot be
   *     null.
   * @param index the index within the list of annotations from which the annotation was removed.
   * @param removedAnnotation the {@link AnnotationInstance} representing the removed annotation.
   *     Cannot be null.
   */
  void annotationRemoved(
      @Nonnull ClassifierInstance<?> node,
      int index,
      @Nonnull AnnotationInstance removedAnnotation);

  /**
   * Notifies that a new {@link ReferenceValue} has been added to a {@link ClassifierInstance}.
   *
   * @param classifierInstance the {@link ClassifierInstance} to which the {@link ReferenceValue}
   *     has been added. Cannot be null.
   * @param reference the {@link Reference} associated with the added {@link ReferenceValue}. Cannot
   *     be null.
   * @param referenceValue the {@link ReferenceValue} that has been added. Cannot be null.
   */
  void referenceValueAdded(
      @Nonnull ClassifierInstance<?> classifierInstance,
      @Nonnull Reference reference,
      int index,
      @Nonnull ReferenceValue referenceValue);

  /**
   * This method is invoked to notify that a {@link ReferenceValue} within a {@link
   * ClassifierInstance} has changed.
   *
   * @param classifierInstance the {@link ClassifierInstance} containing the {@link ReferenceValue}
   *     that changed. Cannot be null.
   * @param reference the {@link Reference} associated with the {@link ReferenceValue} that changed.
   *     Cannot be null.
   * @param index the index within the list of reference values where the change occurred.
   * @param oldReferred the previous referred value of the {@link ReferenceValue}, or null if none
   *     existed.
   * @param oldResolveInfo the previous resolution information for the referenced value, or null if
   *     none existed.
   * @param newReferred the updated referred value of the {@link ReferenceValue}, or null if no new
   *     value is provided.
   * @param newResolveInfo the updated resolution information for the referenced value, or null if
   *     no new information is provided.
   */
  void referenceValueChanged(
      @Nonnull ClassifierInstance<?> classifierInstance,
      @Nonnull Reference reference,
      int index,
      @Nullable String oldReferred,
      @Nullable String oldResolveInfo,
      @Nullable String newReferred,
      @Nullable String newResolveInfo);

  /**
   * Notifies that a {@link ReferenceValue} has been removed from a {@link ClassifierInstance}.
   *
   * @param classifierInstance the {@link ClassifierInstance} from which the {@link ReferenceValue}
   *     has been removed. Cannot be null.
   * @param reference the {@link Reference} associated with the removed {@link ReferenceValue}.
   *     Cannot be null.
   * @param index the index within the list of reference values where the removal occurred.
   * @param referenceValue the {@link ReferenceValue} that has been removed. Cannot be null.
   */
  void referenceValueRemoved(
      @Nonnull ClassifierInstance<?> classifierInstance,
      @Nonnull Reference reference,
      int index,
      @Nonnull ReferenceValue referenceValue);
}
