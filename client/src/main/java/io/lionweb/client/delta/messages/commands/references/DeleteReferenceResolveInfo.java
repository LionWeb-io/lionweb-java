package io.lionweb.client.delta.messages.commands.references;

import io.lionweb.client.delta.messages.DeltaCommand;
import io.lionweb.serialization.data.MetaPointer;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Delete existing deletedResolveInfo from existing entry inside parent's reference at index. */
public final class DeleteReferenceResolveInfo extends DeltaCommand {
  public final @NotNull String parent;
  public final @NotNull MetaPointer reference;
  public final int index;
  public final @Nullable String deletedResolveInfo;

  public DeleteReferenceResolveInfo(
      @NotNull String commandId,
      @NotNull String parent,
      @NotNull MetaPointer reference,
      int index,
      @Nullable String deletedResolveInfo) {
    super(commandId);
    Objects.requireNonNull(parent, "parent must not be null");
    Objects.requireNonNull(reference, "reference must not be null");
    if (index < 0) {
      throw new IllegalArgumentException("index must be non-negative");
    }
    this.parent = parent;
    this.reference = reference;
    this.index = index;
    this.deletedResolveInfo = deletedResolveInfo;
  }

  @Override
  public String toString() {
    return "DeleteReferenceResolveInfo{"
        + "parent='"
        + parent
        + '\''
        + ", reference="
        + reference
        + ", index="
        + index
        + ", deletedResolveInfo='"
        + deletedResolveInfo
        + '\''
        + '}';
  }
}
