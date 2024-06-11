package io.lionweb.lioncore.java.model.impl;

import io.lionweb.lioncore.java.language.*;
import io.lionweb.lioncore.java.model.*;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This is basic an ID holder adapted as a Node. It is used as a placeholder to indicate that we
 * know which Node should be used in a particular point, but at this time we cannot/do not want to
 * retrieve the data necessary to properly instantiate it.
 */
public class ProxyNode extends AbstractClassifierInstance<Concept> implements Node {

  private @Nonnull String id;

  public ProxyNode(@Nonnull String id) {
    Objects.requireNonNull(id, "The node ID of a ProxyNode should not be null");
    this.id = id;
  }

  @Override
  public Node getParent() {
    throw cannotDoBecauseProxy();
  }

  @Override
  public Object getPropertyValue(Property property) {
    throw cannotDoBecauseProxy();
  }

  @Override
  public void setPropertyValue(Property property, Object value) {
    throw cannotDoBecauseProxy();
  }

  @Override
  public List<? extends Node> getChildren(Containment containment) {
    throw cannotDoBecauseProxy();
  }

  @Override
  public void addChild(Containment containment, Node child) {
    throw cannotDoBecauseProxy();
  }

  @Override
  public void removeChild(Node node) {
    throw cannotDoBecauseProxy();
  }

  @Nonnull
  @Override
  public List<ReferenceValue> getReferenceValues(@Nonnull Reference reference) {
    throw cannotDoBecauseProxy();
  }

  @Override
  public void addReferenceValue(
      @Nonnull Reference reference, @Nullable ReferenceValue referredNode) {
    throw cannotDoBecauseProxy();
  }

  @Nonnull
  @Override
  public String getID() {
    return id;
  }

  @Override
  public Concept getClassifier() {
    throw cannotDoBecauseProxy();
  }

  @Override
  public List<AnnotationInstance> getAnnotations() {
    throw cannotDoBecauseProxy();
  }

  @Override
  public Containment getContainmentFeature() {
    throw cannotDoBecauseProxy();
  }

  private CannotDoBecauseProxyException cannotDoBecauseProxy() {
    return new CannotDoBecauseProxyException(this.id);
  }

  /** Exception thrown when invoking most methods of a ProxyNode. */
  public class CannotDoBecauseProxyException extends IllegalStateException {
    private @Nonnull String nodeID;

    private CannotDoBecauseProxyException(@Nonnull String nodeID) {
      super(
          "Replace the proxy node with a real node to perform this operation (nodeID: "
              + nodeID
              + ")");
    }

    public @Nonnull String getNodeID() {
      return this.nodeID;
    }
  }

  @Override
  public String toString() {
    return "ProxyNode(" + id + ")";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ProxyNode)) return false;
    ProxyNode proxyNode = (ProxyNode) o;
    return Objects.equals(id, proxyNode.id);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }

  @Override
  public void removeChild(@Nonnull Containment containment, int index) {
    throw cannotDoBecauseProxy();
  }

  @Override
  public void removeReferenceValue(
      @Nonnull Reference reference, @Nullable ReferenceValue referenceValue) {
    throw cannotDoBecauseProxy();
  }

  @Override
  public void removeReferenceValue(@Nonnull Reference reference, int index) {
    throw cannotDoBecauseProxy();
  }

  @Override
  public void setReferenceValues(
      @Nonnull Reference reference, @Nonnull List<? extends ReferenceValue> values) {
    throw cannotDoBecauseProxy();
  }
}
