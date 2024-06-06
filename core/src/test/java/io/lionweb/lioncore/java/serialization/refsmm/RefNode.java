package io.lionweb.lioncore.java.serialization.refsmm;

import io.lionweb.lioncore.java.language.Concept;
import io.lionweb.lioncore.java.language.Reference;
import io.lionweb.lioncore.java.model.ReferenceValue;
import io.lionweb.lioncore.java.serialization.SimpleNode;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RefNode extends SimpleNode {
  private RefNode referred;

  public RefNode() {
    assignRandomID();
  }

  public void setReferred(RefNode referred) {
    this.referred = referred;
  }

  public RefNode(String id) {
    setId(id);
  }

  @Override
  public Concept getClassifier() {
    return RefsLanguage.REF_NODE;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof RefNode)) return false;
    RefNode that = (RefNode) o;
    return Objects.equals(referred.getID(), that.referred.getID());
  }

  @Override
  public int hashCode() {
    return Objects.hash(referred.getID());
  }

  @Override
  public String toString() {
    return "RefNode{" + "referred=" + referred.getID() + '}';
  }

  @Override
  protected List<ReferenceValue> concreteGetReferenceValues(Reference reference) {
    if (reference.getName().equals("referred")) {
      if (referred == null) {
        return Collections.emptyList();
      }
      return Collections.singletonList(new ReferenceValue(referred, ""));
    }
    return super.concreteGetReferenceValues(reference);
  }

  @Override
  public void concreteAddReferenceValue(
      @Nonnull Reference reference, @Nullable ReferenceValue referredNode) {
    if (reference.getName().equals("referred")) {
      referred = (RefNode) referredNode.getReferred();
      return;
    }
    super.concreteAddReferenceValue(reference, referredNode);
  }
}
