package io.lionweb.client.delta.messages;

import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a key-value pair of additional information used within the Delta framework.
 *
 * <p>The `AdditionalInfoData` class is primarily designed to hold supplementary data as key-value
 * pairs. It is utilized in various Delta-related classes to support the attachment of arbitrary
 * metadata to protocol messages.
 */
public class AdditionalInfoData {
  public final @NotNull String key;
  public final @NotNull String value;

  public AdditionalInfoData(@NotNull String key, @NotNull String value) {
    Objects.requireNonNull(key, "key should not be null");
    Objects.requireNonNull(value, "value should not be null");
    this.key = key;
    this.value = value;
  }
}
