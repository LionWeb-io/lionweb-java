package io.lionweb.client.delta.messages.queries.partitcipations;

import io.lionweb.client.delta.messages.DeltaQuery;
import org.jetbrains.annotations.NotNull;

public class SignOff extends DeltaQuery {

  public SignOff(@NotNull String queryId) {
    super(queryId);
  }
}
