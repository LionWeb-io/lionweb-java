package io.lionweb.client.delta;

import java.util.Objects;
import org.jetbrains.annotations.NotNull;

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
