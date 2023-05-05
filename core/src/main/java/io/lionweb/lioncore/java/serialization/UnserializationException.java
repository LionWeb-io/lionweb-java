package io.lionweb.lioncore.java.serialization;

public class UnserializationException extends RuntimeException {
  public UnserializationException(String message) {
    super("Problem during unserialization: " + message);
  }

  public UnserializationException(String message, UnserializationException e) {
    super("Problem during unserialization: " + message, e);
  }
}
