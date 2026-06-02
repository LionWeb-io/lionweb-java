package io.lionweb.client.delta.messages.events.properties;

import io.lionweb.client.delta.messages.BaseDeltaEvent;
import io.lionweb.serialization.data.MetaPointer;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Delta event fired when an existing property value on a node is replaced.
 *
 * @see <a href="https://lionweb.io/specification/delta/delta-api.html">LionWeb Delta API
 *     specification</a>
 */
public class PropertyChanged extends BaseDeltaEvent<PropertyChanged> {

  public final @NotNull String node;
  public final @NotNull MetaPointer property;
  public final @Nullable String newValue;
  public final @Nullable String oldValue;

  public PropertyChanged(
      int sequenceNumber,
      @NotNull String node,
      @NotNull MetaPointer property,
      @Nullable String newValue,
      @Nullable String oldValue) {
    super(sequenceNumber);
    Objects.requireNonNull(node, "node should not be null");
    Objects.requireNonNull(property, "property should not be null");
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
