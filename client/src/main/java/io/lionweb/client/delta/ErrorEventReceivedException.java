package io.lionweb.client.delta;

import java.util.Objects;

public class ErrorEventReceivedException extends RuntimeException {
  private String code;
  private String errorMessage;

  public ErrorEventReceivedException(String code, String errorMessage) {
    super("code=" + code + " message=" + errorMessage);
    this.code = code;
    this.errorMessage = errorMessage;
  }

  public String getCode() {
    return code;
  }

  public String getErrorMessage() {
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
