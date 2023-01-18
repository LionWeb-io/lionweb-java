package org.lionweb.lioncore.java.model;

import org.lionweb.lioncore.java.Experimental;
import org.lionweb.lioncore.java.metamodel.*;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * A node is an instance of a Concept. It contains all the values associated to that instance.
 *
 * @see org.eclipse.emf.ecore.EObject Ecore equivalent <i>EObject</i>
 * @see <a href="https://www.jetbrains.com/help/mps/basic-notions.html">MPS equivalent <i>Node</i> in documentation</a>
 * @see org.modelix.model.api.INode Modelix equivalent <i>INode</i>
 *
 * TODO consider if the Model should have a version too
 */
@Experimental
public interface Node extends HasFeatureValues {
    /**
     * The Model in which the Node is contained. A Node is contained into a Model when it is a root node of that
     * Node or if one of its ancestors is.
     */
    Model getModel();

    /**
     * If a Node is a root node in a Model, this method returns the node itself. Otherwise it returns
     * the ancestor which is a root node. This method should return null only if the Node is not inserted
     * in a Model and it is therefore considered a dangling Node.
     */
    Node getRoot();

    /**
     * The immediate parent of the Node. This should be null only for root nodes.
     */
    Node getParent();

    /**
     * The concept of which this Node is an instance. The Concept should not be abstract.
     */
    Concept getConcept();

    /**
     * Return all the annotations associated to this Node.
     */
    List<AnnotationInstance> getAnnotations();

    /**
     * Return the Containment feature used to hold this Node within its parent.
     * This will be null only for root nodes or dangling nodes (which are not distinguishable by looking at the node
     * itself).
     *
     * @see <a href="https://download.eclipse.org/modeling/emf/emf/javadoc/2.6.0/org/eclipse/emf/ecore/EObject.html#eContainingFeature()">Ecore equivalent <i>EObject.eContainingFeature</i> in documentation</a>.
     */
    Containment getContainmentFeature();

    /**
     * Given a specific Annotation type it returns either the list of instances of that
     * Annotation associated to the Node.
     */
    @Nonnull List<AnnotationInstance> getAnnotations(Annotation annotation);

    /**
     * If an annotation instance was already associated under the Annotation link used by this AnnotationInstance, and the
     * annotation does not support multiple values, then the existing instance will be removed and replaced by the
     * instance specified in the call to this method.
     *
     * In case the specified Annotation link cannot be used on Nodes of this Concept, then the exception
     * IllegalArgumentException will be thrown.
     */
    void addAnnotation(AnnotationInstance instance);

}
