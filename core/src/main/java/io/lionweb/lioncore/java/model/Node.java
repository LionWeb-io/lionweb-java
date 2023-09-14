package io.lionweb.lioncore.java.model;

import io.lionweb.lioncore.java.language.*;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A node is an instance of a Concept. It contains all the values associated to that instance.
 *
 * @see org.eclipse.emf.ecore.EObject Ecore equivalent <i>EObject</i>
 * @see <a href="https://www.jetbrains.com/help/mps/basic-notions.html">MPS equivalent <i>Node</i>
 *     in documentation</a>
 * @see org.modelix.model.api.INode Modelix equivalent <i>INode</i>
 *     <p>TODO consider if the Model should have a version too
 */
public interface Node extends ClassifierInstance {

  /**
   * This return the Node ID.
   *
   * <p>A valid Node ID should not be null, but this method can return a null value in case the Node
   * is in invalid state.
   */
  @Nullable
  String getID();

  /**
   * The Model in which the Node is contained. A Node is contained into a Model when it is a root
   * node of that Node or if one of its ancestors is.
   */
  Model getModel();

  /**
   * If a Node is a root node in a Model, this method returns the node itself. Otherwise it returns
   * the ancestor which is a root node. This method should return null only if the Node is not
   * inserted in a Model and it is therefore considered a dangling Node.
   */
  Node getRoot();

  /** The immediate parent of the Node. This should be null only for root nodes. */
  Node getParent();

  /** The concept of which this Node is an instance. The Concept should not be abstract. */
  Concept getConcept();

  /** Return all the annotations associated to this Node. */
  List<AnnotationInstance> getAnnotations();

  /**
   * Return the Containment feature used to hold this Node within its parent. This will be null only
   * for root nodes or dangling nodes (which are not distinguishable by looking at the node itself).
   *
   * @see <a
   *     href="https://download.eclipse.org/modeling/emf/emf/javadoc/2.6.0/org/eclipse/emf/ecore/EObject.html#eContainingFeature()">Ecore
   *     equivalent <i>EObject.eContainingFeature</i> in documentation</a>.
   */
  Containment getContainmentFeature();

  /**
   * Given a specific Annotation type it returns either the list of instances of that Annotation
   * associated to the Node.
   */
  @Nonnull
  List<AnnotationInstance> getAnnotations(Annotation annotation);

  /**
   * If an annotation instance was already associated under the Annotation link used by this
   * AnnotationInstance, and the annotation does not support multiple values, then the existing
   * instance will be removed and replaced by the instance specified in the call to this method.
   *
   * <p>In case the specified Annotation link cannot be used on Nodes of this Concept, then the
   * exception IllegalArgumentException will be thrown.
   */
  void addAnnotation(AnnotationInstance instance);

  default Object getPropertyValueByName(String propertyName) {
    Property property = this.getConcept().getPropertyByName(propertyName);
    if (property == null) {
      throw new IllegalArgumentException(
          "Concept "
              + this.getConcept().qualifiedName()
              + " does not contained a property named "
              + propertyName);
    }
    return getPropertyValue(property);
  }

  default void setPropertyValueByName(String propertyName, Object value) {
    Property property = this.getConcept().getPropertyByName(propertyName);
    if (property == null) {
      throw new IllegalArgumentException(
          "Concept "
              + this.getConcept().qualifiedName()
              + " does not contained a property named "
              + propertyName);
    }
    setPropertyValue(property, value);
  }

  default Object getPropertyValueByID(String propertyID) {
    Property property = this.getConcept().getPropertyByID(propertyID);
    return getPropertyValue(property);
  }

  /** Return a list containing this node and all its descendants. */
  default @Nonnull List<Node> thisAndAllDescendants() {
    List<Node> nodes = new ArrayList<>();
    nodes.add(this);
    for (Node child : this.getChildren()) {
      nodes.addAll(child.thisAndAllDescendants());
    }
    return nodes;
  }

  default List<? extends Node> getChildrenByContainmentName(String containmentName) {
    return getChildren(getConcept().requireContainmentByName(containmentName));
  }

  default @Nullable Node getOnlyChildByContainmentName(String containmentName) {
    List<? extends Node> children = getChildrenByContainmentName(containmentName);
    if (children.size() > 1) {
      throw new IllegalStateException();
    } else if (children.size() == 0) {
      return null;
    } else {
      return children.get(0);
    }
  }

  default List<ReferenceValue> getReferenceValueByName(String referenceName) {
    Reference reference = this.getConcept().getReferenceByName(referenceName);
    if (reference == null) {
      throw new IllegalArgumentException(
          "Concept "
              + this.getConcept().qualifiedName()
              + " does not contained a property named "
              + referenceName);
    }
    return getReferenceValues(reference);
  }
}
