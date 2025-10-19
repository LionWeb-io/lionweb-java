package io.lionweb.client.delta.messages.commands;

import io.lionweb.client.delta.messages.DeltaCommand;
import io.lionweb.serialization.data.MetaPointer;
import io.lionweb.serialization.data.SerializationChunk;
import org.jetbrains.annotations.NotNull;

/**
 * Delete current node replacedAnnotation at parent's annotations at index, and all its descendants
 * (including annotation instances). Does NOT change references to any of the deleted nodes.
 */
public final class ReplaceAnnotation extends DeltaCommand {
  public final SerializationChunk newAnnotation;
  public final String parent;
  public final int index;
  public final MetaPointer containment;
  public final String replacedAnnotation;

  public ReplaceAnnotation(
      @NotNull String commandId,
      SerializationChunk newAnnotation,
      String parent,
      MetaPointer containment,
      int index,
      String replacedAnnotation) {
    super(commandId);
    this.newAnnotation = newAnnotation;
    this.parent = parent;
    this.containment = containment;
    this.index = index;
    this.replacedAnnotation = replacedAnnotation;
  }
}
