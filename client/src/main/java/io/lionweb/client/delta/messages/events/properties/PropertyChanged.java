package io.lionweb.client.delta.messages.events.properties;

import io.lionweb.client.delta.messages.BaseDeltaEvent;
import io.lionweb.serialization.data.MetaPointer;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PropertyChanged extends BaseDeltaEvent<PropertyChanged> {

  public final String node;
  public final MetaPointer property;
  public final String newValue;
  public final String oldValue;

  public PropertyChanged(
      int sequenceNumber,
      @NotNull String node,
      @NotNull MetaPointer property,
      @Nullable String newValue,
      @Nullable String oldValue) {
    super(sequenceNumber);
    Objects.requireNonNull(node, "node cannot be null");
    Objects.requireNonNull(property, "property cannot be null");
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
