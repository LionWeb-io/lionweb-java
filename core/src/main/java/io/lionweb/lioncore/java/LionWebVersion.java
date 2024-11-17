package io.lionweb.lioncore.java;

public enum LionWebVersion {
  v2023_1("2023.1"),
  v2024_1("2024.1");
  public static final LionWebVersion currentVersion = LionWebVersion.v2024_1;
  private String value;

  LionWebVersion(String value) {
    this.value = value;
  }

  public static LionWebVersion fromValue(String serializationFormatVersion) {
    for (LionWebVersion lionWebVersion : values()) {
      if (lionWebVersion.getValue().equals(serializationFormatVersion)) {
        return lionWebVersion;
      }
    }
    throw new IllegalArgumentException("Invalid serializationFormatVersion: " + serializationFormatVersion);
  }

  public String getValue() {
    return value;
  }
}
