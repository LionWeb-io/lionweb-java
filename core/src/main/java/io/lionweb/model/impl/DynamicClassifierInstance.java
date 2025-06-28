package io.lionweb.model.impl;

import io.lionweb.language.*;
import io.lionweb.model.*;
import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class DynamicClassifierInstance<T extends Classifier<T>>
    extends AbstractClassifierInstance<T> implements ClassifierInstance<T> {
  /** The ID should _eventually_ be not null. */
  protected @Nullable String id;

  protected final Map<String, Object> propertyValues = new HashMap<>();
  protected final Map<String, List<Node>> containmentValues = new HashMap<>();

  protected final Map<String, List<ReferenceValue<?>>> referenceValues = new HashMap<>();

  @Nullable
  public String getID() {
    return id;
  }

  /** The ID can be _temporarily_ set to null, but _eventually_ it should be not null. */
  public void setID(@Nullable String id) {
    this.id = id;
  }

  // Public methods for properties

  @Override
  public Object getPropertyValue(@Nonnull Property property) {
    Objects.requireNonNull(property, "Property should not be null");
    Objects.requireNonNull(
        property.getKey(), "Property.key should not be null (property: " + property + ")");
    if (!getClassifier().allProperties().contains(property)) {
      throw new IllegalArgumentException("Property not belonging to this classifier");
    }
    Object storedValue = propertyValues.get(property.getKey());
    if (storedValue == null
        && property.getType()
            == LionCoreBuiltins.getBoolean(this.getClassifier().getLionWebVersion())
        && property.isRequired()) {
      return false;
    }
    return storedValue;
  }

  @Override
  public void setPropertyValue(@Nonnull Property property, @Nullable Object value) {
    Objects.requireNonNull(property, "Property should not be null");
    Objects.requireNonNull(property.getKey(), "Cannot assign a property with no Key specified");
    if (!getClassifier().allProperties().contains(property)) {
      throw new IllegalArgumentException(
          "Property " + property + " is not belonging to classifier " + getClassifier());
    }
    if ((value == null || value == Boolean.FALSE) && property.isRequired()) {
      // We remove values corresponding to default values, so that comparisons of instances of
      // DynamicNode can be simplified
      propertyValues.remove(property.getKey());
    } else {
      propertyValues.put(property.getKey(), value);
    }
  }

  // Public methods for containments

  @Override
  public List<Node> getChildren(@Nonnull Containment containment) {
    Objects.requireNonNull(containment, "Containment should not be null");
    Objects.requireNonNull(containment.getKey(), "Containment.key should not be null");
    if (!getClassifier().allContainments().contains(containment)) {
      throw new IllegalArgumentException("Containment not belonging to this concept");
    }
    if (containmentValues.containsKey(containment.getKey())) {
      return containmentValues.get(containment.getKey());
    } else {
      return Collections.emptyList();
    }
  }

  @Override
  public void addChild(@Nonnull Containment containment, @Nonnull Node child) {
    Objects.requireNonNull(containment);
    Objects.requireNonNull(child);
    if (containment.isMultiple()) {
      addContainment(containment, child);
    } else {
      setContainmentSingleValue(containment, child);
    }
  }

  @Override
  public void removeChild(Node node) {
    for (Map.Entry<String, List<Node>> entry : containmentValues.entrySet()) {
      if (entry.getValue().contains(node)) {
        entry.getValue().remove(node);
        if (node instanceof HasSettableParent) {
          ((HasSettableParent) node).setParent(null);
        }
        return;
      }
    }
    throw new IllegalArgumentException("The given node is not a child of this node");
  }

  @Override
  public void removeChild(@Nonnull Containment containment, int index) {
    Objects.requireNonNull(containment);
    Objects.requireNonNull(containment.getKey());
    if (!getClassifier().allContainments().contains(containment)) {
      throw new IllegalArgumentException("Containment not belonging to this concept");
    }
    if (containmentValues.containsKey(containment.getKey())) {
      List<Node> children = containmentValues.get(containment.getKey());
      if (children.size() > index) {
        children.remove(index);
      } else {
        throw new IllegalArgumentException(
            "Invalid index " + index + " when children are " + children.size());
      }
    }
  }

  @Nonnull
  @Override
  public List<ReferenceValue<?>> getReferenceValues(@Nonnull Reference reference) {
    Objects.requireNonNull(reference);
    Objects.requireNonNull(reference.getKey());
    if (!getClassifier().allReferences().contains(reference)) {
      throw new IllegalArgumentException("Reference not belonging to this concept");
    }
    if (referenceValues.containsKey(reference.getKey())) {
      return referenceValues.get(reference.getKey());
    } else {
      return Collections.emptyList();
    }
  }

  @Override
  public void addReferenceValue(@Nonnull Reference reference, @Nullable ReferenceValue<?> value) {
    Objects.requireNonNull(reference, "Reference should not be null");
    if (reference.isMultiple()) {
      if (value != null) {
        addReferenceMultipleValue(reference, value);
      }
    } else {
      setReferenceSingleValue(reference, value);
    }
  }

  @Override
  public void removeReferenceValue(
      @Nonnull Reference reference, @Nullable ReferenceValue<?> referenceValue) {
    Objects.requireNonNull(reference, "Reference should not be null");
    Objects.requireNonNull(reference.getKey(), "Reference.key should not be null");
    if (!getClassifier().allReferences().contains(reference)) {
      throw new IllegalArgumentException("Reference not belonging to this concept");
    }
    if (referenceValues.containsKey(reference.getKey())) {
      List<ReferenceValue<?>> referenceValuesOfInterest = referenceValues.get(reference.getKey());
      for (int i = 0; i < referenceValuesOfInterest.size(); i++) {
        ReferenceValue<?> rv = referenceValuesOfInterest.get(i);
        if (referenceValue == null) {
          if (rv == null) {
            referenceValuesOfInterest.remove(i);
            return;
          }
        } else {
          if (referenceValue.equals(rv)) {
            referenceValuesOfInterest.remove(i);
            return;
          }
        }
      }
    }
    throw new IllegalArgumentException(
        "The given reference value could not be found under reference " + reference.getName());
  }

  @Override
  public void removeReferenceValue(@Nonnull Reference reference, int index) {
    Objects.requireNonNull(reference, "Reference should not be null");
    Objects.requireNonNull(reference.getKey(), "Reference.key should not be null");
    if (!getClassifier().allReferences().contains(reference)) {
      throw new IllegalArgumentException("Reference not belonging to this classifier");
    }
    if (referenceValues.containsKey(reference.getKey())) {
      List<ReferenceValue<?>> referenceValuesOfInterest = referenceValues.get(reference.getKey());
      if (referenceValuesOfInterest.size() > index) {
        referenceValuesOfInterest.remove(index);
      } else {
        throw new IllegalArgumentException(
            "Invalid index "
                + index
                + " when reference values are "
                + referenceValuesOfInterest.size());
      }
    }
  }

  @Override
  public void setReferenceValues(
      @Nonnull Reference reference, @Nonnull List<? extends ReferenceValue<?>> values) {
    Objects.requireNonNull(reference, "Reference should not be null");
    Objects.requireNonNull(reference.getKey(), "Reference.key should not be null");
    if (!getClassifier().allReferences().contains(reference)) {
      throw new IllegalArgumentException("Reference not belonging to this classifier");
    }
    referenceValues.put(reference.getKey(), (List<ReferenceValue<?>>) values);
  }

  // Private methods for containments

  private void addContainment(Containment link, Node value) {
    assert link.isMultiple();
    if (value instanceof HasSettableParent) {
      ((HasSettableParent) value).setParent((ClassifierInstance<?>) this);
    }
    if (containmentValues.containsKey(link.getKey())) {
      containmentValues.get(link.getKey()).add(value);
    } else {
      containmentValues.put(link.getKey(), new ArrayList(Arrays.asList(value)));
    }
  }

  private void setContainmentSingleValue(Containment link, Node value) {
    List<Node> prevValue = containmentValues.get(link.getKey());
    if (prevValue != null) {
      List<Node> copy = new LinkedList<>(prevValue);
      copy.forEach(c -> this.removeChild(c));
    }
    if (value == null) {
      containmentValues.remove(link.getKey());
    } else {
      if (value instanceof HasSettableParent) {
        ((HasSettableParent) value).setParent((Node) this);
      }
      containmentValues.put(link.getKey(), new ArrayList(Arrays.asList(value)));
    }
  }

  // Private methods for references

  private void setReferenceSingleValue(Reference link, ReferenceValue<?> value) {
    if (value == null) {
      referenceValues.remove(link.getKey());
    } else {
      referenceValues.put(link.getKey(), new ArrayList(Arrays.asList(value)));
    }
  }

  private void addReferenceMultipleValue(Reference link, ReferenceValue<?> referenceValue) {
    assert link.isMultiple();
    if (referenceValue == null) {
      return;
    }
    if (referenceValues.containsKey(link.getKey())) {
      referenceValues.get(link.getKey()).add(referenceValue);
    } else {
      referenceValues.put(link.getKey(), new ArrayList(Arrays.asList(referenceValue)));
    }
  }
}
