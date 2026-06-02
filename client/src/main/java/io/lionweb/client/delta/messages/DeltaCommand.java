package io.lionweb.client.delta.messages;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an abstract base class for a command in the delta framework. A DeltaCommand is
 * uniquely identified by a `commandId` and may contain associated protocol messages.
 *
 * <p>Usage: It is designed to be extended by subclasses that define specific types of delta
 * operations.
 */
/**
 * Abstract base for commands sent by a client to request model changes via the LionWeb Delta
 * protocol.
 *
 * @see <a href="https://lionweb.io/specification/delta/delta-api.html">LionWeb Delta API specification</a>
 */
public abstract class DeltaCommand {
  public final @NotNull String commandId;

  /**
   * Whether this message is a continuation of a split/chunked sequence. Absent (null) or false
   * means this is a standalone message; true means more parts follow.
   */
  public boolean split;

  /**
   * Represents additional information associated with a protocol message in the Delta framework.
   */
  public final List<AdditionalInfo> additionalInfos = new LinkedList<>();

  public DeltaCommand(@NotNull String commandId, boolean split) {
    Objects.requireNonNull(commandId, "commandId should not be null");
    this.commandId = commandId;
    this.split = split;
  }

  public DeltaCommand(@NotNull String commandId) {
    this(commandId, false);
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    DeltaCommand that = (DeltaCommand) o;
    return Objects.equals(commandId, that.commandId);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(commandId);
  }

  public boolean isSplit() {
    return split;
  }

  public void setSplit(boolean split) {
    this.split = split;
  }
}
