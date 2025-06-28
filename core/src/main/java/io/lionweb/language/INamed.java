package io.lionweb.language;

import javax.annotation.Nullable;

public interface INamed {
  /**
   * This value may be null either because the element can have an optional name or because
   * something that should be named is an incorrect state (perhaps because it is being loaded). In
   * both cases the name could appear null.
   */
  @Nullable
  String getName();
}
