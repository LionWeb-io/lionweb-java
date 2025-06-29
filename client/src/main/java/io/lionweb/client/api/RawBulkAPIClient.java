package io.lionweb.client.api;

import io.lionweb.LionWebVersion;
import java.io.IOException;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * In certain cases it is convenient to communicate with the repository without forcing a full
 * serialization or de-serialization of the data received. This is useful for performance reasons or
 * because we do not have the languages available, so we cannot proceed to a full deserialization.
 */
public interface RawBulkAPIClient {
  @NotNull
  LionWebVersion getLionWebVersion();

  @Nullable
  RepositoryVersionToken rawCreatePartitions(@NotNull String data) throws IOException;

  @Nullable
  RepositoryVersionToken rawStore(@NotNull String nodes) throws IOException;

  @NotNull
  String rawRetrieve(@Nullable List<String> nodeIds, int limit) throws IOException;

  default @NotNull String rawRetrieve(@Nullable List<String> nodeIds) throws IOException {
    return rawRetrieve(nodeIds, Integer.MAX_VALUE);
  }
}
