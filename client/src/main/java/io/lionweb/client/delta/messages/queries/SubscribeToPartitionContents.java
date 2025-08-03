package io.lionweb.client.delta.messages.queries;

import io.lionweb.client.delta.messages.DeltaQuery;
import org.jetbrains.annotations.NotNull;

public class SubscribeToPartitionContents extends DeltaQuery {

  /** TargetNode Node id of the partition this client wants to receive events of. */
  private String partition;

  public SubscribeToPartitionContents(@NotNull String queryId, String partition) {
    super(queryId);
    this.partition = partition;
  }
}
