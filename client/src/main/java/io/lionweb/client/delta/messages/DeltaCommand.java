package io.lionweb.client.delta.messages;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an abstract base class for a command in the delta framework. A DeltaCommand is
 * uniquely identified by a `commandId` and may contain associated protocol messages.
 *
 * <p>Usage: It is designed to be extended by subclasses that define specific types of delta
 * operations.
 */
public abstract class DeltaCommand {
  public final @NotNull String commandId;
  public final List<ProtocolMessage> protocolMessages = new LinkedList<>();

  public DeltaCommand(@NotNull String commandId) {
    Objects.requireNonNull(commandId, "commandId should not be null");
    this.commandId = commandId;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    DeltaCommand that = (DeltaCommand) o;
    return Objects.equals(commandId, that.commandId);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(commandId);
  }
}
