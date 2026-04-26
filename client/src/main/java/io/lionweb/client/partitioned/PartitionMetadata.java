package io.lionweb.client.partitioned;

/**
 * Compact per-partition metadata that is always kept in memory, regardless of whether the
 * partition itself is currently loaded.
 *
 * <p>Instances of this class are owned by {@link PartitionedRepositoryData} and must not be shared
 * across threads without external synchronisation.
 */
final class PartitionMetadata {

  final String partitionId;

  /** True when the in-memory state differs from the persisted state and needs a flush. */
  volatile boolean dirty;

  PartitionMetadata(String partitionId, boolean dirty) {
    this.partitionId = partitionId;
    this.dirty = dirty;
  }

  /** Creates metadata for a brand-new partition (starts dirty, not yet on disk). */
  static PartitionMetadata newPartition(String partitionId) {
    return new PartitionMetadata(partitionId, true);
  }

  /** Creates metadata for a partition that was loaded from disk (starts clean). */
  static PartitionMetadata existing(String partitionId) {
    return new PartitionMetadata(partitionId, false);
  }
}
