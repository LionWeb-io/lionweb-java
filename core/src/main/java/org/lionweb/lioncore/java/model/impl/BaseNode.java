package org.lionweb.lioncore.java.model.impl;

import org.lionweb.lioncore.java.metamodel.Annotation;
import org.lionweb.lioncore.java.metamodel.Concept;
import org.lionweb.lioncore.java.metamodel.Containment;
import org.lionweb.lioncore.java.metamodel.Property;
import org.lionweb.lioncore.java.metamodel.Reference;
import org.lionweb.lioncore.java.model.AnnotationInstance;
import org.lionweb.lioncore.java.model.Model;
import org.lionweb.lioncore.java.model.Node;

import java.util.List;

public abstract class BaseNode implements Node {
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
        throw new UnsupportedOperationException();
    }

    @Override
    public Concept getConcept() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<AnnotationInstance> getAnnotations() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Containment getContainmentFeature() {
        throw new UnsupportedOperationException();
    }

    @Override
    public AnnotationInstance getAnnotation(Annotation annotation) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setAnnotation(AnnotationInstance instance) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getPropertyValue(Property property) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setPropertyValue(Property property, Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Node> getChildren() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Node> getChildren(Containment containment) {
        throw new UnsupportedOperationException();
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
        throw new UnsupportedOperationException();
    }

    @Override
    public void setReferredNode(Reference reference, Node referredNode) {
        throw new UnsupportedOperationException();
    }
}
