package io.lionweb.serialization;

import io.lionweb.language.Containment;
import io.lionweb.language.Property;
import io.lionweb.language.Reference;
import io.lionweb.model.Node;
import io.lionweb.model.ReferenceValue;
import io.lionweb.model.impl.AbstractNode;
import java.util.List;
import java.util.Random;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class SimpleNode extends AbstractNode {

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
  public Node getParent() {
    return parent;
  }

  @Override
  public Containment getContainmentFeature() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object getPropertyValue(@Nonnull Property property) {
    if (!getClassifier().allProperties().contains(property)) {
      throw new IllegalArgumentException("Property not belonging to this concept");
    }
    return concreteGetPropertyValue(property);
  }

  protected Object concreteGetPropertyValue(Property property) {
    throw new UnsupportedOperationException("Property " + property + " not yet supported");
  }

  @Override
  public void setPropertyValue(Property property, @Nullable Object value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<? extends Node> getChildren(@Nonnull Containment containment) {
    if (!getClassifier().allContainments().contains(containment)) {
      throw new IllegalArgumentException("Containment not belonging to this concept");
    }
    return concreteGetChildren(containment);
  }

  protected List<? extends Node> concreteGetChildren(Containment containment) {
    throw new UnsupportedOperationException("Containment " + containment + " not yet supported");
  }

  @Override
  public void addChild(@Nonnull Containment containment, @Nonnull Node child) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void removeChild(Node node) {
    throw new UnsupportedOperationException();
  }

  @Nonnull
  @Override
  public List<ReferenceValue> getReferenceValues(@Nonnull Reference reference) {
    if (!getClassifier().allReferences().contains(reference)) {
      throw new IllegalArgumentException("Reference not belonging to this concept");
    }
    return concreteGetReferenceValues(reference);
  }

  protected List<ReferenceValue> concreteGetReferenceValues(Reference reference) {
    throw new UnsupportedOperationException("Reference " + reference + " not yet supported");
  }

  @Override
  public int addReferenceValue(
      @Nonnull Reference reference, @Nullable ReferenceValue referredNode) {
    if (!getClassifier().allReferences().contains(reference)) {
      throw new IllegalArgumentException("Reference not belonging to this concept");
    }
    return concreteAddReferenceValue(reference, referredNode);
  }

  public int concreteAddReferenceValue(
      @Nonnull Reference reference, @Nullable ReferenceValue referredNode) {
    throw new UnsupportedOperationException("Reference " + reference + " not yet supported");
  }

  @Override
  public void setReferred(@Nonnull Reference reference, int index, @Nullable Node referredNode) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setResolveInfo(
      @Nonnull Reference reference, int index, @Nullable String resolveInfo) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Node getRoot() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void removeChild(@Nonnull Containment containment, int index) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void removeReferenceValue(
      @Nonnull Reference reference, @Nullable ReferenceValue referenceValue) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void removeReferenceValue(@Nonnull Reference reference, int index) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setReferenceValues(
      @Nonnull Reference reference, @Nonnull List<? extends ReferenceValue> values) {
    throw new UnsupportedOperationException();
  }
}
