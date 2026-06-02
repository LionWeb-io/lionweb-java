package io.lionweb.serialization;

import java.util.Objects;
import javax.annotation.Nonnull;

/** Thrown when a serialized LionWeb payload cannot be parsed or mapped to model instances. */
public class DeserializationException extends RuntimeException {
  public DeserializationException(@Nonnull String message) {
    super(
        "Problem during deserialization: "
            + Objects.requireNonNull(message, "message cannot be null"));
  }

  public DeserializationException(@Nonnull String message, @Nonnull DeserializationException e) {
    super(
        "Problem during deserialization: "
            + Objects.requireNonNull(message, "message cannot be null"),
        Objects.requireNonNull(e, "e cannot be null"));
  }
}
