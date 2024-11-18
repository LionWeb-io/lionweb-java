package io.lionweb.lioncore.java;

import java.util.Objects;
import javax.annotation.Nonnull;

/**
 * A LionWeb Version. Note that the version is used to refer to the specifications but also to the
 * versions of LionCore and LionCore Builtins, as they should always be aligned.
 */
public enum LionWebVersion {
  v2023_1("2023.1"),
  v2024_1("2024.1");
  public static final LionWebVersion currentVersion = LionWebVersion.v2024_1;
  private @Nonnull String value;

  LionWebVersion(@Nonnull String value) {
    Objects.requireNonNull(value, "Value should not be null");
    this.value = value;
  }

  public static LionWebVersion fromValue(String serializationFormatVersion) {
    for (LionWebVersion lionWebVersion : values()) {
      if (lionWebVersion.getValue().equals(serializationFormatVersion)) {
        return lionWebVersion;
      }
    }
    throw new IllegalArgumentException(
        "Invalid serializationFormatVersion: " + serializationFormatVersion);
  }

  public @Nonnull String getValue() {
    return value;
  }
}
