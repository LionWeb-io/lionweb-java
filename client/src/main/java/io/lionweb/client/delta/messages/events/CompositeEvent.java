package io.lionweb.client.delta.messages.events;

import io.lionweb.client.delta.messages.BaseDeltaEvent;
import io.lionweb.client.delta.messages.DeltaEvent;
import java.util.List;

public class CompositeEvent extends DeltaEvent {
  public List<BaseDeltaEvent> parts;

  public CompositeEvent(List<BaseDeltaEvent> parts) {
    this.parts = parts;
  }

  @Override
  public String toString() {
    return "CompositeEvent{" + "parts=" + parts + '}';
  }
}
