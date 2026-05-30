package io.lionweb.client.delta.messages.queries;

import io.lionweb.client.delta.messages.DeltaQueryResponse;
import org.jetbrains.annotations.NotNull;

/** Error response indicating a query or operation has failed. */
public class ErrorResponse extends DeltaQueryResponse {
  /** Machine-readable error code identifying the type of error. */
  public String errorCode;

  /** Human-readable description of the error. */
  public String message;

  public ErrorResponse(@NotNull String queryId) {
    super(queryId);
  }

  @Override
  public String toString() {
    return "ErrorResponse{"
        + "errorCode='"
        + errorCode
        + '\''
        + ", message='"
        + message
        + '\''
        + ", queryId='"
        + queryId
        + '\''
        + '}';
  }
}
