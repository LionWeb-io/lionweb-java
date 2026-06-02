package io.lionweb.client.delta.messages.events.properties;

import io.lionweb.client.delta.messages.BaseDeltaEvent;
import io.lionweb.serialization.data.MetaPointer;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public class PropertyDeleted extends BaseDeltaEvent {

  public final String node;
  public final MetaPointer property;
  public final String oldValue;

  public PropertyDeleted(int sequenceNumber, @NotNull String node, @NotNull MetaPointer property, @NotNull String oldValue) {
    super(sequenceNumber);
    Objects.requireNonNull(node, "node cannot be null");
    Objects.requireNonNull(property, "property cannot be null");
    Objects.requireNonNull(oldValue, "oldValue cannot be null");
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
