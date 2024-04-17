package io.lionweb.lioncore.java.model.impl;

import io.lionweb.lioncore.java.language.*;
import io.lionweb.lioncore.java.model.HasSettableParent;
import io.lionweb.lioncore.java.model.Node;
import io.lionweb.lioncore.java.model.Partition;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * DynamicNode can be used to represent Node of any Concept. The drawback is that this class expose
 * only homogeneous-APIs (e.g., getProperty('book')) and not heterogeneous-APIs (e.g., getBook()).
 */
public class DynamicNode extends DynamicClassifierInstance<Concept>
    implements Node, HasSettableParent {
  private Node parent = null;
  private Concept concept = null;

  public DynamicNode(String id, Concept concept) {
    this.id = id;
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
      if (parent.getChildren(containment).stream().anyMatch( it -> it ==this)) {
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
