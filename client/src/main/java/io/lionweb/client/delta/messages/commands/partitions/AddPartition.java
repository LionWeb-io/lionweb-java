package io.lionweb.client.delta.messages.commands.partitions;

import io.lionweb.client.delta.messages.DeltaCommand;
import io.lionweb.serialization.data.SerializationChunk;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public final class AddPartition extends DeltaCommand {
  public final @NotNull SerializationChunk newPartition;

  public AddPartition(@NotNull String commandId, @NotNull SerializationChunk newPartition) {
    super(commandId);
    Objects.requireNonNull(newPartition, "newPartition must not be null");
    this.newPartition = newPartition;
  }

  @Override
  public String toString() {
    return "AddPartition{" + "newPartition=" + newPartition + '}';
  }
}
