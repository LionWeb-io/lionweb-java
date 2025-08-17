package io.lionweb.model;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface ReferenceValueObserver {
  void resolveInfoChanged(
      @Nonnull ReferenceValue referenceValue, @Nullable String oldValue, @Nullable String newValue);

  void referredIDChanged(
      @Nonnull ReferenceValue referenceValue, @Nullable String oldValue, @Nullable String newValue);
}
