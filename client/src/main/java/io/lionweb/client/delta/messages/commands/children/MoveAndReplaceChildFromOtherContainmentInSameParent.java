package io.lionweb.client.delta.messages.commands.children;

import io.lionweb.client.delta.messages.DeltaCommand;
import io.lionweb.serialization.data.MetaPointer;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * Move existing node movedChild (currently inside one of movedChild's parent’s containments other
 * than newContainment) inside movedChild's parent’s newContainment at newIndex. Delete current
 * child replacedChild inside movedChild's parent’s newContainment at newIndex, and all its
 * descendants (including annotation instances). Does NOT change references to any of the deleted
 * nodes.
 */
public final class MoveAndReplaceChildFromOtherContainmentInSameParent extends DeltaCommand {
  public final @NotNull MetaPointer newContainment;
  public final int newIndex;
  public final @NotNull String replacedChild;
  public final @NotNull String movedChild;

  public MoveAndReplaceChildFromOtherContainmentInSameParent(
      @NotNull String commandId,
      @NotNull MetaPointer newContainment,
      int newIndex,
      @NotNull String replacedChild,
      @NotNull String movedChild) {
    super(commandId);

    Objects.requireNonNull(newContainment, "newContainment must not be null");
    if (newIndex < 0) {
      throw new IllegalArgumentException("newIndex must be non-negative");
    }
    Objects.requireNonNull(replacedChild, "replacedChild must not be null");
    Objects.requireNonNull(movedChild, "movedChild must not be null");
    this.newContainment = newContainment;
    this.newIndex = newIndex;
    this.replacedChild = replacedChild;
    this.movedChild = movedChild;
  }

  @Override
  public String toString() {
    return "MoveAndReplaceChildFromOtherContainmentInSameParent{"
        + "newContainment="
        + newContainment
        + ", newIndex="
        + newIndex
        + ", replacedChild='"
        + replacedChild
        + '\''
        + ", movedChild='"
        + movedChild
        + '\''
        + '}';
  }
}
