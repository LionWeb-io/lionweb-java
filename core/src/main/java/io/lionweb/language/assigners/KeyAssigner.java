package io.lionweb.language.assigners;

import io.lionweb.language.Language;
import javax.annotation.Nonnull;

/** Strategy that assigns stable keys to all elements in a {@link Language} that lack one. */
public interface KeyAssigner {
  void assignKeys(@Nonnull Language language);
}
