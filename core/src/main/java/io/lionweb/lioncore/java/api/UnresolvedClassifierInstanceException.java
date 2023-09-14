package io.lionweb.lioncore.java.api;

public class UnresolvedClassifierInstanceException extends RuntimeException {
  private String nodeID;

  public String getNodeID() {
    return nodeID;
  }

  public UnresolvedClassifierInstanceException(String nodeID) {
    super("Unable to resolve node with ID=" + nodeID);
  }
}
