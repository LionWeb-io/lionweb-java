package io.lionweb.client.delta.messages.commands;

import io.lionweb.client.delta.messages.DeltaCommand;
import io.lionweb.serialization.data.MetaPointer;
import java.util.Objects;
import org.jetbrains.annotations.Nullable;

public class AddProperty extends DeltaCommand {
  public String node;
  public MetaPointer property;
  public @Nullable String newValue;

  public AddProperty(
      String commandId, String node, MetaPointer property, @Nullable String newValue) {
    super(commandId);
    this.node = node;
    this.property = property;
    this.newValue = newValue;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    AddProperty that = (AddProperty) o;
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
    return "AddProperty{"
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
