package io.lionweb.client.delta.messages.commands.references;

import io.lionweb.client.delta.messages.DeltaCommand;
import io.lionweb.serialization.data.MetaPointer;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Add newTarget / newResolveInfo to parent's reference at index. */
public final class AddReference extends DeltaCommand {
  public final @NotNull String parent;
  public final @NotNull MetaPointer reference;
  public final int index;
  public final @Nullable String newTarget;
  public final @Nullable String newResolveInfo;

  public AddReference(
      @NotNull String commandId,
      @NotNull String parent,
      @NotNull MetaPointer reference,
      int index,
      @Nullable String newTarget,
      @Nullable String newResolveInfo) {
    super(commandId);
    Objects.requireNonNull(parent, "parent must not be null");
    Objects.requireNonNull(reference, "reference must not be null");
    if (index < 0) {
      throw new IllegalArgumentException("index must be non-negative");
    }
    this.parent = parent;
    this.reference = reference;
    this.index = index;
    this.newTarget = newTarget;
    this.newResolveInfo = newResolveInfo;
  }

  @Override
  public String toString() {
    return "AddReference{"
        + "parent='"
        + parent
        + '\''
        + ", reference="
        + reference
        + ", index="
        + index
        + ", newTarget='"
        + newTarget
        + '\''
        + ", newResolveInfo='"
        + newResolveInfo
        + '\''
        + '}';
  }
}
