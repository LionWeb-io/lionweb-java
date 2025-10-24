package io.lionweb.client.delta.messages.queries;

import io.lionweb.client.delta.messages.DeltaQuery;
import org.jetbrains.annotations.NotNull;

public class ListPartitions extends DeltaQuery {

  public ListPartitions(@NotNull String queryId) {
    super(queryId);
  }
}
