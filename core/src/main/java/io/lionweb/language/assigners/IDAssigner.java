package io.lionweb.language.assigners;

import io.lionweb.language.Language;
import javax.annotation.Nonnull;

/** Strategy that assigns unique IDs to all elements in a {@link Language} that lack one. */
public interface IDAssigner {
  void assignIDs(@Nonnull Language language);
}
