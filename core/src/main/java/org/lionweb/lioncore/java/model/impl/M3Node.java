package org.lionweb.lioncore.java.model.impl;

import org.lionweb.lioncore.java.metamodel.*;
import org.lionweb.lioncore.java.model.AnnotationInstance;
import org.lionweb.lioncore.java.model.Model;
import org.lionweb.lioncore.java.model.Node;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Base class to help implements Node in the metamodel package.
 *
 * Other libraries could implement Node differently, for example based on reflection. However this is outside
 * the scope of this library. This library should provide a solid, basic dependency to be used by other implementation
 * and it should be as reusable, basic, and unopinionated as possible.
 */
public abstract class M3Node<T extends M3Node> implements Node {
    private String id;
    private Node parent;

    // We use as keys of these maps the name of the features and not the IDs.
    // The reason why we do that, is to avoid a circular dependency as the classes for defining metamodel
    // elements are inheriting from this class.
    private Map<String, Object> propertyValues = new HashMap<>();
    private Map<String, List<Node>> linkValues = new HashMap<>();

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
        return propertyValues.get(property.getSimpleName());
    }

    protected <V> V getPropertyValue(String propertyName, Class<V> clazz) {
        Object value = propertyValues.get(propertyName);
        if (value == null) {
            return null;
        } else {
            return clazz.cast(value);
        }
    }

    @Override
    public void setPropertyValue(Property property, Object value) {
        if (!getConcept().allProperties().contains(property)) {
            throw new IllegalArgumentException("Property not belonging to this concept");
        }
        setPropertyValue(property.getSimpleName(), value);
    }

    protected void setPropertyValue(String propertyName, Object value) {
        propertyValues.put(propertyName, value);
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
        if (linkValues.containsKey(containment.getSimpleName())) {
            return linkValues.get(containment.getSimpleName());
        } else {
            return Collections.emptyList();
        }
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
        if (linkValues.containsKey(reference.getSimpleName())) {
            return linkValues.get(reference.getSimpleName());
        } else {
            return Collections.emptyList();
        }
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

    protected <V extends Node> V getLinkSingleValue(String linkName) {
//        Link link = getConcept().getLinkByName(linkName);
//        if (link == null) {
//            throw new IllegalArgumentException();
//        }
        if (linkValues.containsKey(linkName)) {
            List<Node> values = linkValues.get(linkName);
            if (values.size() == 0) {
                return null;
            } else if (values.size() == 1) {
                return (V)(values.get(0));
            } else {
                throw new IllegalStateException();
            }
        } else {
            return null;
        }
    }

    /*
     * This method could be invoked by the metamodel elements classes before the LionCore metamodel
     * has been built, therefore we cannot look for the definition of the features to verify they
     * exist. We instead just trust a link with that name to exist.
     */
    protected void setLinkSingleValue(String linkName, Node value, boolean containment) {
        if (containment) {
            List<Node> prevValue = linkValues.get(linkName);
            if (prevValue != null) {
                List<Node> copy = new LinkedList<>(prevValue);
                copy.forEach(c -> this.removeChild(c));
            }
        }
        if (value == null) {
            linkValues.remove(linkName);
        } else {
            if (containment) {
                //value.setParent(this);
            }
            linkValues.put(linkName, Arrays.asList(value));
        }
    }
}
