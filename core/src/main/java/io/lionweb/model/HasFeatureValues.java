package io.lionweb.model;

import io.lionweb.language.Containment;
import io.lionweb.language.Property;
import io.lionweb.language.Reference;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface HasFeatureValues {
  /** Get the property value associated with the specified property. */
  @Nullable
  Object getPropertyValue(@Nonnull Property property);

  /**
   * @throws IllegalArgumentException If the value is not compatible with the type of the property
   */
  void setPropertyValue(@Nonnull Property property, @Nullable Object value)
      throws IllegalArgumentException;

  /**
   * This return all the Nodes directly contained into this Node under the specific Containment
   * relation specified.
   */
  @Nonnull
  List<? extends Node> getChildren(@Nonnull Containment containment);

  /**
   * Add a child to the specified list of children associated with the given Containment relation.
   * If the specified Containment does not allow for multiple values, and if a value is already set
   * than the exception IllegalStateException will be thrown.
   *
   * <p>If the child has not a Concept compatible with the target of the Containement, the exception
   * IllegalArgumentException will be thrown.
   */
  void addChild(@Nonnull Containment containment, @Nonnull Node child);

  /**
   * Remove the given child from the list of children associated with the Node, making it a dangling
   * Node. If the specified Node is not currently a child of this Node the exception
   * IllegalArgumentException will be thrown.
   */
  void removeChild(@Nonnull Node node);

  /**
   * Remove the child at the given index, considering the children under the given containment.
   *
   * <p>If there is no match the exception IllegalArgumentException will be thrown.
   */
  void removeChild(@Nonnull Containment containment, int index);

  @Nonnull
  List<ReferenceValue> getReferenceValues(@Nonnull Reference reference);

  /**
   * Add the Node to the list of Nodes referred to from this Node under the given Reference.
   *
   * <p>If the Reference is not multiple, any previous value will be replaced.
   *
   * <p>The Node specified should be either part of this Node's Model or of Models imported by this
   * Node's Model. If that is not the case the exception IllegalArgumentException will be thrown.
   *
   * <p>If the referredNode has not a Concept compatible with the target of the Reference, the
   * exception IllegalArgumentException will be thrown.
   *
   * @return the index of the added ReferenceValue or -1 if the ReferenceValue is not added
   *     (typically because it is null)
   */
  int addReferenceValue(@Nonnull Reference reference, @Nullable ReferenceValue referredNode);

  /**
   * Remove the first reference value that is equal to the given referenceValue. Node. If there is
   * no match the exception IllegalArgumentException will be thrown.
   */
  void removeReferenceValue(@Nonnull Reference reference, @Nullable ReferenceValue referenceValue);

  /**
   * Remove the reference value at the given index, considering the reference values under the given
   * reference.
   *
   * <p>If there is no match the exception IllegalArgumentException will be thrown.
   */
  void removeReferenceValue(@Nonnull Reference reference, int index);

  void setReferenceValues(
      @Nonnull Reference reference, @Nonnull List<? extends ReferenceValue> values);

  void setReferred(@Nonnull Reference reference, int index, @Nullable Node referredNode);

  void setResolveInfo(@Nonnull Reference reference, int index, @Nullable String resolveInfo);
}
