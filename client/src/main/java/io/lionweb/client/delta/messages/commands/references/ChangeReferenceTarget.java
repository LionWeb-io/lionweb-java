package io.lionweb.client.delta.messages.commands.references;

import io.lionweb.client.delta.messages.DeltaCommand;
import io.lionweb.serialization.data.MetaPointer;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Change oldTarget of existing entry inside parent's reference at index to newTarget. */
public final class ChangeReferenceTarget extends DeltaCommand {
  public final @NotNull String parent;
  public final @NotNull MetaPointer reference;
  public final int index;
  public final @Nullable String oldTarget;
  public final @Nullable String newTarget;

  public ChangeReferenceTarget(
      @NotNull String commandId,
      @NotNull String parent,
      @NotNull MetaPointer reference,
      int index,
      @Nullable String oldTarget,
      @Nullable String newTarget) {
    super(commandId);
    Objects.requireNonNull(parent, "parent must not be null");
    Objects.requireNonNull(reference, "reference must not be null");
    if (index < 0) {
      throw new IllegalArgumentException("index must be non-negative");
    }
    this.parent = parent;
    this.reference = reference;
    this.index = index;
    this.oldTarget = oldTarget;
    this.newTarget = newTarget;
  }

  @Override
  public String toString() {
    return "ChangeReferenceTarget{"
        + "parent='"
        + parent
        + '\''
        + ", reference="
        + reference
        + ", index="
        + index
        + ", oldTarget='"
        + oldTarget
        + '\''
        + ", newTarget='"
        + newTarget
        + '\''
        + '}';
  }
}
