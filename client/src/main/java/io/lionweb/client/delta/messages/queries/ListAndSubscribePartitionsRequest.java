package io.lionweb.client.delta.messages.queries;

import io.lionweb.client.delta.messages.DeltaQuery;
import org.jetbrains.annotations.NotNull;

/** Request to both list existing partitions and subscribe to future partition changes. */
public class ListAndSubscribePartitionsRequest extends DeltaQuery {

  public ListAndSubscribePartitionsRequest(@NotNull String queryId) {
    super(queryId);
  }

  @Override
  public String toString() {
    return "ListAndSubscribePartitionsRequest{" + "queryId='" + queryId + '\'' + '}';
  }
}
