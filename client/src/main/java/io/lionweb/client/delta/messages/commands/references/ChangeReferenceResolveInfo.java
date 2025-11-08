package io.lionweb.client.delta.messages.commands.references;

import io.lionweb.client.delta.messages.DeltaCommand;
import io.lionweb.serialization.data.MetaPointer;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Change oldResolveInfo of existing entry inside parent's reference at index to newResolveInfo. */
public final class ChangeReferenceResolveInfo extends DeltaCommand {
  public final @NotNull String parent;
  public final @NotNull MetaPointer reference;
  public final int index;
  public final @Nullable String oldResolveInfo;
  public final @Nullable String newResolveInfo;

  public ChangeReferenceResolveInfo(
      @NotNull String commandId,
      @NotNull String parent,
      @NotNull MetaPointer reference,
      int index,
      @Nullable String oldResolveInfo,
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
    this.oldResolveInfo = oldResolveInfo;
    this.newResolveInfo = newResolveInfo;
  }

  @Override
  public String toString() {
    return "ChangeReferenceResolveInfo{"
        + "parent='"
        + parent
        + '\''
        + ", reference="
        + reference
        + ", index="
        + index
        + ", oldResolveInfo='"
        + oldResolveInfo
        + '\''
        + ", newResolveInfo='"
        + newResolveInfo
        + '\''
        + '}';
  }
}
