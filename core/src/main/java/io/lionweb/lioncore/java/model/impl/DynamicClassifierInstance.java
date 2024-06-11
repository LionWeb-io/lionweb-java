package io.lionweb.lioncore.java.model.impl;

import io.lionweb.lioncore.java.language.*;
import io.lionweb.lioncore.java.model.ClassifierInstance;
import io.lionweb.lioncore.java.model.HasSettableParent;
import io.lionweb.lioncore.java.model.Node;
import io.lionweb.lioncore.java.model.ReferenceValue;
import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class DynamicClassifierInstance<T extends Classifier<T>>
    extends AbstractClassifierInstance<T> implements ClassifierInstance<T> {
  /** The ID should _eventually_ be not null. */
  protected @Nullable String id;

  protected final Map<String, Object> propertyValues = new HashMap<>();
  protected final Map<String, List<Node>> containmentValues = new HashMap<>();

  protected final Map<String, List<ReferenceValue>> referenceValues = new HashMap<>();

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
    if (!getClassifier().allProperties().contains(property)) {
      throw new IllegalArgumentException("Property not belonging to this classifier");
    }
    Object storedValue = propertyValues.get(property.getID());
    if (storedValue == null
        && property.getType() == LionCoreBuiltins.getBoolean()
        && property.isRequired()) {
      return false;
    }
    return storedValue;
  }

  @Override
  public void setPropertyValue(@Nonnull Property property, Object value) {
    Objects.requireNonNull(property, "Property should not be null");
    if (!getClassifier().allProperties().contains(property)) {
      throw new IllegalArgumentException(
          "Property " + property + " is not belonging to classifier " + getClassifier());
    }
    if ((value == null || value == Boolean.FALSE) && property.isRequired()) {
      // We remove values corresponding to default values, so that comparisons of instances of
      // DynamicNode can be simplified
      propertyValues.remove(property.getID());
    } else {
      propertyValues.put(property.getID(), value);
    }
  }

  // Public methods for containments

  @Override
  public List<Node> getChildren(@Nonnull Containment containment) {
    Objects.requireNonNull(containment, "Containment should not be null");
    if (!getClassifier().allContainments().contains(containment)) {
      throw new IllegalArgumentException("Containment not belonging to this concept");
    }
    if (containmentValues.containsKey(containment.getID())) {
      return containmentValues.get(containment.getID());
    } else {
      return Collections.emptyList();
    }
  }

  @Override
  public void addChild(@Nonnull Containment containment, Node child) {
    Objects.requireNonNull(containment);
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
    if (!getClassifier().allContainments().contains(containment)) {
      throw new IllegalArgumentException("Containment not belonging to this concept");
    }
    if (containmentValues.containsKey(containment.getID())) {
      List<Node> children = containmentValues.get(containment.getID());
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
  public List<ReferenceValue> getReferenceValues(@Nonnull Reference reference) {
    if (!getClassifier().allReferences().contains(reference)) {
      throw new IllegalArgumentException("Reference not belonging to this concept");
    }
    if (referenceValues.containsKey(reference.getID())) {
      return referenceValues.get(reference.getID());
    } else {
      return Collections.emptyList();
    }
  }

  @Override
  public void addReferenceValue(@Nonnull Reference reference, @Nullable ReferenceValue value) {
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
      @Nonnull Reference reference, @Nullable ReferenceValue referenceValue) {
    if (!getClassifier().allReferences().contains(reference)) {
      throw new IllegalArgumentException("Reference not belonging to this concept");
    }
    if (referenceValues.containsKey(reference.getID())) {
      List<ReferenceValue> referenceValuesOfInterest = referenceValues.get(reference.getID());
      for (int i = 0; i < referenceValuesOfInterest.size(); i++) {
        ReferenceValue rv = referenceValuesOfInterest.get(i);
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
    if (!getClassifier().allReferences().contains(reference)) {
      throw new IllegalArgumentException("Reference not belonging to this classifier");
    }
    if (referenceValues.containsKey(reference.getID())) {
      List<ReferenceValue> referenceValuesOfInterest = referenceValues.get(reference.getID());
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
      @Nonnull Reference reference, @Nonnull List<? extends ReferenceValue> values) {
    if (!getClassifier().allReferences().contains(reference)) {
      throw new IllegalArgumentException("Reference not belonging to this classifier");
    }
    referenceValues.put(reference.getID(), (List<ReferenceValue>) values);
  }

  // Private methods for containments

  private void addContainment(Containment link, Node value) {
    assert link.isMultiple();
    if (value instanceof HasSettableParent) {
      ((HasSettableParent) value).setParent((Node) this);
    }
    if (containmentValues.containsKey(link.getID())) {
      containmentValues.get(link.getID()).add(value);
    } else {
      containmentValues.put(link.getID(), new ArrayList(Arrays.asList(value)));
    }
  }

  private void setContainmentSingleValue(Containment link, Node value) {
    List<Node> prevValue = containmentValues.get(link.getID());
    if (prevValue != null) {
      List<Node> copy = new LinkedList<>(prevValue);
      copy.forEach(c -> this.removeChild(c));
    }
    if (value == null) {
      containmentValues.remove(link.getID());
    } else {
      if (value instanceof HasSettableParent) {
        ((HasSettableParent) value).setParent((Node) this);
      }
      containmentValues.put(link.getID(), new ArrayList(Arrays.asList(value)));
    }
  }

  // Private methods for references

  private void setReferenceSingleValue(Reference link, ReferenceValue value) {
    if (value == null) {
      referenceValues.remove(link.getID());
    } else {
      referenceValues.put(link.getID(), new ArrayList(Arrays.asList(value)));
    }
  }

  private void addReferenceMultipleValue(Reference link, ReferenceValue referenceValue) {
    assert link.isMultiple();
    if (referenceValue == null) {
      return;
    }
    if (referenceValues.containsKey(link.getID())) {
      referenceValues.get(link.getID()).add(referenceValue);
    } else {
      referenceValues.put(link.getID(), new ArrayList(Arrays.asList(referenceValue)));
    }
  }
}
