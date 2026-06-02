package io.lionweb.client.delta;

import io.lionweb.client.delta.messages.DeltaQueryResponse;
import org.jetbrains.annotations.NotNull;

public interface DeltaQueryResponseReceiver {

  void receiveQueryResponse(@NotNull DeltaQueryResponse queryResponse);
}
