package io.lionweb.client.delta.messages.events;

import io.lionweb.client.delta.messages.BaseDeltaEvent;

/** A no-operation delta event used to advance the sequence number without any model change. */
public class NoOp extends BaseDeltaEvent {

  public NoOp(int sequenceNumber) {
    super(sequenceNumber);
  }

  @Override
  public String toString() {
    return "NoOp{" + "sequenceNumber=" + sequenceNumber + '}';
  }
}
