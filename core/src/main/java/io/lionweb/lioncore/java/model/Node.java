package io.lionweb.lioncore.java.model;

import io.lionweb.lioncore.java.language.*;
import java.util.ArrayList;
import java.util.LinkedList;
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
public interface Node extends ClassifierInstance<Concept> {

  /**
   * This return the Node ID.
   *
   * <p>A valid Node ID should not be null, but this method can return a null value in case the Node
   * is in invalid state.
   */
  @Nullable
  String getID();

  /**
   * If a Node is a root node in a Model, this method returns the node itself. Otherwise it returns
   * the ancestor which is a root node. This method should return null only if the Node is not
   * inserted in a Model and it is therefore considered a dangling Node.
   */
  default Node getRoot() {
    List<Node> ancestors = new LinkedList<>();
    Node curr = this;
    while (curr != null) {
      if (!ancestors.contains(curr)) {
        ancestors.add(curr);
        curr = (Node) curr.getParent();
      } else {
        throw new IllegalStateException("A circular hierarchy has been identified");
      }
    }
    return ancestors.get(ancestors.size() - 1);
  }

  default boolean isRoot() {
    return getParent() == null;
  }

  @Override
  Node getParent();

  /** The concept of which this Node is an instance. The Concept should not be abstract. */
  Concept getClassifier();

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
   * Return a list containing this node and all its descendants. Does <i>not</i> include
   * annotations.
   */
  default @Nonnull List<Node> thisAndAllDescendants() {
    List<Node> result = new ArrayList<>();
    ClassifierInstance.collectSelfAndDescendants(this, false, result);
    return result;
  }

  // References methods

}
