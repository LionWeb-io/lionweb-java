package io.lionweb.client.delta.messages.queries;

import io.lionweb.client.delta.messages.DeltaQuery;
import org.jetbrains.annotations.NotNull;

/** Request to be informed about partition creation and/or deletion events. */
public class InformAboutChangingPartitionsRequest extends DeltaQuery {
  /** Whether the client wants to be notified about newly created partitions. */
  public boolean creation;

  /** Whether the client wants to be notified about deleted partitions. */
  public boolean deletion;

  /** The maximum depth of the node tree to include in notifications. */
  public int depthLimit;

  public InformAboutChangingPartitionsRequest(@NotNull String queryId) {
    super(queryId);
  }

  @Override
  public String toString() {
    return "InformAboutChangingPartitionsRequest{"
        + "creation="
        + creation
        + ", deletion="
        + deletion
        + ", depthLimit="
        + depthLimit
        + ", queryId='"
        + queryId
        + '\''
        + '}';
  }
}
