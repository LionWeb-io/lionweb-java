package io.lionweb.client.delta.messages.commands;

import io.lionweb.client.delta.messages.DeltaCommand;
import java.util.List;
import org.jetbrains.annotations.NotNull;

/** Groups several commands into a logical group. The parts are ordered. */
public class CompositeCommand extends DeltaCommand {
  public final List<DeltaCommand> parts;

  public CompositeCommand(@NotNull String commandId, List<DeltaCommand> parts) {
    super(commandId);
    this.parts = parts;
  }
}
