package io.lionweb.lioncore.java.api;

public class UnresolvedNodeException extends RuntimeException {
  private String nodeID;

  public String getNodeID() {
    return nodeID;
  }

  public UnresolvedNodeException(String nodeID) {
    super("Unable to resolve node with ID=" + nodeID);
  }
}
