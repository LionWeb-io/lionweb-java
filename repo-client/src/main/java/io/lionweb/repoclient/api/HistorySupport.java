package io.lionweb.repoclient.api;

public enum HistorySupport {
  DISABLED,
  ENABLED;

  public static HistorySupport fromBoolean(boolean value) {
    return value ? ENABLED : DISABLED;
  }

  public boolean toBoolean() {
    return this == ENABLED;
  }
}
