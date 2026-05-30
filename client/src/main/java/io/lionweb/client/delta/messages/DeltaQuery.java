package io.lionweb.client.delta.messages;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/** Represents an abstract base class for a Delta Query in the Delta framework. */
public abstract class DeltaQuery {
  /** Represents the unique identifier for this Delta Query. */
  public final @NotNull String queryId;

  /**
   * Represents additional information associated with a protocol message in the Delta framework.
   */
  public final List<AdditionalInfo> additionalInfos = new LinkedList<>();

  public DeltaQuery(@NotNull String queryId) {
    Objects.requireNonNull(queryId, "queryId must not be null");
    this.queryId = queryId;
  }
}
