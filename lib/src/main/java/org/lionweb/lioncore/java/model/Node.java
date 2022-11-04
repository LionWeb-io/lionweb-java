package org.lionweb.lioncore.java.model;

import org.lionweb.lioncore.java.metamodel.*;

import java.util.List;

/**
 * A node is an instance of a Concept. It contains all the values associated to that instance.
 *
 * @see org.eclipse.emf.ecore.EObject Ecore equivalent <i>EObject</i>
 * @see <a href="https://www.jetbrains.com/help/mps/basic-notions.html">MPS equivalent <i>Node</i> in documentation</a>
 *
 * TODO consider if the Model should have a version too
 */
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
     * This will be null only for root nodes.
     */
    Containment getContainmentFeature();

    /**
     * Given a specific Annotation type it returns either null or the only instance of that
     * Annotation associated to the Node.
     *
     * TODO: Decide if we can have multiple annotation instances of the same annotation of one node.
     */
    AnnotationInstance getAnnotation(Annotation annotation);

    /**
     * If an annotation instance was already associated under the Annotation link used by this AnnotationInstance, then
     * it will be removed and replaced by the instance specified in the call to this method.
     *
     * In case the specified Annotation link cannot be used on Nodes of this Concept, then the exception
     * IllegalArgumentException will be thrown.
     */
    void setAnnotation(AnnotationInstance instance);

}
