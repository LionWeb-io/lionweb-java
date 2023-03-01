package org.lionweb.lioncore.java.model.impl;

import org.lionweb.lioncore.java.metamodel.*;
import org.lionweb.lioncore.java.model.AnnotationInstance;
import org.lionweb.lioncore.java.model.Model;
import org.lionweb.lioncore.java.model.Node;
import org.lionweb.lioncore.java.model.ReferenceValue;

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
    private Map<String, List<Node>> containmentValues = new HashMap<>();

    private Map<String, List<ReferenceValue>> referenceValues = new HashMap<>();

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
        if (containmentValues.containsKey(containment.getID())) {
            return containmentValues.get(containment.getID());
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
            addContainment(containment, child);
        } else {
            setContainmentSingleValue(containment, child);
        }
    }

    private void setContainmentSingleValue(Containment link, Node value) {
        List<Node> prevValue = containmentValues.get(link.getID());
        if (prevValue != null) {
            List<Node> copy = new LinkedList<>(prevValue);
            copy.forEach(c -> this.removeChild(c));
        }
        if (value == null) {
            containmentValues.remove(link.getID());
        } else {
            ((DynamicNode)value).setParent(this);
            containmentValues.put(link.getID(), new ArrayList(Arrays.asList(value)));
        }
    }

    private void setReferenceSingleValue(Reference link, ReferenceValue value) {
        if (value == null) {
            referenceValues.remove(link.getID());
        } else {
            referenceValues.put(link.getID(), new ArrayList(Arrays.asList(value)));
        }
    }

    private void addContainment(Containment link, Node value) {
        assert link.isMultiple();
        ((DynamicNode)value).setParent(this);
        if (containmentValues.containsKey(link.getID())) {
            containmentValues.get(link.getID()).add(value);
        } else {
            containmentValues.put(link.getID(), new ArrayList(Arrays.asList(value)));
        }
    }

    private void addReferenceMultipleValue(Reference link, ReferenceValue referenceValue) {
        assert link.isMultiple();
        if (referenceValues.containsKey(link.getID())) {
            referenceValues.get(link.getID()).add(referenceValue);
        } else {
            referenceValues.put(link.getID(), new ArrayList(Arrays.asList(referenceValue)));
        }
    }

    @Override
    public void removeChild(Node node) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Node> getReferredNodes(Reference reference) {
        return getReferenceValues(reference).stream().map(v -> v.getReferred()).toList();
    }

    @Nonnull
    @Override
    public List<ReferenceValue> getReferenceValues(Reference reference) {
        if (!getConcept().allReferences().contains(reference)) {
            throw new IllegalArgumentException("Reference not belonging to this concept");
        }
        if (referenceValues.containsKey(reference.getID())) {
            return referenceValues.get(reference.getID());
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public void addReferredNode(Reference reference, @Nullable Node referredNode, @Nullable  String resolveInfo) {
        if (reference.isMultiple()) {
            addReferenceMultipleValue(reference, new ReferenceValue(referredNode, resolveInfo));
        } else {
            setReferenceSingleValue(reference, new ReferenceValue(referredNode, resolveInfo));
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
        if (this == o) {
            return true;
        }
        if (!(o instanceof DynamicNode)) {
            return false;
        }
        DynamicNode that = (DynamicNode) o;
        return Objects.equals(id, that.id) && Objects.equals(parent, that.parent) && Objects.equals(concept, that.concept) && Objects.equals(propertyValues, that.propertyValues) && Objects.equals(containmentValues, that.containmentValues);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, parent, concept, propertyValues, containmentValues);
    }
}

