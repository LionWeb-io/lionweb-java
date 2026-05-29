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

  /** Target node id. */
  public final @Nullable String newReference;

  /**
   * Specifies the new resolve information for a reference being added.
   *
   * <p>If present, this provides auxiliary details or context needed to resolve the reference. It
   * may be null to indicate that no additional resolve information is provided.
   */
  public final @Nullable String newResolveInfo;

  public AddReference(
      @NotNull String commandId,
      @NotNull String parent,
      @NotNull MetaPointer reference,
      int index,
      @Nullable String newReference,
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
    this.newReference = newReference;
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
        + ", newReference='"
        + newReference
        + '\''
        + ", newResolveInfo='"
        + newResolveInfo
        + '\''
        + '}';
  }
}
