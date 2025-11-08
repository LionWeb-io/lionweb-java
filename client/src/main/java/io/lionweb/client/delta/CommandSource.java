package io.lionweb.client.delta;

import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the source of a command within a delta communication protocol. A CommandSource
 * consists of a unique participation ID and a command ID, which together identify the origin and
 * the specific command within a distributed system.
 *
 * <ul>
 *   <li>{@code participationId}: A non-null identifier for the participant that issued the command.
 *   <li>{@code commandId}: A non-null identifier for the specific command issued by the
 *       participant.
 * </ul>
 *
 * Instances of this class are immutable and are considered equal if both the {@code
 * participationId} and {@code commandId} match.
 */
public class CommandSource {
  public final @NotNull String participationId;
  public final @NotNull String commandId;

  public CommandSource(@NotNull String participationId, @NotNull String commandId) {
    Objects.requireNonNull(participationId, "participationId should not be null");
    Objects.requireNonNull(commandId, "commandId should not be null");
    this.participationId = participationId;
    this.commandId = commandId;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    CommandSource that = (CommandSource) o;
    return Objects.equals(participationId, that.participationId)
        && Objects.equals(commandId, that.commandId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(participationId, commandId);
  }

  @Override
  public String toString() {
    return "CommandSource{"
        + "participationId='"
        + participationId
        + '\''
        + ", commandId='"
        + commandId
        + '\''
        + '}';
  }
}
