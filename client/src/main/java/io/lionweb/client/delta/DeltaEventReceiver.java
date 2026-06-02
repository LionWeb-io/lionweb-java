package io.lionweb.client.delta;

import io.lionweb.client.delta.messages.DeltaEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Handles incoming {@link io.lionweb.client.delta.messages.DeltaEvent} notifications from a delta
 * channel.
 */
public interface DeltaEventReceiver {

  void receiveEvent(@NotNull DeltaEvent event);
}
