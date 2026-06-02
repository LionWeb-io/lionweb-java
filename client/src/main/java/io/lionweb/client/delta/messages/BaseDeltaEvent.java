package io.lionweb.client.delta.messages;

import io.lionweb.client.delta.CommandSource;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/** Base class used by most delta events. */
public abstract class BaseDeltaEvent<T extends BaseDeltaEvent<?>> extends DeltaEvent {
  public final int sequenceNumber;
  public final List<CommandSource> originCommands = new LinkedList<>();

  /**
   * Whether this message is a continuation of a split/chunked sequence. Absent (null) or false
   * means this is a standalone message; true means more parts follow.
   */
  public boolean split;

  public boolean isSplit() {
    return split;
  }

  public void setSplit(boolean split) {
    this.split = split;
  }

  public BaseDeltaEvent(int sequenceNumber) {
    this.sequenceNumber = sequenceNumber;
  }

  public @NotNull T addSource(@NotNull CommandSource source) {
    Objects.requireNonNull(source, "source should not be null");
    originCommands.add(source);
    return (T) this;
  }
}
