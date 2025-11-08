package io.lionweb.client.delta.messages.events.properties;

import io.lionweb.client.delta.messages.BaseDeltaEvent;
import io.lionweb.serialization.data.MetaPointer;

public class PropertyDeleted extends BaseDeltaEvent {

  public final String node;
  public final MetaPointer property;
  public final String oldValue;

  public PropertyDeleted(int sequenceNumber, String node, MetaPointer property, String oldValue) {
    super(sequenceNumber);
    this.node = node;
    this.property = property;
    this.oldValue = oldValue;
  }

  @Override
  public String toString() {
    return "PropertyDeleted{"
        + "node='"
        + node
        + '\''
        + ", property="
        + property
        + ", oldValue='"
        + oldValue
        + '\''
        + '}';
  }
}
