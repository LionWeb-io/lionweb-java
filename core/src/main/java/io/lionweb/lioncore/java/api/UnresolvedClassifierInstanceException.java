package io.lionweb.lioncore.java.api;

public class UnresolvedClassifierInstanceException extends RuntimeException {
  private String instanceID;

  public String getInstanceID() {
    return instanceID;
  }

  public UnresolvedClassifierInstanceException(String instanceID) {
    super("Unable to resolve classifier instance with ID=" + instanceID);
  }
}
