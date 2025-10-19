package io.lionweb.client.delta.messages.commands;

import io.lionweb.client.delta.messages.DeltaCommand;
import org.jetbrains.annotations.NotNull;

/**
 * Delete existing node deletedAnnotation from parent's annotations at index, and all its
 * descendants (including annotation instances). Does NOT change references to any of the deleted
 * nodes.
 */
public class DeleteAnnotation extends DeltaCommand {
  public String node;
  public int index;
  public String deletedAnnotation;

  public DeleteAnnotation(
      @NotNull String commandId, String node, int index, String deletedAnnotation) {
    super(commandId);
    this.node = node;
    this.index = index;
    this.deletedAnnotation = deletedAnnotation;
  }
}
