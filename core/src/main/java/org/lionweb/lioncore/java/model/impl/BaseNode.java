package org.lionweb.lioncore.java.model.impl;

import org.lionweb.lioncore.java.metamodel.*;
import org.lionweb.lioncore.java.model.AnnotationInstance;
import org.lionweb.lioncore.java.model.Model;
import org.lionweb.lioncore.java.model.Node;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Base class to help implements Node in the metamodel package.
 *
 * Other libraries could implement Node differently, for example based on reflection. However this is outside
 * the scope of this library. This library should provide a solid, basic dependency to be used by other implementation
 * and it should be as reusable, basic, and unopinionated as possible.
 */
public abstract class BaseNode<T extends BaseNode> implements Node {
    private String id;
    private Node parent;
    private Map<String, PropertyHandler> propertyHandlers;
    private Map<String, ContainmentHandler> containmentHandlers;

    class ContainmentHandler {

    }

    interface PropertyHandler {

    }

    protected void recordPropertyHandler(Property property, PropertyHandler handler) {
        if (property == null) {
            throw new IllegalStateException("Property is null");
        }
        if (property.getID() == null) {
            throw new IllegalStateException("No ID for property " + property);
        }
        propertyHandlers.put(property.getID(), handler);
    }

    protected void recordContainmentHandler(Containment containment, ContainmentHandler handler) {
        if (containment == null) {
            throw new IllegalStateException("Containment is null");
        }
        if (containment.getID() == null) {
            throw new IllegalStateException("No ID for containment " + containment);
        }
        containmentHandlers.put(containment.getID(), handler);
    }

    private void ensureReflectionElementsAreInPlace() {
        assert (propertyHandlers == null) == (containmentHandlers == null);
        if (propertyHandlers == null) {
            propertyHandlers = new HashMap<>();
            containmentHandlers = new HashMap<>();
            registerReflectionElements();
        }
    }

    protected void registerReflectionElements() {

    }

    private List<AnnotationInstance> annotationInstances = new LinkedList<>();

    public T setID(String id) {
        this.id = id;
        return (T)this;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    @Override
    public Model getModel() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Node getRoot() {
        if (getParent() == null) {
            return this;
        }
        return getParent().getRoot();
    }

    @Override
    public Node getParent() {
        return parent;
    }

    @Override
    public List<AnnotationInstance> getAnnotations() {
        return this.annotationInstances;
    }

    @Override
    public Containment getContainmentFeature() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<AnnotationInstance> getAnnotations(Annotation annotation) {
        return annotationInstances.stream().filter(a -> a.getAnnotationDefinition() == annotation).collect(Collectors.toList());
    }

    @Override
    public void addAnnotation(AnnotationInstance instance) {
        annotationInstances.add(instance);
    }

    @Override
    public Object getPropertyValue(Property property) {
        if (!getConcept().allProperties().contains(property)) {
            throw new IllegalArgumentException("Property not belonging to this concept");
        }
        throw new UnsupportedOperationException("Property " + property + " not supported");
    }

    @Override
    public void setPropertyValue(Property property, Object value) {
        if (!getConcept().allProperties().contains(property)) {
            throw new IllegalArgumentException("Property not belonging to this concept");
        }
        throw new UnsupportedOperationException("Property " + property + " not supported");
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
        throw new UnsupportedOperationException("Containment " + containment + " not supported");
    }

    @Override
    public void addChild(Containment containment, Node child) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeChild(Node node) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Node> getReferredNodes(Reference reference) {
        if (!getConcept().allReferences().contains(reference)) {
            throw new IllegalArgumentException("Reference not belonging to this concept");
        }
        throw new UnsupportedOperationException("Reference " + reference + " not supported");
    }

    @Override
    public void addReferredNode(Reference reference, Node referredNode) {
        if (!getConcept().allReferences().contains(reference)) {
            throw new IllegalArgumentException("Reference not belonging to this concept");
        }
        throw new UnsupportedOperationException("Reference " + reference + " not supported");
    }

    @Nullable
    @Override
    public String getID() {
        return id;
    }
}
