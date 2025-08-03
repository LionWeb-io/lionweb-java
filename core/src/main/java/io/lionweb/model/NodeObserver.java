package io.lionweb.model;

import io.lionweb.language.Containment;
import io.lionweb.language.Property;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface NodeObserver {
  void propertyChanged(
      @Nonnull Node node,
      @Nonnull Property property,
      @Nullable Object oldValue,
      @Nullable Object newValue);

  void childAdded(
      @Nonnull Node node, @Nonnull Containment containment, int index, @Nonnull Node newChild);

  void childRemoved(@Nonnull Node node, @Nonnull Containment containment, int index, @Nonnull Node removedChild);

  void annotationAdded(@Nonnull Node node);

  void annotationRemoved(@Nonnull Node node);

  void referenceValueAdded(@Nonnull Node node);

  void referenceValueChanged(@Nonnull Node node);

  void referenceValueRemoved(@Nonnull Node node);
}
