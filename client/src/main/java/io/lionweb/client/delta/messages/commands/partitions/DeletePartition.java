package io.lionweb.client.delta.messages.commands.partitions;

import io.lionweb.client.delta.messages.DeltaCommand;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public final class DeletePartition extends DeltaCommand {
  public final @NotNull String deletedPartition;

  public DeletePartition(@NotNull String commandId, @NotNull String deletedPartition) {
    super(commandId);
    Objects.requireNonNull(deletedPartition, "deletedPartition must not be null");
    this.deletedPartition = deletedPartition;
  }

  @Override
  public String toString() {
    return "DeletePartition{" + "deletedPartition='" + deletedPartition + '\'' + '}';
  }
}
