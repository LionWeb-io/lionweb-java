package io.lionweb.repoclient.api;

import org.jetbrains.annotations.NotNull;

public enum HistorySupport {
  DISABLED,
  ENABLED;

  public static @NotNull HistorySupport fromBoolean(boolean value) {
    return value ? ENABLED : DISABLED;
  }

  public boolean toBoolean() {
    return this == ENABLED;
  }
}
