package io.lionweb.client.delta;

import io.lionweb.client.delta.messages.DeltaQuery;
import io.lionweb.client.delta.messages.DeltaQueryResponse;
import org.jetbrains.annotations.NotNull;

public interface DeltaQueryReceiver {

  @NotNull
  DeltaQueryResponse receiveQuery(@NotNull DeltaQuery query);
}
