package org.lionweb.lioncore.java.model.impl;

import org.lionweb.lioncore.java.metamodel.*;
import org.lionweb.lioncore.java.model.AnnotationInstance;
import org.lionweb.lioncore.java.model.Model;
import org.lionweb.lioncore.java.model.Node;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * DynamicNode can be used to represent Node of any Concept. The drawback is that this class expose only homogeneous-APIs
 * (e.g., getProperty('book')) and not heterogeneous-APIs (e.g., getBook()).
 */
public class DynamicNode implements Node {
    private String id;
    private Node parent = null;
    private Concept concept;

    private Map<String, Object> propertyValues = new HashMap<>();
    private Map<String, List<Node>> linkValues = new HashMap<>();

    public DynamicNode(String id, Concept concept) {
        this.id = id;
        this.concept = concept;
    }

    @Override
    public Object getPropertyValue(@Nonnull Property property) {
        Objects.requireNonNull(property, "Property should not be null");
        if (!getConcept().allProperties().contains(property)) {
            throw new IllegalArgumentException("Property not belonging to this concept");
        }
        return propertyValues.get(property.getID());
    }

    @Override
    public void setPropertyValue(@Nonnull Property property, Object value) {
        Objects.requireNonNull(property, "Property should not be null");
        if (!getConcept().allProperties().contains(property)) {
            throw new IllegalArgumentException("Property " + property + " is not belonging to concept " + getConcept());
        }
        propertyValues.put(property.getID(), value);
    }

    @Override
    public List<Node> getChildren() {
        List<Node> allChildren = new LinkedList<>();
        getConcept().allContainments().stream().map(c -> getChildren(c)).forEach(children -> allChildren.addAll(children));
        return allChildren;
    }

    @Override
    public List<Node> getChildren(Containment containment) {
        if (!getConcept().allContainments().contains(containment)) {
            throw new IllegalArgumentException("Containment not belonging to this concept");
        }
        if (linkValues.containsKey(containment.getID())) {
            return linkValues.get(containment.getID());
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public void addChild(Containment containment, Node child) {
        if (containment == null) {
            throw new IllegalArgumentException();
        }
        if (containment.isMultiple()) {
            addLinkMultipleValue(containment, child);
        } else {
            setLinkSingleValue(containment, child);
        }
    }

    private void setLinkSingleValue(Link link, Node value) {
        if (link instanceof Containment) {
            List<Node> prevValue = linkValues.get(link.getID());
            if (prevValue != null) {
                List<Node> copy = new LinkedList<>(prevValue);
                copy.forEach(c -> this.removeChild(c));
            }
        }
        if (value == null) {
            linkValues.remove(link.getID());
        } else {
            if (link instanceof Containment) {
                ((DynamicNode)value).setParent(this);
            }
            linkValues.put(link.getID(), new ArrayList(Arrays.asList(value)));
        }
    }

    private void addLinkMultipleValue(Link link, Node value) {
        assert link.isMultiple();
        if (link instanceof Containment) {
            ((DynamicNode)value).setParent(this);
        }
        if (linkValues.containsKey(link.getID())) {
            linkValues.get(link.getID()).add(value);
        } else {
            linkValues.put(link.getID(), new ArrayList(Arrays.asList(value)));
        }
    }

    @Override
    public void removeChild(Node node) {
        throw new UnsupportedOperationException();
    }

    @Nonnull
    @Override
    public List<Node> getReferredNodes(Reference reference) {
        if (!getConcept().allReferences().contains(reference)) {
            throw new IllegalArgumentException("Reference not belonging to this concept");
        }
        if (linkValues.containsKey(reference.getID())) {
            return linkValues.get(reference.getID());
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public void addReferredNode(Reference reference, Node referredNode) {
        if (reference.isMultiple()) {
            addLinkMultipleValue(reference, referredNode);
        } else {
            setLinkSingleValue(reference, referredNode);
        }
    }

    @Nullable
    @Override
    public String getID() {
        return id;
    }

    @Override
    public Model getModel() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Node getRoot() {
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
    public List<AnnotationInstance> getAnnotations() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Containment getContainmentFeature() {
        throw new UnsupportedOperationException();
    }

    @Nonnull
    @Override
    public List<AnnotationInstance> getAnnotations(Annotation annotation) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addAnnotation(AnnotationInstance instance) {
        throw new UnsupportedOperationException();
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DynamicNode that)) return false;
        return Objects.equals(id, that.id) && Objects.equals(parent, that.parent) && Objects.equals(concept, that.concept) && Objects.equals(propertyValues, that.propertyValues) && Objects.equals(linkValues, that.linkValues);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, parent, concept, propertyValues, linkValues);
    }
}

