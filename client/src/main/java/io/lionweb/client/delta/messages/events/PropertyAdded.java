package io.lionweb.client.delta.messages.events;

import io.lionweb.client.delta.messages.DeltaEvent;
import io.lionweb.serialization.data.MetaPointer;

public class PropertyAdded extends DeltaEvent {

  public final String node;
  public final MetaPointer property;
  public final String newValue;

  public PropertyAdded(int sequenceNumber, String node, MetaPointer property, String newValue) {
    super(sequenceNumber);
    this.node = node;
    this.property = property;
    this.newValue = newValue;
  }
}
