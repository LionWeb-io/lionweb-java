package io.lionweb.lioncore.java.model;

import io.lionweb.lioncore.java.language.Reference;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ClassifieInstanceUtils {

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

  public static void setOnlyReferenceValueByName(
      @Nonnull ClassifierInstance<?> _this,
      @Nonnull String referenceName,
      @Nullable ReferenceValue value) {
    Objects.requireNonNull(_this, "_this should not be null");
    Objects.requireNonNull(referenceName, "referenceName should not be null");
    Reference reference = _this.getClassifier().requireReferenceByName(referenceName);
    _this.setOnlyReferenceValue(reference, value);
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
