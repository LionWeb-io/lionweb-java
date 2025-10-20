package io.lionweb.client.delta;

import io.lionweb.client.delta.messages.CommonDeltaEvent;

public interface DeltaEventReceiver {

  void receiveEvent(CommonDeltaEvent event);
}
