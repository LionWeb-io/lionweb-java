package io.lionweb.client.delta.messages.queries.partitcipations;

import io.lionweb.client.delta.messages.DeltaQueryResponse;
import org.jetbrains.annotations.NotNull;

public class SignOnResponse extends DeltaQueryResponse {

  public SignOnResponse(@NotNull String queryId) {
    super(queryId);
  }
}
