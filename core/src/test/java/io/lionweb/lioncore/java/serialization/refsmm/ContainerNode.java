package io.lionweb.lioncore.java.serialization.refsmm;

import io.lionweb.lioncore.java.language.Concept;
import io.lionweb.lioncore.java.language.Containment;
import io.lionweb.lioncore.java.model.Node;
import io.lionweb.lioncore.java.serialization.SimpleNode;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ContainerNode extends SimpleNode {
  private ContainerNode contained;

  public ContainerNode() {
    this.contained = null;
    assignRandomID();
  }

  public ContainerNode(ContainerNode contained) {
    this.contained = contained;
    assignRandomID();
  }

  public ContainerNode(ContainerNode contained, String id) {
    this.contained = contained;
    setId(id);
  }

  @Override
  public Concept getClassifier() {
    return RefsLanguage.CONTAINER_NODE;
  }

  @Override
  protected List<? extends Node> concreteGetChildren(Containment containment) {
    if (containment.getName().equals("contained")) {
      return Arrays.asList(contained);
    }
    return super.concreteGetChildren(containment);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ContainerNode)) return false;
    ContainerNode that = (ContainerNode) o;
    return Objects.equals(contained, that.contained);
  }

  @Override
  public int hashCode() {
    return Objects.hash(contained);
  }

  @Override
  public String toString() {
    return "ContainerNode{" + "contained=" + contained.getID() + '}';
  }

  public ContainerNode getContained() {
    return contained;
  }

  public void setContained(ContainerNode contained) {
    this.contained = contained;
  }
}
