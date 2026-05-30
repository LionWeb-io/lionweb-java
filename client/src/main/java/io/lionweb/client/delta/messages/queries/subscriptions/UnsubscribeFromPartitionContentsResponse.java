package io.lionweb.client.delta.messages.queries.subscriptions;

import io.lionweb.client.delta.messages.DeltaQueryResponse;
import org.jetbrains.annotations.NotNull;

/** Acknowledgement response confirming that the client has been unsubscribed from a partition. */
public class UnsubscribeFromPartitionContentsResponse extends DeltaQueryResponse {

  public UnsubscribeFromPartitionContentsResponse(@NotNull String queryId) {
    super(queryId);
  }

  @Override
  public String toString() {
    return "UnsubscribeFromPartitionContentsResponse{" + "queryId='" + queryId + '\'' + '}';
  }
}
