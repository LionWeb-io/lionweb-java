package io.lionweb.client.delta.messages.commands.references;

import io.lionweb.client.delta.messages.DeltaCommand;
import io.lionweb.serialization.data.MetaPointer;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Delete existing entry deletedTarget/deletedResolveInfo from parent's reference at index. */
public final class DeleteReference extends DeltaCommand {
  public final @NotNull String parent;
  public final @NotNull MetaPointer reference;
  public final int index;

  /** Deleted target node id. */
  public final @Nullable String deletedReference;

  /** Deleted resolve information. */
  public final @Nullable String deletedResolveInfo;

  public DeleteReference(
      @NotNull String commandId,
      @NotNull String parent,
      @NotNull MetaPointer reference,
      int index,
      @Nullable String deletedReference,
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
    this.deletedReference = deletedReference;
    this.deletedResolveInfo = deletedResolveInfo;
  }

  @Override
  public String toString() {
    return "DeleteReference{"
        + "parent='"
        + parent
        + '\''
        + ", reference="
        + reference
        + ", index="
        + index
        + ", deletedReference='"
        + deletedReference
        + '\''
        + ", deletedResolveInfo='"
        + deletedResolveInfo
        + '\''
        + '}';
  }
}
