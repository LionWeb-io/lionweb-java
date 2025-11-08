package io.lionweb.client.delta.messages.events;

public enum StandardErrorCode {
  INVALID_PARTICIPATION("invalidParticipation"),
  NODE_ALREADY_EXISTS("nodeAlreadyExists"),
  UNKNOWN_NODE("unknownNode"),
  UNKNOWN_INDEX("unknownIndex"),
  INDEX_NODE_MISMATCH("indexNodeMismatch"),
  MOVE_WITHOUT_PARENT("moveWithoutParent"),
  INVALID_MOVE("invalidMove"),
  UNDEFINED_REFERENCE_TARGET("undefinedReferenceTarget");

  public final String code;

  StandardErrorCode(String code) {
    this.code = code;
  }
}
