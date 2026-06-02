package io.lionweb.language;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Any element in a Language (M2) that can be referred from an instance (M1).
 *
 * @param <T> Type of keyed element.
 */
public interface IKeyed<T> extends INamed {
  @Nullable
  String getKey();

  @Nonnull
  T setKey(@Nullable String value);
}
