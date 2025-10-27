package io.lionweb.client.delta.messages.commands.properties;

import io.lionweb.client.delta.messages.DeltaCommand;
import io.lionweb.serialization.data.MetaPointer;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ChangeProperty extends DeltaCommand {
  public final @NotNull String node;
  public final @NotNull MetaPointer property;
  public final @Nullable String newValue;

  public ChangeProperty(
      @NotNull String commandId,
      @NotNull String node,
      @NotNull MetaPointer property,
      @Nullable String newValue) {
    super(commandId);
    Objects.requireNonNull(node, "node must not be null");
    Objects.requireNonNull(property, "property must not be null");
    this.node = node;
    this.property = property;
    this.newValue = newValue;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    ChangeProperty that = (ChangeProperty) o;
    return Objects.equals(commandId, that.commandId)
        && Objects.equals(node, that.node)
        && Objects.equals(property, that.property)
        && Objects.equals(newValue, that.newValue);
  }

  @Override
  public int hashCode() {
    return Objects.hash(commandId, node, property, newValue);
  }

  @Override
  public String toString() {
    return "ChangeProperty{"
        + "node='"
        + node
        + '\''
        + ", property="
        + property
        + ", newValue='"
        + newValue
        + '\''
        + ", commandId='"
        + commandId
        + '\''
        + ", protocolMessages="
        + protocolMessages
        + '}';
  }
}
