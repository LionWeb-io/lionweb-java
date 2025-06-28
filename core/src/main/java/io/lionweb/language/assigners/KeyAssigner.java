package io.lionweb.language.assigners;

import io.lionweb.language.Language;
import javax.annotation.Nonnull;

public interface KeyAssigner {
  void assignKeys(@Nonnull Language language);
}
