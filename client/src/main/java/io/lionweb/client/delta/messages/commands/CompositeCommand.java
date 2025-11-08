package io.lionweb.client.delta.messages.commands;

import io.lionweb.client.delta.messages.DeltaCommand;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/** Groups several commands into a logical group. The parts are ordered. */
public class CompositeCommand extends DeltaCommand {
  public final List<DeltaCommand> parts;

  public CompositeCommand(@NotNull String commandId, @NotNull List<DeltaCommand> parts) {
    super(commandId);
    Objects.requireNonNull(parts, "parts must not be null");
    this.parts = parts;
  }

  @Override
  public String toString() {
    return "CompositeCommand{" + "parts=" + parts + '}';
  }
}
