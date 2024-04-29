package io.lionweb.lioncore.java.model.impl;

import io.lionweb.lioncore.java.language.*;
import io.lionweb.lioncore.java.model.HasSettableParent;
import io.lionweb.lioncore.java.model.Node;
import io.lionweb.lioncore.java.model.Partition;
import io.lionweb.lioncore.java.model.ReferenceValue;

import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * DynamicNode can be used to represent Node of any Concept. The drawback is that this class expose
 * only homogeneous-APIs (e.g., getProperty('book')) and not heterogeneous-APIs (e.g., getBook()).
 */
public class DynamicNode extends DynamicClassifierInstance<Concept>
    implements Node, HasSettableParent {

  public static DynamicNode shallowClone(Node originalNode) {
    Concept concept = originalNode.getConcept();
    DynamicNode clone = new DynamicNode(originalNode.getID(), concept);
    concept.allProperties().forEach(p -> clone.setPropertyValue(p, originalNode.getPropertyValue(p)));
    concept.allContainments().forEach(c -> {
      List<? extends Node> children = originalNode.getChildren(c);
      children.forEach(child -> clone.addChild(c, child));
    });
    concept.allReferences().forEach(r -> {
      List<ReferenceValue> referenceValues = originalNode.getReferenceValues(r);
      referenceValues.forEach(rv -> clone.addReferenceValue(r, rv));
    });
    return clone;
  }

  private Node parent = null;
  private Concept concept = null;

  public DynamicNode(@Nonnull String id, @Nonnull Concept concept) {
    this.id = id;
    this.concept = concept;
  }

  public DynamicNode() {
    this.id = null;
    this.concept = null;
  }

  public void setConcept(Concept concept) {
    this.concept = concept;
  }

  @Override
  public Partition getPartition() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Node getParent() {
    return this.parent;
  }

  @Override
  public Concept getConcept() {
    return this.concept;
  }

  @Override
  @Nullable
  public Containment getContainmentFeature() {
    if (parent == null) {
      return null;
    }
    for (Containment containment : parent.getConcept().allContainments()) {
      if (parent.getChildren(containment).stream().anyMatch(it -> it == this)) {
        return containment;
      }
    }
    throw new IllegalStateException("Unable to find the containment feature");
  }

  @Override
  public void setParent(Node parent) {
    this.parent = parent;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof DynamicNode)) {
      return false;
    }
    DynamicNode that = (DynamicNode) o;
    return Objects.equals(id, that.id)
        && Objects.equals(parent, that.parent)
        && Objects.equals(concept, that.concept)
        && Objects.equals(propertyValues, that.propertyValues)
        && Objects.equals(containmentValues, that.containmentValues)
        && Objects.equals(referenceValues, that.referenceValues)
        && Objects.equals(annotations, that.annotations);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    String qualifiedName;
    try {
      qualifiedName = concept.qualifiedName();
    } catch (RuntimeException t) {
      qualifiedName = "<cannot be calculated>";
    }

    return "DynamicNode{"
        + "id='"
        + id
        + '\''
        + ", parent="
        + (parent == null ? "null" : parent.getID())
        + ", concept="
        + qualifiedName
        + ", propertyValues={"
        + propertyValues.entrySet().stream()
            .map(e -> e.getKey() + "=" + e.getValue())
            .collect(Collectors.joining(", "))
        + "}, containmentValues={"
        + containmentValues.entrySet().stream()
            .map(
                e -> {
                  String childrenRepr =
                      e.getValue().stream().map(c -> c.getID()).collect(Collectors.joining(", "));
                  return e.getKey() + "=" + childrenRepr;
                })
            .collect(Collectors.joining(", "))
        + "}, referenceValues={"
        + referenceValues.entrySet().stream()
            .map(e -> e.getKey() + "=" + e.getValue())
            .collect(Collectors.joining(", "))
        + "}, annotations={"
        + annotations
        + "} }";
  }
}
