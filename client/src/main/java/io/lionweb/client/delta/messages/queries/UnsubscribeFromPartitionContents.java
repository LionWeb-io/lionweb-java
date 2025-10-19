package io.lionweb.client.delta.messages.queries;

import io.lionweb.client.delta.messages.DeltaQuery;

public class UnsubscribeFromPartitionContents extends DeltaQuery {

  public final String partition;

  public UnsubscribeFromPartitionContents(String queryId, String partition) {
    super(queryId);
    this.partition = partition;
  }
}
