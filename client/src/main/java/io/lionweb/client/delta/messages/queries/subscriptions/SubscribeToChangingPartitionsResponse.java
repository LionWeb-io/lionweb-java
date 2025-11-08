package io.lionweb.client.delta.messages.queries.subscriptions;

import io.lionweb.client.delta.messages.DeltaQueryResponse;
import org.jetbrains.annotations.NotNull;

public class SubscribeToChangingPartitionsResponse extends DeltaQueryResponse {
  public SubscribeToChangingPartitionsResponse(@NotNull String queryId) {
    super(queryId);
  }

  @Override
  public String toString() {
    return "SubscribeToChangingPartitionsResponse{" + "queryId='" + queryId + '\'' + '}';
  }
}
