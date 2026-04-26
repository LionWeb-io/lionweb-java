package io.lionweb.client.partitioned;

import io.lionweb.serialization.data.SerializedClassifierInstance;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A partition that has been loaded into the in-memory cache.
 *
 * <p>The {@code nodesByID} map holds all nodes belonging to this partition. All containment
 * relationships within a partition are local: a node's children are always in the same partition
 * as the node itself.
 *
 * <p>Pinning ({@link #pin()} / {@link #unpin()}) prevents the cache from evicting this partition
 * while it is actively being read or written.
 */
final class LoadedPartition {

  final String partitionId;
  final Map<String, SerializedClassifierInstance> nodesByID;

  /** Reference count used to prevent eviction while the partition is in use. */
  private int pinCount = 0;

  LoadedPartition(String partitionId) {
    this.partitionId = partitionId;
    this.nodesByID = new HashMap<>();
  }

  LoadedPartition(String partitionId, List<SerializedClassifierInstance> nodes) {
    this.partitionId = partitionId;
    this.nodesByID = new HashMap<>(Math.max(16, nodes.size() * 2));
    for (SerializedClassifierInstance n : nodes) {
      this.nodesByID.put(n.getID(), n);
    }
  }

  int nodeCount() {
    return nodesByID.size();
  }

  void pin() {
    pinCount++;
  }

  void unpin() {
    if (pinCount <= 0) {
      throw new IllegalStateException("Unpin called more times than pin on " + partitionId);
    }
    pinCount--;
  }

  boolean isPinned() {
    return pinCount > 0;
  }
}
