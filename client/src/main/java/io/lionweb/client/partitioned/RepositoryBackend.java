package io.lionweb.client.partitioned;

import io.lionweb.serialization.data.SerializationChunk;
import io.lionweb.serialization.data.SerializedClassifierInstance;
import java.io.IOException;
import java.util.List;

/**
 * Storage abstraction for {@link PartitionedServer}.
 *
 * <p>A backend is responsible for durable persistence of individual partitions. The server layer
 * owns the in-memory cache and dirty-tracking; the backend only deals with reading and writing
 * serialised partition data.
 *
 * <p>All methods may throw {@link IOException} for underlying storage failures. Callers in {@link
 * PartitionedServer} wrap these in {@link java.io.UncheckedIOException}.
 *
 * <p>The contract for implementations:
 *
 * <ul>
 *   <li>{@code loadPartition} returns an empty list if the partition does not exist.
 *   <li>{@code savePartition} overwrites any existing data atomically (best-effort).
 *   <li>{@code deletePartition} is idempotent.
 *   <li>{@code deleteRepository} removes all persisted data for a repository.
 * </ul>
 */
public interface RepositoryBackend {

  /** Returns the IDs of partitions that exist in persistent storage for this repository. */
  List<String> listPersistedPartitionIds(String repositoryName) throws IOException;

  /**
   * Loads and deserialises all nodes for the given partition.
   *
   * @return the list of nodes; empty if the partition has not been persisted yet.
   */
  List<SerializedClassifierInstance> loadPartition(String repositoryName, String partitionId)
      throws IOException;

  /**
   * Persists a partition as a serialised chunk.
   *
   * <p>The chunk must have its {@code serializationFormatVersion} set before calling this method.
   * Use {@link io.lionweb.serialization.data.SerializationChunk#fromNodes} to create the chunk.
   */
  void savePartition(String repositoryName, String partitionId, SerializationChunk chunk)
      throws IOException;

  /**
   * Removes the persisted data for a partition. Does nothing if the partition was never persisted.
   */
  void deletePartition(String repositoryName, String partitionId) throws IOException;

  /** Returns {@code true} if a persisted file exists for the partition. */
  boolean hasPartition(String repositoryName, String partitionId) throws IOException;

  /** Removes all persisted data for the given repository. */
  void deleteRepository(String repositoryName) throws IOException;

  /** Releases any resources held by this backend. Called once at server close time. */
  void close() throws IOException;
}
