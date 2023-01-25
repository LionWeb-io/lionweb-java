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
    private Map<String, ReferenceHandler> referenceHandlers;

    class ContainmentHandler {

    }

    protected interface PropertyGetter<T> {

        T getValue();
    }

    protected interface PropertySetter<T> {

        void setValue(T value);
    }

    protected interface PropertyHandler<T> extends PropertyGetter<T>, PropertySetter<T> {

    }

    protected interface ReferenceGetter<T extends Node> {

        List<T> getValue();
    }

    protected interface ReferenceSetter<T> {

        void addValue(T value);
    }

    protected interface ReferenceHandler<T extends Node> extends ReferenceGetter<T>, ReferenceSetter<T> {

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

    protected <T> void recordPropertyHandler(Property property, Class<T> clazz, PropertyGetter<Object> getter,
                                             PropertySetter<T> setter) {
        recordPropertyHandler(property, new PropertyHandler<Object>() {
            @Override
            public T getValue() {
                return clazz.cast(getter.getValue());
            }

            @Override
            public void setValue(Object value) {
                setter.setValue(clazz.cast(value));
            }
        });
    }

    protected void recordReferenceHandler(Reference reference, ReferenceHandler handler) {
        if (reference == null) {
            throw new IllegalStateException("Reference is null");
        }
        if (reference.getID() == null) {
            throw new IllegalStateException("No ID for reference " + reference);
        }
        referenceHandlers.put(reference.getID(), handler);
    }

    protected <T extends Node> void recordReferenceHandler(Reference reference, Class<T> clazz, ReferenceGetter<T> getter, ReferenceSetter<T> setter) {
        recordReferenceHandler(reference, new ReferenceHandler<T>() {
            @Override
            public void addValue(T value) {
                setter.addValue(value);
            }

            @Override
            public List<T> getValue() {
                return getter.getValue();
            }

        });
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
            referenceHandlers = new HashMap<>();
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
        ensureReflectionElementsAreInPlace();
        if (this.propertyHandlers.containsKey(property.getID())) {
            return this.propertyHandlers.get(property.getID()).getValue();    
        } else {
            throw new UnsupportedOperationException("Property " + property + " not supported");
        }
    }

    @Override
    public void setPropertyValue(Property property, Object value) {
        if (!getConcept().allProperties().contains(property)) {
            throw new IllegalArgumentException("Property not belonging to this concept");
        }
        ensureReflectionElementsAreInPlace();
        if (this.propertyHandlers.containsKey(property.getID())) {
            this.propertyHandlers.get(property.getID()).setValue(value);
        } else {
            throw new UnsupportedOperationException("Property " + property + " not supported");
        }
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
        ensureReflectionElementsAreInPlace();
        if (this.referenceHandlers.containsKey(reference.getID())) {
            return this.referenceHandlers.get(reference.getID()).getValue();
        } else {
            throw new UnsupportedOperationException("Reference " + reference + " not supported");
        }
    }

    @Override
    public void addReferredNode(Reference reference, Node referredNode) {
        if (!getConcept().allReferences().contains(reference)) {
            throw new IllegalArgumentException("Reference not belonging to this concept");
        }
        ensureReflectionElementsAreInPlace();
        if (this.referenceHandlers.containsKey(reference.getID())) {
            this.referenceHandlers.get(reference.getID()).addValue(referredNode);
        } else {
            throw new UnsupportedOperationException("Reference " + reference + " not supported");
        }
    }

    @Nullable
    @Override
    public String getID() {
        return id;
    }
}
