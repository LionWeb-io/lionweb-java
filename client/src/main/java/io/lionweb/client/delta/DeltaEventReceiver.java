package io.lionweb.client.delta;

import io.lionweb.client.delta.messages.DeltaEvent;

public interface DeltaEventReceiver {

  void receiveEvent(DeltaEvent event);
}
