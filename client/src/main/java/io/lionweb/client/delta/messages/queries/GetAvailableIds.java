package io.lionweb.client.delta.messages.queries;

import io.lionweb.client.delta.messages.DeltaQuery;
import org.jetbrains.annotations.NotNull;

public class GetAvailableIds extends DeltaQuery {
  public int count;

  public GetAvailableIds(@NotNull String queryId, int count) {
    super(queryId);
    this.count = count;
  }
}
