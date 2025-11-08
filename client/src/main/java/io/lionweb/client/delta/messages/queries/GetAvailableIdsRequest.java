package io.lionweb.client.delta.messages.queries;

import io.lionweb.client.delta.messages.DeltaQuery;
import org.jetbrains.annotations.NotNull;

public class GetAvailableIdsRequest extends DeltaQuery {
  public int count;

  public GetAvailableIdsRequest(@NotNull String queryId, int count) {
    super(queryId);
    if (count < 0) {
      throw new IllegalArgumentException("count must be non-negative");
    }
    this.count = count;
  }

  @Override
  public String toString() {
    return "GetAvailableIdsRequest{" + "count=" + count + ", queryId='" + queryId + '\'' + '}';
  }
}
