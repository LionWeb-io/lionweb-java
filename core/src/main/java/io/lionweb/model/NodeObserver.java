package io.lionweb.model;

import io.lionweb.language.Property;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface NodeObserver {
  void propertyChanged(@Nonnull Node node, @Nonnull Property property, @Nullable Object newValue);

  void childAdded(@Nonnull Node node);

  void childRemoved(@Nonnull Node node);

  void referenceValueAdded(@Nonnull Node node);

  void referenceValueChanged(@Nonnull Node node);

  void referenceValueRemoved(@Nonnull Node node);

  void parentChanged(@Nonnull Node node);
}
