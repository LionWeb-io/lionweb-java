package io.lionweb.repoclient.api;

public enum HistorySupport {
  Disabled,
  Enabled;

  public static HistorySupport fromBoolean(boolean value) {
    return value ? Enabled : Disabled;
  }

  public boolean toBoolean() {
    return this == Enabled;
  }
}
