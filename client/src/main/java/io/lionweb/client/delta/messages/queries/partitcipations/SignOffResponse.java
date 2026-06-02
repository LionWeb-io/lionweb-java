package io.lionweb.client.delta.messages.queries.partitcipations;

import io.lionweb.client.delta.messages.DeltaQueryResponse;
import org.jetbrains.annotations.NotNull;

/** Response acknowledging a {@link SignOffRequest}. */
public class SignOffResponse extends DeltaQueryResponse {

  public SignOffResponse(@NotNull String queryId) {
    super(queryId);
  }

  @Override
  public String toString() {
    return "SignOffResponse{" + "queryId='" + queryId + '\'' + '}';
  }
}
