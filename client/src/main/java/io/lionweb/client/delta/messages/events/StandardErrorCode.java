package io.lionweb.client.delta.messages.events;

/**
 * Well-known error codes that may appear in {@link ErrorEvent} messages.
 *
 * @see <a href="https://lionweb.io/specification/delta/delta-api.html">LionWeb Delta API specification</a>
 */
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
