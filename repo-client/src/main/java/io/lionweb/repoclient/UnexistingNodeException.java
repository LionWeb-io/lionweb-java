package io.lionweb.repoclient;

public class UnexistingNodeException extends RuntimeException {
  private final String nodeID;

  public UnexistingNodeException(String nodeID) {
    super("Unexisting node " + nodeID);
    this.nodeID = nodeID;
  }

  public UnexistingNodeException(String nodeID, Throwable cause) {
    super("Unexisting node " + nodeID, cause);
    this.nodeID = nodeID;
  }

  public String getNodeID() {
    return nodeID;
  }
}
