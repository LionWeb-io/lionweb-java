package io.lionweb.model;

import javax.annotation.Nullable;

public class GenericReferenceValue extends ReferenceValue<Node> {
  public GenericReferenceValue() {
    super();
  }

  public GenericReferenceValue(@Nullable Node referred, @Nullable String resolveInfo) {
    super(referred, resolveInfo);
  }
}
