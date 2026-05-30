package io.lionweb.client.delta.messages.queries;

import io.lionweb.client.delta.messages.DeltaQuery;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a request to list the partitions of a dataset in the Delta framework.
 *
 * <p>This request allows the client to retrieve hierarchical information about partitions in a
 * dataset, based on a specified depth limit. The depth limit determines how deeply into the node
 * tree the response will include data.
 *
 * <p>The depth limit is defined as follows: - A value of 0 means that only top-level partitions
 * (without any nested partitions) will be included in the response. - Higher depth values result in
 * more nested levels of partitions being included.
 *
 * <p>This class extends {@code DeltaQuery}, which provides the base functionality for identifying
 * and managing queries sent to the server.
 */
public class ListPartitionsRequest extends DeltaQuery {
  /** The maximum depth of the node tree to include in the response (0 = partitions only). */
  public int depthLimit;

  public ListPartitionsRequest(@NotNull String queryId) {
    super(queryId);
  }

  @Override
  public String toString() {
    return "ListPartitionsRequest{" + "queryId='" + queryId + '\'' + '}';
  }
}
