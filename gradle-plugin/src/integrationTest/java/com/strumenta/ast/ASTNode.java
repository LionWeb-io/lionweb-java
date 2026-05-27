package com.strumenta.ast;

import io.lionweb.language.*;
import io.lionweb.model.*;
import io.lionweb.model.impl.AbstractNode;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Stub base class used by integration tests.
 *
 * <p>The db2sql language generator maps StarLasu's ASTNode concept to this class. Generated db2sql
 * node classes extend it; all feature dispatch delegates through their own generated methods to
 * super, so the "not found" throws here are the correct terminal case.
 *
 * <p>Extends AbstractNode so that the generated subclasses inherit `partitionObserverCache`
 * (declared in AbstractClassifierInstance), which the generated setters and addTo* methods
 * reference.
 */
public abstract class ASTNode extends AbstractNode implements HasSettableParent {

  private String id;
  private ClassifierInstance<?> parent;

  protected ASTNode(String id) {
    this.id = id;
  }

  @Override
  public String getID() {
    return id;
  }

  @Override
  public ClassifierInstance<?> getParent() {
    return parent;
  }

  @Override
  public ClassifierInstance<?> setParent(@Nullable ClassifierInstance<?> parent) {
    this.parent = parent;
    return this;
  }

  @Override
  public Concept getClassifier() {
    throw new UnsupportedOperationException("Stub ASTNode: subclass must override getClassifier()");
  }

  @Override
  public Object getPropertyValue(Property property) {
    throw new IllegalStateException("Property " + property + " not found in ASTNode.");
  }

  @Override
  public void setPropertyValue(Property property, Object value) {
    throw new IllegalStateException("Property " + property + " not found in ASTNode.");
  }

  @Override
  @Nonnull
  public List<? extends Node> getChildren(@Nonnull Containment containment) {
    throw new IllegalStateException("Containment " + containment + " not found in ASTNode.");
  }

  @Override
  public void addChild(@Nonnull Containment containment, @Nonnull Node child) {
    throw new IllegalStateException("Containment " + containment + " not found in ASTNode.");
  }

  @Override
  public void addChild(@Nonnull Containment containment, @Nonnull Node child, int index) {
    throw new IllegalStateException("Containment " + containment + " not found in ASTNode.");
  }

  @Override
  @Nonnull
  public List<ReferenceValue> getReferenceValues(@Nonnull Reference reference) {
    throw new IllegalStateException("Reference " + reference + " not found in ASTNode.");
  }

  @Override
  public int addReferenceValue(
      @Nonnull Reference reference, @Nullable ReferenceValue referredNode) {
    throw new IllegalStateException("Reference " + reference + " not found in ASTNode.");
  }

  @Override
  public int addReferenceValue(
      @Nonnull Reference reference, int index, @Nullable ReferenceValue referredNode) {
    throw new IllegalStateException("Reference " + reference + " not found in ASTNode.");
  }

  @Override
  public void setReferenceValues(
      @Nonnull Reference reference, @Nonnull List<? extends ReferenceValue> values) {
    throw new IllegalStateException("Reference " + reference + " not found in ASTNode.");
  }

  @Override
  public void setReferred(@Nonnull Reference reference, int index, @Nullable Node referredNode) {
    throw new IllegalStateException("Reference " + reference + " not found in ASTNode.");
  }

  @Override
  public void setResolveInfo(
      @Nonnull Reference reference, int index, @Nullable String resolveInfo) {
    throw new IllegalStateException("Reference " + reference + " not found in ASTNode.");
  }
}
