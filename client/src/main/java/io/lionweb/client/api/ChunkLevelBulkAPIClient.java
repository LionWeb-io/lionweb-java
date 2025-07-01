package io.lionweb.client.api;

import io.lionweb.LionWebVersion;
import io.lionweb.serialization.data.SerializedChunk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;

/**
 * In certain cases it is convenient to communicate with the repository without forcing a full
 * serialization or de-serialization of the data received. This is useful for performance reasons or
 * because we do not have the languages available, so we cannot proceed to a full deserialization.
 */
public interface ChunkLevelBulkAPIClient {
  @NotNull
  LionWebVersion getLionWebVersion();

  @Nullable
  RepositoryVersionToken createPartitions(@NotNull SerializedChunk data) throws IOException;


  @Nullable
  RepositoryVersionToken store(@NotNull SerializedChunk nodes) throws IOException;

  @NotNull
  SerializedChunk retrieveAsChunk(@Nullable List<String> nodeIds, int limit) throws IOException;

  default @NotNull SerializedChunk retrieveAsChunk(@Nullable List<String> nodeIds)
          throws IOException {
    return retrieveAsChunk(nodeIds, Integer.MAX_VALUE);
  }

}
