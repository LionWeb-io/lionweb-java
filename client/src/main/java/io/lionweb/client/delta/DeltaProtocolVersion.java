package io.lionweb.client.delta;

import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * Represents various versions of the Delta protocol. Each version is associated with a specific
 * year and minor version, denoted as major.minor (e.g., 2026.1).
 *
 * <p>The enum provides methods to convert between the wire-format string representation and the
 * corresponding enum constant.
 */
public enum DeltaProtocolVersion {
  v2025_1,
  v2026_1;

  /** Returns the wire-format string for this version (e.g. {@code "2026.1"}). */
  public @NotNull String toWireString() {
    // Enum name pattern: v{YEAR}_{MINOR} → "{YEAR}.{MINOR}"
    return name().substring(1).replace('_', '.');
  }

  /**
   * Parses a wire-format string (e.g. {@code "2026.1"}) to the corresponding enum constant.
   *
   * <p>Some client implementations send their LionWeb metamodel version here instead of an actual
   * Delta Protocol version (this field is otherwise unused by this implementation), so unknown
   * values fall back to the latest known protocol version rather than rejecting the connection.
   */
  public static @NotNull DeltaProtocolVersion fromWireString(@NotNull String wire) {
    Objects.requireNonNull(wire, "wire must not be null");
    String enumName = "v" + wire.replace('.', '_');
    try {
      return DeltaProtocolVersion.valueOf(enumName);
    } catch (IllegalArgumentException e) {
      return DeltaProtocolVersion.v2026_1;
    }
  }
}
