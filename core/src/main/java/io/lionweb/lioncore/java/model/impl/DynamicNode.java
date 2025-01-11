package io.lionweb.lioncore.java.model.impl;

import io.lionweb.lioncore.java.language.*;
import io.lionweb.lioncore.java.model.HasSettableParent;
import io.lionweb.lioncore.java.model.Node;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * DynamicNode can be used to represent Node of any Concept. The drawback is that this class expose
 * only homogeneous-APIs (e.g., getProperty('book')) and not heterogeneous-APIs (e.g., getBook()).
 */
public class DynamicNode extends DynamicClassifierInstance<Concept>
    implements Node, HasSettableParent {
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
  public Node getParent() {
    return this.parent;
  }

  @Override
  public Concept getClassifier() {
    return this.concept;
  }

  @Override
  @Nullable
  public Containment getContainmentFeature() {
    if (parent == null) {
      return null;
    }
    for (Containment containment : parent.getClassifier().allContainments()) {
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
        && shallowNodeEquality(parent, that.parent)
        && shallowNodeEquality(concept, that.concept)
        && Objects.equals(propertyValues, that.propertyValues)
        && shallowContainmentsEquality(containmentValues, that.containmentValues)
        && Objects.equals(referenceValues, that.referenceValues)
        && Objects.equals(annotations, that.annotations);
  }

  private static boolean shallowContainmentsEquality(
      Map<String, List<Node>> containments1, Map<String, List<Node>> containments2) {
    if (!containments1.keySet().equals(containments2.keySet())) {
      return false;
    }
    return containments1.keySet().stream()
        .allMatch(
            containmentName -> {
              List<Node> nodes1 = containments1.get(containmentName);
              List<Node> nodes2 = containments2.get(containmentName);
              return nodes1.size() == nodes2.size()
                  && IntStream.range(0, nodes1.size())
                      .allMatch(i -> shallowNodeEquality(nodes1.get(i), nodes2.get(i)));
            });
  }

  private static boolean shallowNodeEquality(@Nullable Node node1, @Nullable Node node2) {
    if (node1 == null || node2 == null) {
      return true;
    }
    if (node1 != null && node2 != null && node1.getID() != null) {
      return Objects.equals(node1.getID(), node2.getID());
    }
    return Objects.equals(node1, node2);
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
