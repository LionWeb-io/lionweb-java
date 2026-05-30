package io.lionweb.client.delta.messages;

import com.google.gson.JsonElement;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * Represents additional information associated with a protocol message in the Delta framework. This
 * class allows the attachment of metadata or supplementary data to messages.
 *
 * <p>Instances of this class can describe various types of additional information through its
 * properties, which include a type identifier, optional flags, a message, and a list of associated
 * key-value pairs.
 */
public class AdditionalInfo {
  public final @NotNull String kind;

  /** Optional — present in some messages, absent in others. */
  public final boolean distribute;

  public final @NotNull String message;
  public final @NotNull JsonElement data;

  public AdditionalInfo(
      @NotNull String kind,
      boolean distribute,
      @NotNull String message,
      @NotNull JsonElement data) {
    Objects.requireNonNull(kind, "kind must not be null");
    Objects.requireNonNull(message, "message must not be null");
    Objects.requireNonNull(data, "data must not be null");
    this.kind = kind;
    this.distribute = distribute;
    this.message = message;
    this.data = data;
  }
}
