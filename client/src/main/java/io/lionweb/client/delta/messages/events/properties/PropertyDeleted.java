package io.lionweb.client.delta.messages.events.properties;

import io.lionweb.client.delta.messages.BaseDeltaEvent;
import io.lionweb.serialization.data.MetaPointer;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * Delta event fired when a property value is removed from a node.
 *
 * @see <a href="https://lionweb.io/specification/delta/delta-api.html">LionWeb Delta API
 *     specification</a>
 */
public class PropertyDeleted extends BaseDeltaEvent<PropertyDeleted> {

  public final @NotNull String node;
  public final @NotNull MetaPointer property;
  public final @NotNull String oldValue;

  public PropertyDeleted(
      int sequenceNumber,
      @NotNull String node,
      @NotNull MetaPointer property,
      @NotNull String oldValue) {
    super(sequenceNumber);
    Objects.requireNonNull(node, "node should not be null");
    Objects.requireNonNull(property, "property should not be null");
    Objects.requireNonNull(oldValue, "oldValue should not be null");
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
