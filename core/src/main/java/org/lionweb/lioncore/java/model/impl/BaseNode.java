package org.lionweb.lioncore.java.model.impl;

import org.lionweb.lioncore.java.metamodel.Annotation;
import org.lionweb.lioncore.java.metamodel.Concept;
import org.lionweb.lioncore.java.metamodel.Containment;
import org.lionweb.lioncore.java.metamodel.FeaturesContainer;
import org.lionweb.lioncore.java.metamodel.Property;
import org.lionweb.lioncore.java.metamodel.Reference;
import org.lionweb.lioncore.java.model.AnnotationInstance;
import org.lionweb.lioncore.java.model.Model;
import org.lionweb.lioncore.java.model.Node;
import org.lionweb.lioncore.java.self.LionCore;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class BaseNode implements Node {
    private Node parent;
    private Concept concept;
    private List<AnnotationInstance> annotationInstances = new LinkedList<>();

    public void setParent(Node parent) {
        this.parent = parent;
    }

    protected void setConcept(Concept concept) {
        this.concept = concept;
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
    public Concept getConcept() {
        return concept;
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
    public Node getReferredNode(Reference reference) {
        if (!getConcept().allReferences().contains(reference)) {
            throw new IllegalArgumentException("Reference not belonging to this concept");
        }
        throw new UnsupportedOperationException("Reference " + reference + " not supported");
    }

    @Override
    public void setReferredNode(Reference reference, Node referredNode) {
        if (!getConcept().allReferences().contains(reference)) {
            throw new IllegalArgumentException("Reference not belonging to this concept");
        }
        throw new UnsupportedOperationException("Reference " + reference + " not supported");
    }
}