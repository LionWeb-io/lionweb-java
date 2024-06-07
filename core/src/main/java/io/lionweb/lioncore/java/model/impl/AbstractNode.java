package io.lionweb.lioncore.java.model.impl;

import io.lionweb.lioncore.java.language.Concept;
import io.lionweb.lioncore.java.language.Containment;
import io.lionweb.lioncore.java.model.Node;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class AbstractNode extends AbstractClassifierInstance<Concept> implements Node {

  @Override
  public void setOnlyChildByContainmentName(@Nonnull String containmentName, @Nullable Node child) {
    Containment containment = this.getClassifier().requireContainmentByName(containmentName);
    if (containment.isMultiple()) {
      throw new IllegalArgumentException("Cannot invoke this method with a multiple containment");
    }
    List<? extends Node> children = this.getChildren(containment);
    if (children.size() > 1) {
      throw new IllegalStateException(
          "The node should not have multiple children under containment " + containment);
    }
    if (children.size() > 0) {
      removeChild(children.get(0));
    }
    addChild(containment, child);
  }
}
