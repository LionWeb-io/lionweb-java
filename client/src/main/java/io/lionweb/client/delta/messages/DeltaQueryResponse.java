package io.lionweb.client.delta.messages;

import java.util.*;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an abstract base class for handling responses to Delta Query objects in the Delta
 * framework. A DeltaQueryResponse is uniquely identified by a `queryId` and includes properties to
 * store protocol messages and arbitrary values.
 */
public abstract class DeltaQueryResponse {
  /**
   * Represents the unique identifier for this Delta Query Response.
   *
   * <p>This identifier is used to uniquely associate a response with the corresponding query that
   * was initiated within the Delta framework. It ensures traceability and mapping between requests
   * and their outcomes.
   *
   * <p>The value of this identifier must not be null.
   */
  public final @NotNull String queryId;

  /**
   * Represents additional information associated with a protocol message in the Delta framework.
   */
  public final List<AdditionalInfo> additionalInfos = new LinkedList<>();

  public DeltaQueryResponse(@NotNull String queryId) {
    Objects.requireNonNull(queryId, "queryId must not be null");
    this.queryId = queryId;
  }
}
