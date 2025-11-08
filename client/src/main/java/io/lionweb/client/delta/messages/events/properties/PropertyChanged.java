package io.lionweb.client.delta.messages.events.properties;

import io.lionweb.client.delta.messages.BaseDeltaEvent;
import io.lionweb.serialization.data.MetaPointer;

public class PropertyChanged extends BaseDeltaEvent<PropertyChanged> {

  public final String node;
  public final MetaPointer property;
  public final String newValue;
  public final String oldValue;

  public PropertyChanged(
      int sequenceNumber, String node, MetaPointer property, String newValue, String oldValue) {
    super(sequenceNumber);
    this.node = node;
    this.property = property;
    this.newValue = newValue;
    this.oldValue = oldValue;
  }

  @Override
  public String toString() {
    return "PropertyChanged{"
        + "node='"
        + node
        + '\''
        + ", property="
        + property
        + ", newValue='"
        + newValue
        + '\''
        + ", oldValue='"
        + oldValue
        + '\''
        + '}';
  }
}
