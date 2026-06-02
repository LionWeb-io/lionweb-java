package io.lionweb.serialization;

/** Thrown when a serialized LionWeb payload cannot be parsed or mapped to model instances. */
public class DeserializationException extends RuntimeException {
  public DeserializationException(String message) {
    super("Problem during deserialization: " + message);
  }

  public DeserializationException(String message, DeserializationException e) {
    super("Problem during deserialization: " + message, e);
  }
}
