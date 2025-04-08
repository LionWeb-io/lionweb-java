package io.lionweb.lioncore.java.model.impl;

import static io.lionweb.lioncore.java.model.ClassifierInstanceUtils.shallowAnnotationsEquality;
import static io.lionweb.lioncore.java.model.ClassifierInstanceUtils.shallowClassifierInstanceEquality;

import io.lionweb.lioncore.java.language.*;
import io.lionweb.lioncore.java.model.*;
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
  private ClassifierInstance<?> parent = null;
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
  public ClassifierInstance<?> getParent() {
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
  public void setParent(ClassifierInstance<?> parent) {
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
        && shallowClassifierInstanceEquality(parent, that.parent)
        && shallowClassifierInstanceEquality(concept, that.getClassifier())
        && Objects.equals(propertyValues, that.propertyValues)
        && shallowContainmentsEquality(containmentValues, that.containmentValues)
        && shallowReferenceEquality(referenceValues, that.referenceValues)
        && shallowAnnotationsEquality(annotations, that.annotations);
  }

  private static boolean shallowReferenceEquality(
      Map<String, List<ReferenceValue>> reference1, Map<String, List<ReferenceValue>> reference2) {
    if (!reference1.keySet().equals(reference2.keySet())) {
      return false;
    }
    return reference1.keySet().stream()
        .allMatch(
            referenceName -> {
              List<ReferenceValue> references1 = reference1.get(referenceName);
              List<ReferenceValue> references2 = reference2.get(referenceName);
              return ClassifierInstanceUtils.shallowReferenceEquality(references1, references2);
            });
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
              return ClassifierInstanceUtils.shallowContainmentEquality(nodes1, nodes2);
            });
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
