package io.lionweb.client.delta.messages.queries;

import io.lionweb.client.delta.messages.DeltaQuery;
import org.jetbrains.annotations.NotNull;

public class ListPartitionsRequest extends DeltaQuery {

  public ListPartitionsRequest(@NotNull String queryId) {
    super(queryId);
  }

  @Override
  public String toString() {
    return "ListPartitionsRequest{" + "queryId='" + queryId + '\'' + '}';
  }
}
