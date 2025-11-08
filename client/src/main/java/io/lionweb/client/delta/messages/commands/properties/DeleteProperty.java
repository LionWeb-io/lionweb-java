package io.lionweb.client.delta.messages.commands.properties;

import io.lionweb.client.delta.messages.DeltaCommand;
import io.lionweb.serialization.data.MetaPointer;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public final class DeleteProperty extends DeltaCommand {
  public final @NotNull String node;
  public final @NotNull MetaPointer property;

  public DeleteProperty(
      @NotNull String commandId, @NotNull String node, @NotNull MetaPointer property) {
    super(commandId);
    Objects.requireNonNull(node, "node must not be null");
    Objects.requireNonNull(property, "property must not be null");
    this.node = node;
    this.property = property;
  }

  @Override
  public String toString() {
    return "DeleteProperty{" + "node='" + node + '\'' + ", property=" + property + '}';
  }
}
