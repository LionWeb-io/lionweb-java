package io.lionweb.client.delta.messages.events;

import io.lionweb.client.delta.messages.CommonDeltaEvent;
import io.lionweb.client.delta.messages.DeltaEvent;
import java.util.List;

public class CompositeEvent extends DeltaEvent {
  public List<CommonDeltaEvent> parts;

  public CompositeEvent(List<CommonDeltaEvent> parts) {
    this.parts = parts;
  }
}
