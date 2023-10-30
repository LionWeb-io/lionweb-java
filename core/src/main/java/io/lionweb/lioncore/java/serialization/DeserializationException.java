package io.lionweb.lioncore.java.serialization;

public class DeserializationException extends RuntimeException {
  public DeserializationException(String message) {
    super("Problem during deserialization: " + message);
  }

  public DeserializationException(String message, DeserializationException e) {
    super("Problem during deserialization: " + message, e);
  }
}
