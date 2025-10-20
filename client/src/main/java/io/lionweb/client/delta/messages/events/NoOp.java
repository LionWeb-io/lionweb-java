package io.lionweb.client.delta.messages.events;

import io.lionweb.client.delta.messages.CommonDeltaEvent;

public class NoOp extends CommonDeltaEvent {

  public NoOp(int sequenceNumber) {
    super(sequenceNumber);
  }
}
