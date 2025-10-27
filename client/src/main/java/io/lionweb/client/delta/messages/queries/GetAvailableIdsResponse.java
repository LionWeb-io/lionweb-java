package io.lionweb.client.delta.messages.queries;

import io.lionweb.client.delta.messages.DeltaQueryResponse;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public class GetAvailableIdsResponse extends DeltaQueryResponse {

  private final @NotNull String[] ids;

  public GetAvailableIdsResponse(@NotNull String queryId, @NotNull String[] ids) {
    super(queryId);
    Objects.requireNonNull(ids, "ids must not be null");
    this.ids = ids;
  }
}
