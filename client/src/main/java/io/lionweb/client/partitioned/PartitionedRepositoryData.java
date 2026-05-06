package io.lionweb.client.partitioned;

import io.lionweb.client.api.ClassifierKey;
import io.lionweb.client.api.ClassifierResult;
import io.lionweb.client.api.RepositoryConfiguration;
import io.lionweb.client.api.RepositoryVersionToken;
import io.lionweb.serialization.data.MetaPointer;
import io.lionweb.serialization.data.SerializedClassifierInstance;
import io.lionweb.utils.CommonChecks;
import io.lionweb.utils.ValidationResult;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Manages all node data for a single repository, backed by a {@link PartitionCache} and a {@link
 * RepositoryBackend}.
 *
 * <p>The following structures are always kept in memory:
 *
 * <ul>
 *   <li>{@code partitionIds} — ordered list of registered partition IDs.
 *   <li>{@code nodeToPartitionIndex} — maps every known node ID to its containing partition ID.
 *   <li>{@code classifierIndex} — maps every {@link ClassifierKey} to the set of node IDs of that
 *       type; used for O(1) {@link #nodesByClassifier} queries.
 *   <li>{@code partitionMetadata} — lightweight per-partition state (dirty flag).
 * </ul>
 *
 * <p><b>Thread safety:</b> same as {@code RepositoryData} — concurrent reads of the node maps are
 * tolerated, but <em>mutation methods are not thread-safe</em>.
 */
final class PartitionedRepositoryData {

  @NotNull final RepositoryConfiguration configuration;

  /** Ordered list of partition IDs in this repository. */
  final List<String> partitionIds = new ArrayList<>();

  /** nodeId → partitionId; populated for every known node, including unloaded partitions. */
  private final Map<String, String> nodeToPartitionIndex = new HashMap<>();

  /** ClassifierKey → set of node IDs; kept in sync across all partitions. */
  private final @Nullable Map<ClassifierKey, Set<String>> classifierIndex;

  private final boolean materializeClassifierIndex;

  /** Per-partition metadata (dirty flag, etc.). */
  private final Map<String, PartitionMetadata> partitionMetadata = new HashMap<>();

  private final PartitionCache cache;
  private final PartitionCachingPolicy cachingPolicy;

  private int currentVersion = 0;
  private int nextId = 1;

  PartitionedRepositoryData(
      @NotNull RepositoryConfiguration configuration,
      @NotNull RepositoryBackend backend,
      @NotNull CacheConfig cacheConfig) {
    this(configuration, backend, cacheConfig, true, PartitionCachingPolicy.ALWAYS_CACHE);
  }

  PartitionedRepositoryData(
      @NotNull RepositoryConfiguration configuration,
      @NotNull RepositoryBackend backend,
      @NotNull CacheConfig cacheConfig,
      boolean materializeClassifierIndex) {
    this(
        configuration,
        backend,
        cacheConfig,
        materializeClassifierIndex,
        PartitionCachingPolicy.ALWAYS_CACHE);
  }

  PartitionedRepositoryData(
      @NotNull RepositoryConfiguration configuration,
      @NotNull RepositoryBackend backend,
      @NotNull CacheConfig cacheConfig,
      boolean materializeClassifierIndex,
      @NotNull PartitionCachingPolicy cachingPolicy) {
    this.configuration = configuration;
    this.materializeClassifierIndex = materializeClassifierIndex;
    this.classifierIndex = materializeClassifierIndex ? new HashMap<>() : null;
    this.cachingPolicy = cachingPolicy;
    this.cache =
        new PartitionCache(
            cacheConfig,
            backend,
            configuration.getName(),
            configuration.getLionWebVersion(),
            partitionMetadata);
  }

  // ---------------------------------------------------------------------------
  // Partition lifecycle
  // ---------------------------------------------------------------------------

  /** Registers a new partition and stores its initial nodes. */
  void createPartition(List<SerializedClassifierInstance> nodes) throws IOException {
    List<String> newIds = new ArrayList<>();
    for (SerializedClassifierInstance n : nodes) {
      if (n.getParentNodeID() == null && !partitionIds.contains(n.getID())) {
        newIds.add(n.getID());
      }
    }
    for (String id : newIds) {
      partitionIds.add(id);
      partitionMetadata.put(id, PartitionMetadata.newPartition(id));
      cache.putNew(id, new LoadedPartition(id));
    }
    store(nodes);
  }

  /** Removes partitions and all their nodes. */
  void deletePartitions(List<String> toDelete) throws IOException {
    for (String partitionId : toDelete) {
      if (!partitionIds.contains(partitionId)) {
        continue;
      }
      LoadedPartition partition = cache.getOrLoad(partitionId);
      for (SerializedClassifierInstance node : new ArrayList<>(partition.nodesByID.values())) {
        nodeToPartitionIndex.remove(node.getID());
        classifierIndexRemove(node);
      }
      cache.remove(partitionId);
      partitionMetadata.remove(partitionId);
    }
    partitionIds.removeIf(toDelete::contains);
  }

  // ---------------------------------------------------------------------------
  // Store
  // ---------------------------------------------------------------------------

  /**
   * Stores or updates a list of nodes. The nodes may belong to one or more partitions.
   *
   * <p>All root nodes (those with no parent) must already be registered via {@link
   * #createPartition} before calling this method.
   */
  void store(List<SerializedClassifierInstance> newNodes) throws IOException {
    // Validate: root nodes must be registered partitions
    for (SerializedClassifierInstance node : newNodes) {
      if (node.getParentNodeID() == null && !partitionIds.contains(node.getID())) {
        throw new IllegalArgumentException("Node " + node + " should be registered as a partition");
      }
    }

    // Build a local map for the entire batch
    Map<String, SerializedClassifierInstance> batchMap = new HashMap<>(newNodes.size() * 2);
    for (SerializedClassifierInstance node : newNodes) {
      batchMap.put(node.getID(), node);
    }

    // Resolve which partition each node belongs to
    Map<String, String> resolvedPartitions = new HashMap<>(newNodes.size() * 2);
    for (SerializedClassifierInstance node : newNodes) {
      resolvePartitionId(node, batchMap, resolvedPartitions);
    }

    // Group nodes by partition
    Map<String, List<SerializedClassifierInstance>> byPartition = new HashMap<>();
    for (SerializedClassifierInstance node : newNodes) {
      String pid = resolvedPartitions.get(node.getID());
      byPartition.computeIfAbsent(pid, k -> new ArrayList<>()).add(node);
    }

    // Apply changes partition by partition
    for (Map.Entry<String, List<SerializedClassifierInstance>> entry : byPartition.entrySet()) {
      String partitionId = entry.getKey();
      LoadedPartition partition = cache.getOrLoad(partitionId);
      partition.pin();
      try {
        int oldCount = partition.nodeCount();
        applyChangesInPartition(partition, entry.getValue(), batchMap, partitionId);
        cache.nodeCountChanged(partitionId, partition.nodeCount() - oldCount);
        partitionMetadata.get(partitionId).dirty = true;
      } finally {
        partition.unpin();
      }
      if (!cachingPolicy.shouldCache(
          partitionId, Collections.unmodifiableCollection(partition.nodesByID.values()))) {
        cache.evict(partitionId);
      }
    }
  }

  // ---------------------------------------------------------------------------
  // Retrieve
  // ---------------------------------------------------------------------------

  void retrieve(String nodeId, int limit, List<SerializedClassifierInstance> retrieved)
      throws IOException {
    String partitionId = nodeToPartitionIndex.get(nodeId);
    if (partitionId == null) {
      throw new IllegalArgumentException("Node with id " + nodeId + " cannot be found");
    }
    LoadedPartition partition = cache.getOrLoad(partitionId);
    partition.pin();
    try {
      retrieveFromPartition(partition.nodesByID, nodeId, limit, retrieved);
    } finally {
      partition.unpin();
    }
  }

  private void retrieveFromPartition(
      Map<String, SerializedClassifierInstance> nodesByID,
      String nodeId,
      int limit,
      List<SerializedClassifierInstance> retrieved) {
    SerializedClassifierInstance node = nodesByID.get(nodeId);
    if (node == null) {
      throw new IllegalArgumentException("Node with id " + nodeId + " cannot be found");
    }
    retrieved.add(node);
    if (limit > 0) {
      for (String childId : node.getChildren()) {
        try {
          retrieveFromPartition(nodesByID, childId, limit - 1, retrieved);
        } catch (Exception e) {
          throw new RuntimeException("Unable to retrieve child of " + node, e);
        }
      }
      for (String annotationId : node.getAnnotations()) {
        try {
          retrieveFromPartition(nodesByID, annotationId, limit - 1, retrieved);
        } catch (Exception e) {
          throw new RuntimeException("Unable to retrieve annotation of " + node, e);
        }
      }
    }
  }

  // ---------------------------------------------------------------------------
  // Queries
  // ---------------------------------------------------------------------------

  ClassifierResult nodesByClassifier(Integer limit, ClassifierKey key) {
    int actualLimit = (limit != null) ? limit : Integer.MAX_VALUE;
    Set<String> allIds;
    if (materializeClassifierIndex) {
      allIds = classifierIndex.getOrDefault(key, Collections.emptySet());
    } else {
      allIds = new HashSet<>();
      for (String partitionId : partitionIds) {
        LoadedPartition partition;
        try {
          partition = cache.getOrLoad(partitionId);
        } catch (IOException e) {
          throw new UncheckedIOException(e);
        }
        partition.pin();
        try {
          for (SerializedClassifierInstance node : partition.nodesByID.values()) {
            if (key.equals(classifierKeyOf(node))) allIds.add(node.getID());
          }
        } finally {
          partition.unpin();
        }
      }
    }
    int total = allIds.size();
    Set<String> limitedIds;
    if (actualLimit >= total) {
      limitedIds = Collections.unmodifiableSet(allIds);
    } else {
      limitedIds = new HashSet<>();
      for (String id : allIds) {
        limitedIds.add(id);
        if (limitedIds.size() >= actualLimit) break;
      }
    }
    return new ClassifierResult(limitedIds, total);
  }

  Map<ClassifierKey, ClassifierResult> nodesByClassifier(Integer limit) {
    int actualLimit = (limit != null) ? limit : Integer.MAX_VALUE;
    Map<ClassifierKey, Set<String>> index;
    if (materializeClassifierIndex) {
      index = classifierIndex;
    } else {
      index = new HashMap<>();
      for (String partitionId : partitionIds) {
        LoadedPartition partition;
        try {
          partition = cache.getOrLoad(partitionId);
        } catch (IOException e) {
          throw new UncheckedIOException(e);
        }
        partition.pin();
        try {
          for (SerializedClassifierInstance node : partition.nodesByID.values()) {
            index.computeIfAbsent(classifierKeyOf(node), k -> new HashSet<>()).add(node.getID());
          }
        } finally {
          partition.unpin();
        }
      }
    }
    Map<ClassifierKey, ClassifierResult> result = new HashMap<>(index.size() * 2);
    for (Map.Entry<ClassifierKey, Set<String>> entry : index.entrySet()) {
      Set<String> allIds = entry.getValue();
      int total = allIds.size();
      Set<String> limitedIds;
      if (actualLimit >= total) {
        limitedIds = Collections.unmodifiableSet(allIds);
      } else {
        limitedIds = new HashSet<>();
        for (String id : allIds) {
          limitedIds.add(id);
          if (limitedIds.size() >= actualLimit) break;
        }
      }
      result.put(entry.getKey(), new ClassifierResult(limitedIds, total));
    }
    return result;
  }

  Map<String, ClassifierResult> nodesByLanguage(Integer limit) {
    int actualLimit = (limit != null) ? limit : Integer.MAX_VALUE;

    // Aggregate from classifierIndex (no partition loading needed)
    Map<String, List<Set<String>>> rawByLang = new HashMap<>();
    for (Map.Entry<ClassifierKey, Set<String>> entry : classifierIndex.entrySet()) {
      String langKey = entry.getKey().getLanguageKey();
      rawByLang.computeIfAbsent(langKey, k -> new ArrayList<>()).add(entry.getValue());
    }

    Map<String, ClassifierResult> result = new HashMap<>(rawByLang.size() * 2);
    for (Map.Entry<String, List<Set<String>>> entry : rawByLang.entrySet()) {
      String langKey = entry.getKey();
      int total = 0;
      Set<String> limitedIds = new HashSet<>();
      for (Set<String> ids : entry.getValue()) {
        total += ids.size();
        for (String id : ids) {
          if (limitedIds.size() < actualLimit) limitedIds.add(id);
        }
      }
      result.put(langKey, new ClassifierResult(Collections.unmodifiableSet(limitedIds), total));
    }
    return result;
  }

  // ---------------------------------------------------------------------------
  // Consistency check
  // ---------------------------------------------------------------------------

  ValidationResult checkConsistency() throws IOException {
    ValidationResult result = new ValidationResult();

    for (String nodeId : nodeToPartitionIndex.keySet()) {
      if (!CommonChecks.isValidID(nodeId)) {
        result.addError("Invalid node id: " + nodeId);
      }
    }

    // Load every partition and check internal consistency
    for (String partitionId : partitionIds) {
      LoadedPartition partition = cache.getOrLoad(partitionId);
      partition.pin();
      try {
        checkPartitionConsistency(partition, result);
      } finally {
        partition.unpin();
      }
    }
    return result;
  }

  private void checkPartitionConsistency(LoadedPartition partition, ValidationResult result) {
    Map<String, SerializedClassifierInstance> nodesByID = partition.nodesByID;
    Map<String, Set<String>> containedNodes = new HashMap<>();

    for (SerializedClassifierInstance node : nodesByID.values()) {
      for (io.lionweb.serialization.data.SerializedContainmentValue cv : node.getContainments()) {
        for (String childId : cv.getChildrenIds()) {
          String placement = node.getID() + " at " + cv.getMetaPointer();
          if (containedNodes.containsKey(childId)) {
            result.addError(
                childId
                    + " is listed in multiple places: "
                    + containedNodes.get(childId)
                    + " and now "
                    + placement);
          } else {
            containedNodes.put(childId, new HashSet<>(Collections.singletonList(placement)));
          }
          SerializedClassifierInstance child = nodesByID.get(childId);
          if (child != null && !child.getParentNodeID().equals(node.getID())) {
            result.addError(
                childId
                    + " is listed as child of "
                    + node.getID()
                    + " but it has "
                    + child.getParentNodeID()
                    + " as parent");
          }
        }
      }
      for (String annotationId : node.getAnnotations()) {
        String placement = node.getID() + " among annotations";
        if (containedNodes.containsKey(annotationId)) {
          result.addError(
              annotationId
                  + " is listed in multiple places: "
                  + containedNodes.get(annotationId)
                  + " and now "
                  + placement);
        } else {
          containedNodes.put(annotationId, new HashSet<>(Collections.singletonList(placement)));
        }
        SerializedClassifierInstance annotation = nodesByID.get(annotationId);
        if (annotation != null && !Objects.equals(annotation.getParentNodeID(), node.getID())) {
          result.addError(
              annotationId
                  + " is listed as an annotation of "
                  + node.getID()
                  + " but it has "
                  + annotation.getParentNodeID()
                  + " as parent");
        }
      }

      if (node.getParentNodeID() != null) {
        SerializedClassifierInstance parent = nodesByID.get(node.getParentNodeID());
        if (parent != null && !parent.contains(node.getID())) {
          result.addError(
              node.getID()
                  + " lists as parent "
                  + node.getParentNodeID()
                  + " but such parent does not contain it");
        }
      }
    }
  }

  // ---------------------------------------------------------------------------
  // Version and ID management
  // ---------------------------------------------------------------------------

  RepositoryVersionToken bumpVersion() {
    return new RepositoryVersionToken("v-" + ++currentVersion);
  }

  List<String> ids(int count) {
    List<String> res = new ArrayList<>(count);
    while (res.size() < count) {
      String candidate = "id-" + (nextId++);
      if (!nodeToPartitionIndex.containsKey(candidate)) {
        res.add(candidate);
      }
    }
    return res;
  }

  // ---------------------------------------------------------------------------
  // Flush and close
  // ---------------------------------------------------------------------------

  void flush() throws IOException {
    cache.flush();
  }

  void close() throws IOException {
    cache.close();
  }

  // ---------------------------------------------------------------------------
  // Private helpers – partition resolution
  // ---------------------------------------------------------------------------

  private String resolvePartitionId(
      SerializedClassifierInstance node,
      Map<String, SerializedClassifierInstance> batchMap,
      Map<String, String> resolved) {

    String cached = resolved.get(node.getID());
    if (cached != null) return cached;

    // Node already in index from a previous operation
    String existing = nodeToPartitionIndex.get(node.getID());
    if (existing != null) {
      resolved.put(node.getID(), existing);
      return existing;
    }

    // Partition root (no parent)
    if (node.getParentNodeID() == null) {
      if (!partitionIds.contains(node.getID())) {
        throw new IllegalArgumentException(
            "Node " + node.getID() + " is a root but not a registered partition");
      }
      resolved.put(node.getID(), node.getID());
      return node.getID();
    }

    // New node: inherit partition from parent
    String parentId = node.getParentNodeID();
    String parentPartition = nodeToPartitionIndex.get(parentId);
    if (parentPartition != null) {
      resolved.put(node.getID(), parentPartition);
      return parentPartition;
    }

    // Parent is also new (in the batch) – resolve recursively
    SerializedClassifierInstance parentNode = batchMap.get(parentId);
    if (parentNode != null) {
      String partitionId = resolvePartitionId(parentNode, batchMap, resolved);
      resolved.put(node.getID(), partitionId);
      return partitionId;
    }

    throw new IllegalArgumentException(
        "Cannot resolve partition for node "
            + node.getID()
            + ": parent "
            + parentId
            + " not found in index or batch");
  }

  // ---------------------------------------------------------------------------
  // Private helpers – change application (mirrors RepositoryData.ChangeCalculator)
  // ---------------------------------------------------------------------------

  private void applyChangesInPartition(
      LoadedPartition partition,
      List<SerializedClassifierInstance> updatedNodes,
      Map<String, SerializedClassifierInstance> fullBatchMap,
      String partitionId) {

    Map<String, SerializedClassifierInstance> nodesByID = partition.nodesByID;

    Map<String, SerializedClassifierInstance> updatedNodesAsMap =
        new HashMap<>(updatedNodes.size() * 2);
    for (SerializedClassifierInstance n : updatedNodes) {
      updatedNodesAsMap.put(n.getID(), n);
    }

    Map<String, SerializedClassifierInstance> addedNodes = new HashMap<>();
    Set<String> removedNodes = new HashSet<>();

    for (SerializedClassifierInstance updatedNode : updatedNodes) {
      if (nodesByID.containsKey(updatedNode.getID())) {
        SerializedClassifierInstance currentNode = nodesByID.get(updatedNode.getID());

        // Handle parent change: remove from old parent when parent is not in the batch
        if (currentNode.getParentNodeID() != null
            && !updatedNodesAsMap.containsKey(currentNode.getParentNodeID())
            && !fullBatchMap.containsKey(currentNode.getParentNodeID())) {
          if (!currentNode.getParentNodeID().equals(updatedNode.getParentNodeID())) {
            removeContainedNodeFromPartition(
                nodesByID, currentNode.getParentNodeID(), updatedNode.getID());
          }
        }

        // Track child and annotation list changes
        calculateListDiff(
            updatedNodesAsMap,
            nodesByID,
            nodesByID.get(updatedNode.getID()).getChildren(),
            updatedNode.getChildren(),
            addedNodes,
            removedNodes);
        calculateListDiff(
            updatedNodesAsMap,
            nodesByID,
            nodesByID.get(updatedNode.getID()).getAnnotations(),
            updatedNode.getAnnotations(),
            addedNodes,
            removedNodes);
      }
    }

    // Nodes that appear both removed and added were moved (not actually deleted)
    removedNodes.removeAll(addedNodes.keySet());

    // Update classifier index before replacing entries
    for (SerializedClassifierInstance updatedNode : updatedNodes) {
      SerializedClassifierInstance existing = nodesByID.get(updatedNode.getID());
      if (existing != null) {
        ClassifierKey oldKey = classifierKeyOf(existing);
        ClassifierKey newKey = classifierKeyOf(updatedNode);
        if (!oldKey.equals(newKey)) {
          classifierIndexRemove(existing);
          classifierIndexAdd(updatedNode);
        }
      } else {
        classifierIndexAdd(updatedNode);
      }
    }

    // Apply the updates
    nodesByID.putAll(updatedNodesAsMap);

    // Update nodeToPartitionIndex for all updated nodes (handles new ones)
    for (SerializedClassifierInstance updatedNode : updatedNodes) {
      nodeToPartitionIndex.put(updatedNode.getID(), partitionId);
    }

    // Recursively remove nodes that left the tree
    for (String removedId : removedNodes) {
      removeNodeRecursive(partition, removedId, addedNodes.keySet(), partitionId);
    }
  }

  /**
   * Calculates which nodes were added to or removed from a containment/annotation list. Mirrors
   * {@code ChangeCalculator.calculateNodeListDifferences}.
   */
  private static void calculateListDiff(
      Map<String, SerializedClassifierInstance> updatedNodesAsMap,
      Map<String, SerializedClassifierInstance> nodesByID,
      List<String> oldState,
      List<String> newState,
      Map<String, SerializedClassifierInstance> addedNodes,
      Set<String> removedNodes) {

    Set<String> oldSet = new HashSet<>(oldState);
    Set<String> newSet = new HashSet<>(newState);

    for (String n : newState) {
      if (!oldSet.contains(n)) {
        addedNodes.put(n, updatedNodesAsMap.get(n));
      }
    }

    List<String> unknownNodes = null;
    for (String c : newState) {
      if (!updatedNodesAsMap.containsKey(c) && !nodesByID.containsKey(c)) {
        if (unknownNodes == null) unknownNodes = new ArrayList<>();
        unknownNodes.add(c);
      }
    }
    if (unknownNodes != null) {
      throw new IllegalArgumentException("Unknown nodes referenced: " + unknownNodes);
    }

    for (String n : oldState) {
      if (!newSet.contains(n)) {
        removedNodes.add(n);
      }
    }
  }

  /**
   * Removes a child/annotation ID from the containment/annotation lists of its container, so that
   * the parent's data stays consistent when a node changes parent.
   */
  private static void removeContainedNodeFromPartition(
      Map<String, SerializedClassifierInstance> nodesByID, String containerId, String containedId) {
    SerializedClassifierInstance container = nodesByID.get(containerId);
    if (container == null) return;
    container.getContainments().forEach(c -> c.removeChild(containedId));
    container.removeAnnotation(containedId);
  }

  /**
   * Recursively removes a node and all descendants that are not being moved. Mirrors {@code
   * ChangeCalculator.removeNode}.
   */
  private void removeNodeRecursive(
      LoadedPartition partition, String nodeId, Set<String> movedNodeIds, String partitionId) {
    SerializedClassifierInstance node = partition.nodesByID.get(nodeId);
    if (node == null) return;

    for (String childId : node.getChildren()) {
      if (!movedNodeIds.contains(childId)) {
        removeNodeRecursive(partition, childId, movedNodeIds, partitionId);
      }
    }
    for (String annotationId : node.getAnnotations()) {
      if (!movedNodeIds.contains(annotationId)) {
        removeNodeRecursive(partition, annotationId, movedNodeIds, partitionId);
      }
    }

    classifierIndexRemove(node);
    nodeToPartitionIndex.remove(nodeId);
    partition.nodesByID.remove(nodeId);
  }

  // ---------------------------------------------------------------------------
  // Classifier index helpers
  // ---------------------------------------------------------------------------

  private static ClassifierKey classifierKeyOf(SerializedClassifierInstance node) {
    MetaPointer mp = node.getClassifier();
    return new ClassifierKey(mp.getLanguage(), mp.getKey());
  }

  private void classifierIndexAdd(SerializedClassifierInstance node) {
    if (!materializeClassifierIndex) return;
    classifierIndex.computeIfAbsent(classifierKeyOf(node), k -> new HashSet<>()).add(node.getID());
  }

  private void classifierIndexRemove(SerializedClassifierInstance node) {
    if (!materializeClassifierIndex) return;
    ClassifierKey key = classifierKeyOf(node);
    Set<String> ids = classifierIndex.get(key);
    if (ids != null) {
      ids.remove(node.getID());
      if (ids.isEmpty()) classifierIndex.remove(key);
    }
  }
}
