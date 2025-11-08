package io.lionweb.client.delta.messages.commands;

import io.lionweb.client.delta.messages.DeltaCommand;
import io.lionweb.serialization.data.MetaPointer;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/** Change classifier of node to newClassifier. */
public class ChangeClassifier extends DeltaCommand {

  public final @NotNull String node;
  public final @NotNull MetaPointer newClassifier;

  public ChangeClassifier(
      @NotNull String commandId, @NotNull String node, @NotNull MetaPointer newClassifier) {
    super(commandId);
    Objects.requireNonNull(node, "node must not be null");
    Objects.requireNonNull(newClassifier, "newClassifier must not be null");
    this.node = node;
    this.newClassifier = newClassifier;
  }

  @Override
  public String toString() {
    return "ChangeClassifier{" + "node='" + node + '\'' + ", newClassifier=" + newClassifier + '}';
  }
}
