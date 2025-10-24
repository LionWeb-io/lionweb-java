package io.lionweb.client.delta;

public class CommandRejectException extends RuntimeException {
  public CommandRejectException(String message) {
    super(message);
  }
}
