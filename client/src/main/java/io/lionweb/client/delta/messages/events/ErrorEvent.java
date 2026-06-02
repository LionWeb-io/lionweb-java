package io.lionweb.client.delta.messages.events;

import io.lionweb.client.delta.messages.BaseDeltaEvent;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Delta event signalling that an error occurred during command processing.
 *
 * @see <a href="https://lionweb.io/specification/delta/delta-api.html">LionWeb Delta API
 *     specification</a>
 */
public class ErrorEvent extends BaseDeltaEvent<ErrorEvent> {
  public @NotNull String errorCode;
  public @Nullable String message;

  public ErrorEvent(int sequenceNumber, @NotNull String errorCode, @Nullable String message) {
    super(sequenceNumber);
    Objects.requireNonNull(errorCode, "errorCode should not be null");
    this.errorCode = errorCode;
    this.message = message;
  }

  public ErrorEvent(
      int sequenceNumber, @NotNull StandardErrorCode standardErrorCode, @Nullable String message) {
    this(
        sequenceNumber,
        Objects.requireNonNull(standardErrorCode, "standardErrorCode should not be null").code,
        message);
  }

  @Override
  public String toString() {
    return "ErrorEvent{" + "errorCode='" + errorCode + '\'' + ", message='" + message + '\'' + '}';
  }
}
