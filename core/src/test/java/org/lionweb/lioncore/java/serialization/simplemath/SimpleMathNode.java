package org.lionweb.lioncore.java.serialization.simplemath;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.lionweb.lioncore.java.metamodel.*;
import org.lionweb.lioncore.java.model.AnnotationInstance;
import org.lionweb.lioncore.java.model.Model;
import org.lionweb.lioncore.java.model.Node;
import org.lionweb.lioncore.java.model.ReferenceValue;

public abstract class SimpleMathNode implements Node {

  private String id;
  private Node parent;

  protected void assignRandomID() {
    String randomId = "id_" + Math.abs(new Random().nextLong());
    setId(randomId);
  }

  public void setId(String id) {
    this.id = id;
  }

  public void setParent(Node parent) {
    this.parent = parent;
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
  public Node getParent() {
    return parent;
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

  @Override
  public Object getPropertyValueByName(String propertyName) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setPropertyValueByName(String propertyName, Object value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object getPropertyValueByID(String propertyID) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object getPropertyValue(Property property) {
    if (!getConcept().allProperties().contains(property)) {
      throw new IllegalArgumentException("Property not belonging to this concept");
    }
    return concreteGetPropertyValue(property);
  }

  protected Object concreteGetPropertyValue(Property property) {
    throw new UnsupportedOperationException("Property " + property + " not yet supported");
  }

  @Override
  public void setPropertyValue(Property property, Object value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<? extends Node> getChildren(Containment containment) {
    if (!getConcept().allContainments().contains(containment)) {
      throw new IllegalArgumentException("Containment not belonging to this concept");
    }
    return concreteGetChildren(containment);
  }

  protected List<? extends Node> concreteGetChildren(Containment containment) {
    throw new UnsupportedOperationException("Containment " + containment + " not yet supported");
  }

  @Override
  public void addChild(Containment containment, Node child) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void removeChild(Node node) {
    throw new UnsupportedOperationException();
  }

  @Nonnull
  @Override
  public List<Node> getReferredNodes(@Nonnull Reference reference) {
    throw new UnsupportedOperationException();
  }

  @Nonnull
  @Override
  public List<ReferenceValue> getReferenceValues(@Nonnull Reference reference) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void addReferenceValue(
      @Nonnull Reference reference, @Nullable ReferenceValue referredNode) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Node getRoot() {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<Node> getChildren() {
    List<Node> allChildren = new LinkedList<>();
    getConcept().allContainments().stream()
        .map(c -> getChildren(c))
        .forEach(children -> allChildren.addAll(children));
    return allChildren;
  }
}
