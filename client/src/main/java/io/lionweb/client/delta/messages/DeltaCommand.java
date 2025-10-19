package io.lionweb.client.delta.messages;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public class DeltaCommand {
  public final @NotNull String commandId;
  public final List<ProtocolMessage> protocolMessages = new LinkedList<>();

  public DeltaCommand(@NotNull String commandId) {
    Objects.requireNonNull(commandId, "commandId should not be null");
    this.commandId = commandId;
  }
}
