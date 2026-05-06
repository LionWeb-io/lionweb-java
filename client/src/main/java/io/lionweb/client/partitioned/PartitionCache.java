package io.lionweb.client.partitioned;

import io.lionweb.LionWebVersion;
import io.lionweb.serialization.data.SerializationChunk;
import io.lionweb.serialization.data.SerializedClassifierInstance;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Bounded LRU cache of loaded partitions for a single repository.
 *
 * <p>The cache evicts the least-recently-used non-pinned partition when the number of loaded
 * partitions exceeds {@link CacheConfig#getMaxLoadedPartitions()} or the total node count exceeds
 * {@link CacheConfig#getMaxLoadedNodes()}. Dirty partitions are written to disk through the {@link
 * RepositoryBackend} before being evicted.
 *
 * <p>Pinning ({@link LoadedPartition#pin()} / {@link LoadedPartition#unpin()}) keeps a partition in
 * cache while it is actively being accessed, preventing accidental eviction during multi-step
 * operations.
 *
 * <p>This class is not thread-safe. The caller must provide external synchronisation for concurrent
 * access.
 */
final class PartitionCache {

  private final int maxPartitions;
  private final int maxNodes;
  private final RepositoryBackend backend;
  private final String repositoryName;
  private final LionWebVersion lionWebVersion;
  private final Map<String, PartitionMetadata> allMetadata;

  /** Access-order LinkedHashMap: eldest entry = LRU candidate for eviction. */
  private final LinkedHashMap<String, LoadedPartition> loaded =
      new LinkedHashMap<>(16, 0.75f, true);

  private int currentNodeCount = 0;

  PartitionCache(
      CacheConfig config,
      RepositoryBackend backend,
      String repositoryName,
      LionWebVersion lionWebVersion,
      Map<String, PartitionMetadata> allMetadata) {
    this.maxPartitions = config.getMaxLoadedPartitions();
    this.maxNodes = config.getMaxLoadedNodes();
    this.backend = backend;
    this.repositoryName = repositoryName;
    this.lionWebVersion = lionWebVersion;
    this.allMetadata = allMetadata;
  }

  /**
   * Returns the loaded partition for {@code partitionId}, loading it from disk if necessary.
   *
   * @throws IOException if the backend fails to load
   */
  LoadedPartition getOrLoad(String partitionId) throws IOException {
    LoadedPartition existing = loaded.get(partitionId);
    if (existing != null) {
      return existing;
    }
    List<SerializedClassifierInstance> nodes = backend.loadPartition(repositoryName, partitionId);
    LoadedPartition lp = new LoadedPartition(partitionId, nodes);
    insertIntoCache(partitionId, lp);
    return lp;
  }

  /** Inserts a freshly-created empty partition directly into the cache. */
  void putNew(String partitionId, LoadedPartition lp) throws IOException {
    insertIntoCache(partitionId, lp);
  }

  private void insertIntoCache(String partitionId, LoadedPartition lp) throws IOException {
    loaded.put(partitionId, lp);
    currentNodeCount += lp.nodeCount();
    evictIfNeeded();
  }

  /** Returns the loaded partition or {@code null} if not currently cached. */
  LoadedPartition get(String partitionId) {
    return loaded.get(partitionId);
  }

  boolean isLoaded(String partitionId) {
    return loaded.containsKey(partitionId);
  }

  /**
   * Notifies the cache that the node count of a partition has changed. Call this after adding or
   * removing nodes from a loaded partition so that the cache's total count stays accurate.
   */
  void nodeCountChanged(String partitionId, int delta) {
    currentNodeCount += delta;
  }

  /**
   * Persists the partition if dirty, then removes it from the cache.
   *
   * <p>Used by {@link PartitionCachingPolicy} to implement write-through eviction: the data is
   * guaranteed to be on disk before the in-memory copy is released.
   */
  void evict(String partitionId) throws IOException {
    LoadedPartition lp = loaded.get(partitionId);
    if (lp == null) return;
    persistIfDirty(partitionId, lp);
    currentNodeCount -= lp.nodeCount();
    loaded.remove(partitionId);
  }

  /** Removes a partition from the cache without saving, even if dirty. */
  void remove(String partitionId) {
    LoadedPartition lp = loaded.remove(partitionId);
    if (lp != null) {
      currentNodeCount -= lp.nodeCount();
    }
  }

  /** Writes all dirty loaded partitions to the backend. */
  void flush() throws IOException {
    for (Map.Entry<String, LoadedPartition> entry : loaded.entrySet()) {
      persistIfDirty(entry.getKey(), entry.getValue());
    }
  }

  /** Flushes all dirty partitions and clears the cache. */
  void close() throws IOException {
    flush();
    loaded.clear();
    currentNodeCount = 0;
  }

  int loadedCount() {
    return loaded.size();
  }

  int currentNodeCount() {
    return currentNodeCount;
  }

  // ---------------------------------------------------------------------------
  // Private helpers
  // ---------------------------------------------------------------------------

  private void evictIfNeeded() throws IOException {
    if (loaded.size() <= maxPartitions && currentNodeCount <= maxNodes) {
      return;
    }
    Iterator<Map.Entry<String, LoadedPartition>> it = loaded.entrySet().iterator();
    while ((loaded.size() > maxPartitions || currentNodeCount > maxNodes) && it.hasNext()) {
      Map.Entry<String, LoadedPartition> entry = it.next();
      LoadedPartition lp = entry.getValue();
      if (lp.isPinned()) {
        continue;
      }
      String partitionId = entry.getKey();
      persistIfDirty(partitionId, lp);
      currentNodeCount -= lp.nodeCount();
      it.remove();
    }
  }

  private void persistIfDirty(String partitionId, LoadedPartition lp) throws IOException {
    PartitionMetadata metadata = allMetadata.get(partitionId);
    if (metadata == null || !metadata.dirty) {
      return;
    }
    List<SerializedClassifierInstance> nodes = new ArrayList<>(lp.nodesByID.values());
    if (nodes.isEmpty()) {
      return;
    }
    SerializationChunk chunk = SerializationChunk.fromNodes(lionWebVersion, nodes);
    backend.savePartition(repositoryName, partitionId, chunk);
    metadata.dirty = false;
  }
}
