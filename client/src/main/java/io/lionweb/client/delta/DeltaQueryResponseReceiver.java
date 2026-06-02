package io.lionweb.client.delta;

import io.lionweb.client.delta.messages.DeltaQueryResponse;
import org.jetbrains.annotations.NotNull;

/** Handles incoming {@link io.lionweb.client.delta.messages.DeltaQueryResponse} messages from a delta channel. */
public interface DeltaQueryResponseReceiver {

  void receiveQueryResponse(@NotNull DeltaQueryResponse queryResponse);
}
