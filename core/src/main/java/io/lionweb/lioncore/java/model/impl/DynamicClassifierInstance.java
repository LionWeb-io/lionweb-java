package io.lionweb.lioncore.java.model.impl;

import io.lionweb.lioncore.java.language.*;
import io.lionweb.lioncore.java.model.AnnotationInstance;
import io.lionweb.lioncore.java.model.ClassifierInstance;
import io.lionweb.lioncore.java.model.Node;
import io.lionweb.lioncore.java.model.ReferenceValue;
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class DynamicClassifierInstance<T extends Classifier<T>>
    implements ClassifierInstance<T> {
  protected String id;
  protected Map<String, Object> propertyValues = new HashMap<>();
  protected Map<String, List<Node>> containmentValues = new HashMap<>();

  protected Map<String, List<ReferenceValue>> referenceValues = new HashMap<>();
  protected List<AnnotationInstance> annotations = new ArrayList<>();

  @Override
  public Object getPropertyValue(@Nonnull Property property) {
    Objects.requireNonNull(property, "Property should not be null");
    if (!getClassifier().allProperties().contains(property)) {
      throw new IllegalArgumentException("Property not belonging to this classifier");
    }
    return propertyValues.get(property.getID());
  }

  @Override
  public void setPropertyValue(@Nonnull Property property, Object value) {
    Objects.requireNonNull(property, "Property should not be null");
    if (!getClassifier().allProperties().contains(property)) {
      throw new IllegalArgumentException(
          "Property " + property + " is not belonging to classifier " + getClassifier());
    }
    if (value == null || value == Boolean.FALSE) {
      // We remove values corresponding to default values, so that comparisons of instances of
      // DynamicNode can be
      // simplified
      propertyValues.remove(property.getID());
    } else {
      propertyValues.put(property.getID(), value);
    }
  }

  @Override
  public List<Node> getChildren() {
    List<Node> allChildren = new LinkedList<>();
    getClassifier().allContainments().stream()
        .map(c -> getChildren(c))
        .forEach(children -> allChildren.addAll(children));
    return allChildren;
  }

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

  private void setContainmentSingleValue(Containment link, Node value) {
    List<Node> prevValue = containmentValues.get(link.getID());
    if (prevValue != null) {
      List<Node> copy = new LinkedList<>(prevValue);
      copy.forEach(c -> this.removeChild(c));
    }
    if (value == null) {
      containmentValues.remove(link.getID());
    } else {
      ((DynamicNode) value).setParent((Node) this);
      containmentValues.put(link.getID(), new ArrayList(Arrays.asList(value)));
    }
  }

  private void setReferenceSingleValue(Reference link, ReferenceValue value) {
    if (value == null) {
      referenceValues.remove(link.getID());
    } else {
      referenceValues.put(link.getID(), new ArrayList(Arrays.asList(value)));
    }
  }

  private void addContainment(Containment link, Node value) {
    assert link.isMultiple();
    ((DynamicNode) value).setParent((Node) this);
    if (containmentValues.containsKey(link.getID())) {
      containmentValues.get(link.getID()).add(value);
    } else {
      containmentValues.put(link.getID(), new ArrayList(Arrays.asList(value)));
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

  @Override
  public void removeChild(Node node) {
    for (Map.Entry<String, List<Node>> entry : containmentValues.entrySet()) {
      if (entry.getValue().contains(node)) {
        entry.getValue().remove(node);
        ((DynamicNode) node).setParent(null);
        return;
      }
    }
    throw new IllegalArgumentException("The given node is not a child of this node");
  }

  @Override
  public List<Node> getReferredNodes(Reference reference) {
    return getReferenceValues(reference).stream()
        .map(v -> v.getReferred())
        .collect(Collectors.toList());
  }

  @Nonnull
  @Override
  public List<ReferenceValue> getReferenceValues(Reference reference) {
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
  public List<AnnotationInstance> getAnnotations() {
    return annotations;
  }

  public List<AnnotationInstance> getAnnotations(Annotation annotation) {
    return annotations.stream()
        .filter(a -> a.getAnnotationDefinition() == annotation)
        .collect(Collectors.toList());
  }

  @Nullable
  public String getID() {
    return id;
  }

  public void addAnnotation(@Nonnull AnnotationInstance instance) {
    Objects.requireNonNull(instance);
    if (this.annotations.contains(instance)) {
      // necessary to avoid infinite loops and duplicate insertions
      return;
    }
    if (instance instanceof DynamicAnnotationInstance) {
      ((DynamicAnnotationInstance) instance).setAnnotated(this);
    }
    this.annotations.add(instance);
  }

  public void removeAnnotation(@Nonnull AnnotationInstance instance) {
    Objects.requireNonNull(instance);
    if (!this.annotations.remove(instance)) {
      throw new IllegalArgumentException();
    }
    if (instance instanceof DynamicAnnotationInstance) {
      ((DynamicAnnotationInstance) instance).setAnnotated(null);
    }
  }

  void tryToRemoveAnnotation(@Nonnull AnnotationInstance instance) {
    Objects.requireNonNull(instance);
    if (!this.annotations.remove(instance)) {
      return;
    }
    if (instance instanceof DynamicAnnotationInstance) {
      ((DynamicAnnotationInstance) instance).setAnnotated(null);
    }
  }
}
