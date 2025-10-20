package io.lionweb.client.delta.messages.events.children;

import io.lionweb.client.delta.messages.DeltaCommand;
import io.lionweb.serialization.data.MetaPointer;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public final class ChildMovedAndReplacedFromOtherContainment extends DeltaCommand {
  public final String newParent;
  public final MetaPointer newContainment;
  public final int newIndex;
  public final String movedChild;
  public final String oldParent;
  public final MetaPointer oldContainment;
  public final int oldIndex;
  public final String replacedChild;
  public final List<String> replacedDescendants;

  public ChildMovedAndReplacedFromOtherContainment(
      @NotNull String commandId,
      String newParent,
      MetaPointer newContainment,
      int newIndex,
      String movedChild,
      String oldParent,
      MetaPointer oldContainment,
      int oldIndex,
      String replacedChild,
      List<String> replacedDescendants) {
    super(commandId);
    this.newParent = newParent;
    this.newContainment = newContainment;
    this.newIndex = newIndex;
    this.movedChild = movedChild;
    this.oldParent = oldParent;
    this.oldContainment = oldContainment;
    this.oldIndex = oldIndex;
    this.replacedChild = replacedChild;
    this.replacedDescendants = replacedDescendants;
  }
}
