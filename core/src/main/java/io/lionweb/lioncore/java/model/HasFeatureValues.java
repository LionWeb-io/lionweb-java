package io.lionweb.lioncore.java.model;

import io.lionweb.lioncore.java.language.Containment;
import io.lionweb.lioncore.java.language.Property;
import io.lionweb.lioncore.java.language.Reference;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface HasFeatureValues {
  /**
   * Get the property value associated with the specified property.
   *
   * <p>Should this return a String?
   */
  Object getPropertyValue(Property property);

  /** If the value is not compatible with the type of the property, the exception */
  void setPropertyValue(Property property, Object value);

  /** This return all the Nodes directly contained into this Node. */
  List<? extends Node> getChildren();

  /**
   * This return all the Nodes directly contained into this Node under the specific Containment
   * relation specified.
   */
  List<? extends Node> getChildren(Containment containment);

  /**
   * Add a child to the specified list of children associated with the given Containment relation.
   * If the specified Containment does not allow for multiple values, and if a value is already set
   * than the exception IllegalStateException will be thrown.
   *
   * <p>If the child has not a Concept compatible with the target of the Containement, the exception
   * IllegalArgumentException will be thrown.
   */
  void addChild(Containment containment, Node child);

  /**
   * Remove the given child from the list of children associated with the Node, making it a dangling
   * Node. If the specified Node is not currently a child of this Node the exception
   * IllegalArgumentException will be thrown.
   */
  void removeChild(Node node);

  /**
   * Remove the child at the given index, considering the children under the given containment.
   *
   * <p>If there is no match the exception IllegalArgumentException will be thrown.
   */
  void removeChild(@Nonnull Containment containment, int index);

  @Nonnull
  List<ReferenceValue> getReferenceValues();

  /**
   * Return the Nodes referred to under any Reference link. This returns an empty list if no Node is
   * referred by this Node.
   *
   * <p>The Node returned is guaranteed to be either part of this Node's Model or of Models imported
   * by this Node's Model.
   *
   * <p>Please note that this will not return null values, differently from the variant taking a
   * Reference. It may contain duplicates.
   */
  @Nonnull
  List<Node> getReferredNodes();

  @Nonnull
  List<ReferenceValue> getReferenceValues(@Nonnull Reference reference);

  /**
   * Return the Nodes referred to under the specified Reference link. This returns an empty list if
   * no Node is associated with the specified Reference link.
   *
   * <p>The Node returned is guaranteed to be either part of this Node's Model or of Models imported
   * by this Node's Model.
   *
   * <p>Please note that it may contain null values in case of ReferenceValue with a null referred
   * field.
   */
  @Nonnull
  List<Node> getReferredNodes(@Nonnull Reference reference);

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
   */
  void addReferenceValue(@Nonnull Reference reference, @Nullable ReferenceValue referredNode);

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

  void setOnlyReferenceValue(@Nonnull Reference reference, @Nullable ReferenceValue value);

  void setOnlyReferenceValueByName(String referenceName, @Nullable ReferenceValue value);

  void setReferenceValues(
      @Nonnull Reference reference, @Nonnull List<? extends ReferenceValue> values);

  void setReferenceValuesByName(
      String referenceName, @Nonnull List<? extends ReferenceValue> values);
}
