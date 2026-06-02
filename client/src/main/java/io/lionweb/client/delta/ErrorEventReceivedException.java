package io.lionweb.client.delta;

import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A custom exception that represents an error event received with a specific code and corresponding
 * error message. This exception extends {@code RuntimeException} and provides additional context
 * through code and errorMessage fields.
 */
public class ErrorEventReceivedException extends RuntimeException {
  private final @NotNull String code;
  private final @Nullable String errorMessage;

  public ErrorEventReceivedException(@NotNull String code, @Nullable String errorMessage) {
    super("code=" + code + " message=" + errorMessage);
    Objects.requireNonNull(code, "code must not be null");
    this.code = code;
    this.errorMessage = errorMessage;
  }

  public @NotNull String getCode() {
    return code;
  }

  public @Nullable String getErrorMessage() {
    return errorMessage;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    ErrorEventReceivedException that = (ErrorEventReceivedException) o;
    return Objects.equals(code, that.code) && Objects.equals(errorMessage, that.errorMessage);
  }

  @Override
  public int hashCode() {
    return Objects.hash(code, errorMessage);
  }
}
