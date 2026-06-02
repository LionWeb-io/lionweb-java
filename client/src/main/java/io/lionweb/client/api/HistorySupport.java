package io.lionweb.client.api;

import org.jetbrains.annotations.NotNull;

/**
 * Enum representing the support status for history-related functionality. The two possible states,
 * ENABLED and DISABLED, indicate whether history support is activated or not.
 */
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
