package org.lionweb.lioncore.java.model;

import org.lionweb.lioncore.java.metamodel.Containment;
import org.lionweb.lioncore.java.metamodel.Property;
import org.lionweb.lioncore.java.metamodel.Reference;

import java.util.List;

public interface HasFeatureValues {
    /**
     * Get the property value associated with the specified property.
     */
    Object getPropertyValue(Property property);

    /**
     * If the value is not compatible with the type of the property, the exception IllegalArgumentException will be
     * thrown.
     * If the feature is derived, the exception IllegalArgumentException will be thrown.
     */
    void setPropertyValue(Property property, Object value);

    /**
     * This return all the Nodes directly contained into this Node.
     */
    List<Node> getChildren();

    /**
     * This return all the Nodes directly contained into this Node under the specific Containment relation specified.
     */
    List<Node> getChildren(Containment containment);

    /**
     * Add a child to the specified list of children associated with the given Containment relation.
     * If the specified Containment does not allow for multiple values, and if a value is already set than
     * the exception IllegalStateException will be thrown.
     *
     * If the child has not a Concept compatible with the target of the Containement, the exception
     * IllegalArgumentException will be
     * thrown.
     * If the Containment feature is derived, the exception IllegalArgumentException will be thrown.
     */
    void addChild(Containment containment, Node child);

    /**
     * Remove the given child from the list of children associated with the Node, making it a dangling Node.
     * If the specified Node is not currently a child of this Node the exception IllegalArgumentException will be thrown.
     *
     * If the Containment feature is derived, the exception IllegalArgumentException will be thrown.
     */
    void removeChild(Node node);

    /**
     * Return the Node referred to under the specified Reference link. This returns null if no Node is associated
     * with the specified Reference link.
     *
     * The Node returned is guaranteed to be either part of this Node's Model or of Models imported by this Node's
     * Model.
     */
    Node getReferredNode(Reference reference);

    /**
     * If a Node was already associated with this reference link, it will be dissasociated and replaced by this new
     * Node specified.
     *
     * The Node specified should be either part of this Node's Model or of Models imported by this Node's
     * Model. If that is not the case the exception IllegalArgumentException will be thrown.
     *
     * If the referredNode has not a Concept compatible with the target of the Reference, the exception
     * IllegalArgumentException will be thrown.
     * If the Reference feature is derived, the exception IllegalArgumentException will be thrown.
     */
    void setReferredNode(Reference reference, Node referredNode);
}
