package io.lionweb.client.delta.messages.commands;

import io.lionweb.client.delta.messages.DeltaCommand;
import io.lionweb.serialization.data.MetaPointer;
import org.jetbrains.annotations.NotNull;

/** Change classifier of node to newClassifier. */
public class ChangeClassifier extends DeltaCommand {

  public final String node;
  public final MetaPointer newClassifier;

  public ChangeClassifier(@NotNull String commandId, String node, MetaPointer newClassifier) {
    super(commandId);
    this.node = node;
    this.newClassifier = newClassifier;
  }
}
