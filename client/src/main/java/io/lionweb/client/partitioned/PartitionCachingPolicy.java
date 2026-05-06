package io.lionweb.client.partitioned;

import io.lionweb.serialization.data.SerializedClassifierInstance;
import java.util.Collection;

/**
 * Strategy that controls whether a partition remains in the in-memory cache after it has been
 * written.
 *
 * <p>Use {@link #ALWAYS_CACHE} for the default behaviour: partitions stay in cache until evicted by
 * the {@link CacheConfig} size limits. Use {@link #NEVER_CACHE} for write-once or archive
 * partitions that must be persisted but will never be queried again, so keeping them in memory
 * would only waste heap.
 *
 * <p>Custom implementations can apply a mixed strategy — for example, evicting only partitions
 * whose ID matches a known prefix, or partitions above a certain node count.
 */
@FunctionalInterface
public interface PartitionCachingPolicy {

  /**
   * Returns {@code true} if the partition with the given ID should remain in the in-memory cache
   * after it has been written to disk.
   *
   * <p>This is evaluated once per write operation, immediately after the partition is unpinned. The
   * {@code nodes} collection is an unmodifiable view of the partition's current content and may be
   * used to make content-aware decisions (e.g. based on node count or classifier types). A return
   * value of {@code false} causes the partition to be flushed to the backend and evicted from cache
   * right away, freeing the memory it occupies.
   */
  boolean shouldCache(String partitionId, Collection<SerializedClassifierInstance> nodes);

  /** Default policy: every partition stays in cache until evicted by {@link CacheConfig} limits. */
  PartitionCachingPolicy ALWAYS_CACHE = (partitionId, nodes) -> true;

  /** Write-through policy: every partition is flushed and evicted immediately after each write. */
  PartitionCachingPolicy NEVER_CACHE = (partitionId, nodes) -> false;
}
