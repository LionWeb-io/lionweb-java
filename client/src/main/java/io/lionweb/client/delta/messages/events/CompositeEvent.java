package io.lionweb.client.delta.messages.events;

import io.lionweb.client.delta.messages.BaseDeltaEvent;
import java.util.List;

/** Groups several events into a logical sequence. The parts are ordered by sequence number. */
public class CompositeEvent extends BaseDeltaEvent<CompositeEvent> {
  /** The ordered list of events that make up this composite event. */
  public List<BaseDeltaEvent> parts;

  public CompositeEvent(int sequenceNumber, List<BaseDeltaEvent> parts) {
    super(sequenceNumber);
    this.parts = parts;
  }

  @Override
  public String toString() {
    return "CompositeEvent{" + "parts=" + parts + '}';
  }
}
