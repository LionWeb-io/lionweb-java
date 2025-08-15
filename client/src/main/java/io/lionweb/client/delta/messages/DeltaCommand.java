package io.lionweb.client.delta.messages;

import java.util.LinkedList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class DeltaCommand {
  public final @NotNull String commandId;
  public final List<ProtocolMessage> protocolMessages = new LinkedList<>();

  public DeltaCommand(@NotNull String commandId) {
    this.commandId = commandId;
  }
}
