package io.lionweb.lioncore.java.model;

import io.lionweb.lioncore.java.language.Classifier;
import io.lionweb.lioncore.java.language.Property;
import io.lionweb.lioncore.java.language.Reference;
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ClassifierInstanceUtils {

  // Public methods about properties

  @Nullable
  public static Object getPropertyValueByName(
      @Nonnull ClassifierInstance<?> _this, @Nonnull String propertyName) {
    Objects.requireNonNull(_this, "_this should not be null");
    Objects.requireNonNull(propertyName, "propertyName should not be null");
    Property property = _this.getClassifier().getPropertyByName(propertyName);
    if (property == null) {
      throw new IllegalArgumentException(
          "Concept "
              + _this.getClassifier().qualifiedName()
              + " does not contained a property named "
              + propertyName);
    }
    return _this.getPropertyValue(property);
  }

  public static void setPropertyValueByName(
      @Nonnull ClassifierInstance<?> _this, @Nonnull String propertyName, @Nullable Object value) {
    Objects.requireNonNull(_this, "_this should not be null");
    Objects.requireNonNull(propertyName, "propertyName should not be null");
    Classifier<?> classifier = _this.getClassifier();
    if (classifier == null) {
      throw new IllegalStateException(
          "Classifier should not be null for "
              + _this
              + " (class "
              + _this.getClass().getCanonicalName()
              + ")");
    }
    Property property = classifier.getPropertyByName(propertyName);
    if (property == null) {
      throw new IllegalArgumentException(
          "Concept "
              + _this.getClassifier().qualifiedName()
              + " does not contained a property named "
              + propertyName);
    }
    _this.setPropertyValue(property, value);
  }

  @Nullable
  public static Object getPropertyValueByID(
      @Nonnull ClassifierInstance<?> _this, @Nonnull String propertyID) {
    Objects.requireNonNull(_this, "_this should not be null");
    Objects.requireNonNull(propertyID, "propertyID should not be null");
    Property property = _this.getClassifier().getPropertyByID(propertyID);
    return _this.getPropertyValue(property);
  }

  // Public methods about containments

  /** This return all the Nodes directly contained into this Node. */
  @Nonnull
  public static List<Node> getChildren(@Nonnull ClassifierInstance<?> _this) {
    Objects.requireNonNull(_this, "_this should not be null");
    List<Node> allChildren = new LinkedList<>();
    _this.getClassifier().allContainments().stream()
        .map(c -> _this.getChildren(c))
        .forEach(children -> allChildren.addAll(children));
    return allChildren;
  }

  // Public methods about references

  @Nonnull
  public static List<ReferenceValue> getReferenceValues(@Nonnull ClassifierInstance<?> _this) {
    Objects.requireNonNull(_this, "_this should not be null");
    List<ReferenceValue> allReferredValues = new LinkedList<>();
    _this.getClassifier().allReferences().stream()
        .map(r -> _this.getReferenceValues(r))
        .forEach(referenceValues -> allReferredValues.addAll(referenceValues));
    return allReferredValues;
  }

  public static void setOnlyReferenceValue(
      @Nonnull ClassifierInstance<?> _this,
      @Nonnull Reference reference,
      @Nullable ReferenceValue value) {
    Objects.requireNonNull(_this, "_this should not be null");
    if (value == null) {
      _this.setReferenceValues(reference, Collections.emptyList());
    } else {
      _this.setReferenceValues(reference, Arrays.asList(value));
    }
  }

  public static void setOnlyReferenceValueByName(
      @Nonnull ClassifierInstance<?> _this,
      @Nonnull String referenceName,
      @Nullable ReferenceValue value) {
    Objects.requireNonNull(_this, "_this should not be null");
    Objects.requireNonNull(referenceName, "referenceName should not be null");
    Reference reference = _this.getClassifier().requireReferenceByName(referenceName);
    setOnlyReferenceValue(_this, reference, value);
  }

  public static void setReferenceValuesByName(
      @Nonnull ClassifierInstance<?> _this,
      @Nonnull String referenceName,
      @Nonnull List<? extends ReferenceValue> values) {
    Objects.requireNonNull(_this, "_this should not be null");
    Objects.requireNonNull(referenceName, "referenceName should not be null");
    Reference reference = _this.getClassifier().requireReferenceByName(referenceName);
    _this.setReferenceValues(reference, values);
  }

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
  public static List<Node> getReferredNodes(
      @Nonnull ClassifierInstance<?> _this, @Nonnull Reference reference) {
    Objects.requireNonNull(_this, "_this should not be null");
    Objects.requireNonNull(reference, "reference should not be null");
    return _this.getReferenceValues(reference).stream()
        .map(v -> v.getReferred())
        .collect(Collectors.toList());
  }

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
  public static List<Node> getReferredNodes(@Nonnull ClassifierInstance<?> _this) {
    Objects.requireNonNull(_this, "_this should not be null");
    return getReferenceValues(_this).stream()
        .map(rv -> rv.getReferred())
        .filter(n -> n != null)
        .collect(Collectors.toList());
  }
}
