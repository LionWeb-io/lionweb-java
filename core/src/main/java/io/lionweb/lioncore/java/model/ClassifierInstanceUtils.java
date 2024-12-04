package io.lionweb.lioncore.java.model;

import io.lionweb.lioncore.java.language.Classifier;
import io.lionweb.lioncore.java.language.Containment;
import io.lionweb.lioncore.java.language.Property;
import io.lionweb.lioncore.java.language.Reference;
import io.lionweb.lioncore.java.versions.LionWebVersionToken;

import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ClassifierInstanceUtils {

  // Public methods about properties

  @Nullable
  public static Object getPropertyValueByName(
      @Nonnull ClassifierInstance<?, ?> _this, @Nonnull String propertyName) {
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
      @Nonnull ClassifierInstance<?, ?> _this, @Nonnull String propertyName, @Nullable Object value) {
    Objects.requireNonNull(_this, "_this should not be null");
    Objects.requireNonNull(propertyName, "propertyName should not be null");
    Classifier<?, ?> classifier = _this.getClassifier();
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
      @Nonnull ClassifierInstance<?, ?> _this, @Nonnull String propertyID) {
    Objects.requireNonNull(_this, "_this should not be null");
    Objects.requireNonNull(propertyID, "propertyID should not be null");
    Property property = _this.getClassifier().getPropertyByID(propertyID);
    return _this.getPropertyValue(property);
  }

  // Public methods about containments

  /** This return all the Nodes directly contained into this Node. */
  @Nonnull
  public static <V extends LionWebVersionToken> List<Node<V>> getChildren(@Nonnull ClassifierInstance<?, V> _this) {
    Objects.requireNonNull(_this, "_this should not be null");
    List<Node<V>> allChildren = new LinkedList<>();
    _this.getClassifier().allContainments().stream()
        .map(c -> _this.getChildren(c))
        .forEach(children -> allChildren.addAll(children));
    return allChildren;
  }

  @Nonnull
  public static <V extends LionWebVersionToken> List<? extends Node<V>> getChildrenByContainmentName(
      @Nonnull ClassifierInstance<?, V> _this, @Nonnull String containmentName) {
    Objects.requireNonNull(_this, "_this should not be null");
    Objects.requireNonNull(containmentName, "containmentName should not be null");
    return _this.getChildren(_this.getClassifier().requireContainmentByName(containmentName));
  }

  @Nullable
  public static <V extends LionWebVersionToken> Node<V> getOnlyChildByContainmentName(
      @Nonnull ClassifierInstance<?, V> _this, @Nonnull String containmentName) {
    Objects.requireNonNull(_this, "_this should not be null");
    Objects.requireNonNull(containmentName, "containmentName should not be null");
    List<? extends Node> children = getChildrenByContainmentName(_this, containmentName);
    if (children.size() > 1) {
      throw new IllegalStateException();
    } else if (children.isEmpty()) {
      return null;
    } else {
      return children.get(0);
    }
  }

  public static void setOnlyChildByContainmentName(
      @Nonnull ClassifierInstance<?, ?> _this, @Nonnull String containmentName, @Nullable Node child) {
    Objects.requireNonNull(_this, "_this should not be null");
    Objects.requireNonNull(containmentName, "containmentName should not be null");
    Containment containment = _this.getClassifier().requireContainmentByName(containmentName);
    if (containment.isMultiple()) {
      throw new IllegalArgumentException("Cannot invoke this method with a multiple containment");
    }
    List<? extends Node> children = _this.getChildren(containment);
    if (children.size() > 1) {
      throw new IllegalStateException(
          "The node should not have multiple children under containment " + containment);
    }
    if (children.size() > 0) {
      _this.removeChild(children.get(0));
    }
    _this.addChild(containment, child);
  }

  // Public methods about references

  @Nonnull
  public static <V extends LionWebVersionToken> List<ReferenceValue<V>> getReferenceValues(@Nonnull ClassifierInstance<?, V> _this) {
    Objects.requireNonNull(_this, "_this should not be null");
    List<ReferenceValue<V>> allReferredValues = new LinkedList<>();
    _this.getClassifier().allReferences().stream()
        .map(r -> _this.getReferenceValues(r))
        .forEach(referenceValues -> allReferredValues.addAll(referenceValues));
    return allReferredValues;
  }

  public static <V extends LionWebVersionToken> void setOnlyReferenceValue(
      @Nonnull ClassifierInstance<?, V> _this,
      @Nonnull Reference<V> reference,
      @Nullable ReferenceValue<V> value) {
    Objects.requireNonNull(_this, "_this should not be null");
    if (value == null) {
      _this.setReferenceValues(reference, Collections.emptyList());
    } else {
      _this.setReferenceValues(reference, Arrays.asList(value));
    }
  }

  public static void setOnlyReferenceValueByName(
      @Nonnull ClassifierInstance<?, ?> _this,
      @Nonnull String referenceName,
      @Nullable ReferenceValue value) {
    Objects.requireNonNull(_this, "_this should not be null");
    Objects.requireNonNull(referenceName, "referenceName should not be null");
    Reference reference = _this.getClassifier().requireReferenceByName(referenceName);
    setOnlyReferenceValue(_this, reference, value);
  }

  public static <V extends LionWebVersionToken> void setReferenceValuesByName(
      @Nonnull ClassifierInstance<?, V> _this,
      @Nonnull String referenceName,
      @Nonnull List<? extends ReferenceValue<V>> values) {
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
  public static <V extends LionWebVersionToken> List<Node<V>> getReferredNodes(
      @Nonnull ClassifierInstance<?, V> _this, @Nonnull Reference<V> reference) {
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
  public static List<Node> getReferredNodes(@Nonnull ClassifierInstance<?, ?> _this) {
    Objects.requireNonNull(_this, "_this should not be null");
    return getReferenceValues(_this).stream()
        .map(rv -> rv.getReferred())
        .filter(n -> n != null)
        .collect(Collectors.toList());
  }

  @Nonnull
  public static <V extends LionWebVersionToken> List<ReferenceValue<V>> getReferenceValueByName(
      @Nonnull ClassifierInstance<?, V> _this, @Nonnull String referenceName) {
    Objects.requireNonNull(_this, "_this should not be null");
    Objects.requireNonNull(referenceName, "referenceName should not be null");
    Classifier<?, V> classifier = _this.getClassifier();
    if (classifier == null) {
      throw new IllegalStateException(
          "Concept should not be null for "
              + _this
              + " (class "
              + _this.getClass().getCanonicalName()
              + ")");
    }
    Reference reference = classifier.getReferenceByName(referenceName);
    if (reference == null) {
      throw new IllegalArgumentException(
          "Concept "
              + _this.getClassifier().qualifiedName()
              + " does not contained a property named "
              + referenceName);
    }
    return _this.getReferenceValues(reference);
  }

  @Nullable
  public static <V extends LionWebVersionToken> ReferenceValue getOnlyReferenceValueByReferenceName(
      @Nonnull ClassifierInstance<?, V> _this, @Nonnull String referenceName) {
    Objects.requireNonNull(_this, "_this should not be null");
    Objects.requireNonNull(referenceName, "referenceName should not be null");
    List<ReferenceValue<V>> referenceValues = getReferenceValueByName(_this, referenceName);
    if (referenceValues.size() > 1) {
      throw new IllegalStateException();
    } else if (referenceValues.isEmpty()) {
      return null;
    } else {
      return referenceValues.get(0);
    }
  }

  public static void addChild(
      @Nonnull ClassifierInstance<?, ?> _this, @Nonnull String containmentName, @Nonnull Node child) {
    Objects.requireNonNull(_this, "_this should not be null");
    Objects.requireNonNull(containmentName, "containmentName should not be null");
    Objects.requireNonNull(child, "child should not be null");
    Containment containment = _this.getClassifier().getContainmentByName(containmentName);
    _this.addChild(containment, child);
  }
}
