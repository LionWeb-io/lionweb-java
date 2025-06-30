package io.lionweb.language.assigners;

import io.lionweb.language.Language;
import javax.annotation.Nonnull;

public interface IDAssigner {
  void assignIDs(@Nonnull Language language);
}
