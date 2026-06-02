package io.lionweb.client.delta.messages.queries;

import io.lionweb.client.delta.messages.DeltaQueryResponse;
import java.util.Arrays;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * Response to {@link GetAvailableIdsRequest} carrying the reserved node IDs.
 *
 * @see <a href="https://lionweb.io/specification/delta/delta-api.html">LionWeb Delta API specification</a>
 */
public class GetAvailableIdsResponse extends DeltaQueryResponse {

  private final @NotNull String[] ids;

  public GetAvailableIdsResponse(@NotNull String queryId, @NotNull String[] ids) {
    super(queryId);
    Objects.requireNonNull(ids, "ids must not be null");
    this.ids = ids;
  }

  @Override
  public String toString() {
    return "GetAvailableIdsResponse{"
        + "ids="
        + Arrays.toString(ids)
        + ", queryId='"
        + queryId
        + '\''
        + '}';
  }
}
