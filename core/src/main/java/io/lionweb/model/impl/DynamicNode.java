package io.lionweb.model.impl;

import static io.lionweb.model.ClassifierInstanceUtils.shallowAnnotationsEquality;
import static io.lionweb.model.ClassifierInstanceUtils.shallowClassifierInstanceEquality;

import io.lionweb.language.*;
import io.lionweb.model.*;
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
  public void registerPartitionObserver(@Nullable PartitionObserver observer) {
    if (!this.isRoot()) {
      throw new UnsupportedOperationException(
          "Cannot register a partition observer on a node which is not root");
    }
    if (this.observer == observer) {
      throw new IllegalArgumentException("Observer already registered: " + observer);
    }
    if (this.observer == null) {
      this.observer = observer;
    } else {
      this.observer = CompositePartitionObserver.combine(this.observer, observer);
    }
    thisAndAllDescendants().forEach(d -> d.partitionObserverRegistered(this.observer));
  }

  @Override
  public void unregisterPartitionObserver(@Nonnull PartitionObserver observer) {
    if (!this.isRoot()) {
      throw new UnsupportedOperationException(
          "Cannot unregister a partition observer on a node which is not root");
    }
    if (this.observer == observer) {
      this.observer = null;
      thisAndAllDescendants().forEach(ClassifierInstance::partitionObserverUnregistered);
      return;
    }
    if (this.observer instanceof CompositePartitionObserver) {
      this.observer = ((CompositePartitionObserver) this.observer).remove(observer);
      thisAndAllDescendants().forEach(ClassifierInstance::partitionObserverUnregistered);
      if (this.observer == null) {
        // refObservers.forEach(ReferenceValue::unregisterObserver);
        // refObservers = null;
        throw new UnsupportedOperationException();
      } else {
        thisAndAllDescendants().forEach(d -> d.partitionObserverRegistered(this.observer));
      }
    } else {
      throw new IllegalArgumentException("Observer not registered: " + observer);
    }
  }

  /**
   * In most cases we will have no observers or one observer, shared across many nodes, so we avoid
   * instantiating lists. We Represent multiple observers with a CompositeObserver instead.
   */
  protected @Nullable PartitionObserver observer = null;

  @Override
  public DynamicNode setParent(@Nullable ClassifierInstance<?> parent) {
    this.parent = parent;
    return this;
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
    return Objects.equals(getID(), that.getID())
        && shallowClassifierInstanceEquality(parent, that.parent)
        && shallowClassifierInstanceEquality(concept, that.getClassifier())
        && Objects.equals(propertyValues, that.propertyValues)
        && shallowContainmentsEquality(containmentValues, that.containmentValues)
        && shallowReferenceEquality(referenceValues, that.referenceValues)
        && shallowAnnotationsEquality(annotations, that.annotations);
  }

  private static boolean shallowReferenceEquality(
      Map<String, List<ReferenceValue>> reference1, Map<String, List<ReferenceValue>> reference2) {
    boolean empty1 = reference1 == null || reference1.isEmpty();
    boolean empty2 = reference2 == null || reference2.isEmpty();
    if (empty1 != empty2) {
      return false;
    }
    if (empty1 && empty2) {
      return true;
    }
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
    boolean empty1 = containments1 == null || containments1.isEmpty();
    boolean empty2 = containments2 == null || containments2.isEmpty();
    if (empty1 != empty2) {
      return false;
    }
    if (empty1 && empty2) {
      return true;
    }
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
    return Objects.hash(getID());
  }

  @Override
  public String toString() {
    String qualifiedName;
    try {
      qualifiedName = concept.qualifiedName();
    } catch (RuntimeException t) {
      qualifiedName = "<cannot be calculated>";
    }

    String referenceValuesStr = "<null>";
    if (referenceValues != null) {
      referenceValuesStr =
          referenceValues.entrySet().stream()
              .map(e -> e.getKey() + "=" + e.getValue())
              .collect(Collectors.joining(", "));
    }

    String containmentValueStr = "<null>";
    if (containmentValues != null) {
      containmentValues.entrySet().stream()
          .map(
              e -> {
                String childrenRepr =
                    e.getValue().stream().map(c -> c.getID()).collect(Collectors.joining(", "));
                return e.getKey() + "=" + childrenRepr;
              })
          .collect(Collectors.joining(", "));
    }

    return "DynamicNode{"
        + "id='"
        + getID()
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
        + containmentValueStr
        + "}, referenceValues={"
        + referenceValuesStr
        + "}, annotations={"
        + annotations
        + "} }";
  }
}
