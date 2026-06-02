package io.lionweb.client.delta.messages.events.properties;

import io.lionweb.client.delta.messages.BaseDeltaEvent;
import io.lionweb.serialization.data.MetaPointer;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * Delta event fired when a property value is set for the first time on a node.
 *
 * @see <a href="https://lionweb.io/specification/delta/delta-api.html">LionWeb Delta API
 *     specification</a>
 */
public class PropertyAdded extends BaseDeltaEvent<PropertyAdded> {

  public final @NotNull String node;
  public final @NotNull MetaPointer property;
  public final @NotNull String newValue;

  public PropertyAdded(
      int sequenceNumber,
      @NotNull String node,
      @NotNull MetaPointer property,
      @NotNull String newValue) {
    super(sequenceNumber);
    Objects.requireNonNull(node, "node should not be null");
    Objects.requireNonNull(property, "property should not be null");
    Objects.requireNonNull(newValue, "newValue should not be null");
    this.node = node;
    this.property = property;
    this.newValue = newValue;
  }

  @Override
  public String toString() {
    return "PropertyAdded{"
        + "node='"
        + node
        + '\''
        + ", property="
        + property
        + ", newValue='"
        + newValue
        + '\''
        + '}';
  }
}
