package io.lionweb.client.delta;

import io.lionweb.client.delta.messages.DeltaEvent;
import org.jetbrains.annotations.NotNull;

public interface DeltaEventReceiver {

  void receiveEvent(@NotNull DeltaEvent event);
}
