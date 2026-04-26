package io.lionweb.client.partitioned;

/**
 * Configuration for the in-memory partition cache used by {@link PartitionedServer}.
 *
 * <p>Eviction happens when either {@code maxLoadedPartitions} or {@code maxLoadedNodes} is
 * exceeded. The two limits are independent; either one triggers eviction.
 */
public final class CacheConfig {

  public static final CacheConfig DEFAULT = new CacheConfig(2_000, 5_000_000);

  private final int maxLoadedPartitions;
  private final int maxLoadedNodes;

  public CacheConfig(int maxLoadedPartitions, int maxLoadedNodes) {
    if (maxLoadedPartitions <= 0) {
      throw new IllegalArgumentException("maxLoadedPartitions must be > 0");
    }
    if (maxLoadedNodes <= 0) {
      throw new IllegalArgumentException("maxLoadedNodes must be > 0");
    }
    this.maxLoadedPartitions = maxLoadedPartitions;
    this.maxLoadedNodes = maxLoadedNodes;
  }

  public int getMaxLoadedPartitions() {
    return maxLoadedPartitions;
  }

  public int getMaxLoadedNodes() {
    return maxLoadedNodes;
  }

  @Override
  public String toString() {
    return "CacheConfig{maxPartitions="
        + maxLoadedPartitions
        + ", maxNodes="
        + maxLoadedNodes
        + '}';
  }
}
