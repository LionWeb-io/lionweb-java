package io.lionweb.client.delta.messages.commands.annotations;

import io.lionweb.client.delta.messages.DeltaCommand;
import io.lionweb.serialization.data.MetaPointer;
import io.lionweb.serialization.data.SerializationChunk;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * Delete current node replacedAnnotation at parent's annotations at index, and all its descendants
 * (including annotation instances). Does NOT change references to any of the deleted nodes.
 */
public final class ReplaceAnnotation extends DeltaCommand {
  public final @NotNull SerializationChunk newAnnotation;
  public final @NotNull String parent;
  public final int index;
  public final @NotNull MetaPointer containment;
  public final @NotNull String replacedAnnotation;

  public ReplaceAnnotation(
      @NotNull String commandId,
      @NotNull SerializationChunk newAnnotation,
      @NotNull String parent,
      @NotNull MetaPointer containment,
      int index,
      @NotNull String replacedAnnotation) {
    super(commandId);
    Objects.requireNonNull(newAnnotation, "newAnnotation must not be null");
    Objects.requireNonNull(parent, "parent must not be null");
    if (index < 0) {
      throw new IllegalArgumentException("index must be non-negative");
    }
    Objects.requireNonNull(containment, "containment must not be null");
    Objects.requireNonNull(replacedAnnotation, "replacedAnnotation must not be null");
    this.newAnnotation = newAnnotation;
    this.parent = parent;
    this.containment = containment;
    this.index = index;
    this.replacedAnnotation = replacedAnnotation;
  }

  @Override
  public String toString() {
    return "ReplaceAnnotation{"
        + "newAnnotation="
        + newAnnotation
        + ", parent='"
        + parent
        + '\''
        + ", index="
        + index
        + ", containment="
        + containment
        + ", replacedAnnotation='"
        + replacedAnnotation
        + '\''
        + ", commandId='"
        + commandId
        + '\''
        + ", protocolMessages="
        + protocolMessages
        + '}';
  }
}
