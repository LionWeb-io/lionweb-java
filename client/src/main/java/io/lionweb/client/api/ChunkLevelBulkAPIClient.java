package io.lionweb.client.api;

import io.lionweb.LionWebVersion;
import io.lionweb.serialization.data.SerializedClassifierInstance;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * In certain cases it is convenient to communicate with the repository without forcing a full
 * serialization or de-serialization of the data received. This is useful for performance reasons or
 * because we do not have the languages available, so we cannot proceed to a full deserialization.
 */
public interface ChunkLevelBulkAPIClient {
  @NotNull
  LionWebVersion getLionWebVersion();

  @NotNull
  List<String> ids(int count) throws IOException;

  @NotNull
  List<String> listPartitionsIDs() throws IOException;

  @Nullable
  RepositoryVersionToken createPartitionsFromChunk(@NotNull List<SerializedClassifierInstance> data)
      throws IOException;

  @Nullable
  RepositoryVersionToken deletePartitions(List<String> ids) throws IOException;

  @Nullable
  RepositoryVersionToken storeChunk(@NotNull List<SerializedClassifierInstance> nodes)
      throws IOException;

  @Nullable
  default RepositoryVersionToken storeChunk(@NotNull Iterable<SerializedClassifierInstance> nodes)
      throws IOException {
    return storeChunk(
        StreamSupport.stream(nodes.spliterator(), false).collect(Collectors.toList()));
  }

  @NotNull
  List<SerializedClassifierInstance> retrieveAsChunk(@NotNull List<String> nodeIds, int limit)
      throws IOException;

  @NotNull
  default Iterable<SerializedClassifierInstance> retrieveAsIterableChunk(
      @NotNull Iterable<String> nodeIds, int limit) throws IOException {
    Objects.requireNonNull(nodeIds);
    return retrieveAsChunk(
        StreamSupport.stream(nodeIds.spliterator(), false).collect(Collectors.toList()), limit);
  }

  default @NotNull List<SerializedClassifierInstance> retrieveAsChunk(@NotNull List<String> nodeIds)
      throws IOException {
    return retrieveAsChunk(nodeIds, Integer.MAX_VALUE);
  }

  default @NotNull Iterable<SerializedClassifierInstance> retrieveAsIterableChunk(
      @NotNull Iterable<String> nodeIds) throws IOException {
    return retrieveAsIterableChunk(nodeIds, Integer.MAX_VALUE);
  }
}
