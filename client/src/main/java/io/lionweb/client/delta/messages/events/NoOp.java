package io.lionweb.client.delta.messages.events;

import io.lionweb.client.delta.messages.BaseDeltaEvent;

public class NoOp extends BaseDeltaEvent {

  public NoOp(int sequenceNumber) {
    super(sequenceNumber);
  }

  @Override
  public String toString() {
    return "NoOp{" + "sequenceNumber=" + sequenceNumber + '}';
  }
}
