package io.lionweb.client.inmemory;

import io.lionweb.client.api.ClassifierKey;
import io.lionweb.client.api.ClassifierResult;
import io.lionweb.client.api.RepositoryConfiguration;
import io.lionweb.client.api.RepositoryVersionToken;
import io.lionweb.serialization.LowLevelJsonSerialization;
import io.lionweb.serialization.data.SerializationChunk;
import io.lionweb.serialization.data.SerializedClassifierInstance;
import io.lionweb.utils.ValidationResult;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Holds all node data for a single repository using a two-tier hot/cold strategy.
 *
 * <p>Hot partitions are kept as {@link RepositoryData} instances in memory. Cold partitions are
 * serialized to disk and evicted from memory using LRU policy. The number of hot partitions is
 * bounded by {@code maxHotPartitions}; when the limit is exceeded the least-recently-used
 * partition is flushed to disk.
 *
 * <p>Two compact count indexes are maintained across ALL partitions (hot and cold):
 * {@code classifierCountIndex} and {@code languageCountIndex}. Each maps a classifier/language
 * key to a per-partition count. This lets {@code nodesByClassifier} and {@code nodesByLanguage}
 * return accurate totals without loading cold partitions. Node IDs in the result are populated
 * from hot partitions only; to get IDs from a cold partition, warm it up first via
 * {@code retrieve}.
 *
 * <p>Cold-node lookup during {@code store} or {@code retrieve} automatically loads the
 * appropriate cold partition. The lookup scans cold partition files (O(cold partitions));
 * a bloom-filter index per partition is the natural next step for very large cold sets.
 *
 * <p><b>Thread safety:</b> same caveats as {@link RepositoryData} — mutations are not
 * thread-safe.
 */
class DiskBackedRepositoryData {

  private static final LowLevelJsonSerialization SERIALIZATION = new LowLevelJsonSerialization();

  @NotNull final RepositoryConfiguration configuration;

  /** All partition IDs known to this repository (hot + cold). */
  final List<String> partitionIDs = new ArrayList<>();

  /**
   * Access-order LinkedHashMap gives LRU eviction: the eldest entry is the least-recently
   * accessed partition and is evicted to disk when the hot tier is full.
   */
  private final Map<String, RepositoryData> hotPartitions;

  private final Set<String> coldPartitionIDs = new HashSet<>();
  private final Set<String> dirtyPartitions = new HashSet<>();

  /**
   * nodeId → partitionId for nodes currently in hot partitions. May have stale entries for
   * deleted nodes; validated and cleaned up on lookup.
   */
  private final Map<String, String> hotNodeIndex = new HashMap<>();

  /**
   * Compact count index covering ALL partitions (hot + cold).
   * ClassifierKey → (partitionId → node count in that partition).
   * Kept in sync on every mutation; never cleared on eviction.
   */
  private final Map<ClassifierKey, Map<String, Integer>> classifierCountIndex = new HashMap<>();

  /**
   * Compact count index covering ALL partitions (hot + cold).
   * language string → (partitionId → node count in that partition).
   */
  private final Map<String, Map<String, Integer>> languageCountIndex = new HashMap<>();

  private final Path repoDir;
  private final int maxHotPartitions;
  private int currentVersion = 0;
  private int nextId = 1;

  DiskBackedRepositoryData(
      @NotNull RepositoryConfiguration configuration,
      @NotNull Path repoDir,
      int maxHotPartitions) {
    this.configuration = configuration;
    this.repoDir = repoDir;
    this.maxHotPartitions = maxHotPartitions;
    this.hotPartitions =
        new LinkedHashMap<String, RepositoryData>(maxHotPartitions + 1, 0.75f, true) {
          @Override
          protected boolean removeEldestEntry(Map.Entry<String, RepositoryData> eldest) {
            if (size() > DiskBackedRepositoryData.this.maxHotPartitions) {
              evictPartition(eldest.getKey(), eldest.getValue());
              return true;
            }
            return false;
          }
        };
    try {
      Files.createDirectories(repoDir);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  RepositoryVersionToken bumpVersion() {
    return new RepositoryVersionToken("v-" + ++currentVersion);
  }

  List<String> ids(int count) {
    List<String> result = new ArrayList<>(count);
    while (result.size() < count) {
      String candidate = "id-" + (nextId++);
      if (!hotNodeIndex.containsKey(candidate)) {
        result.add(candidate);
      }
    }
    return result;
  }

  void addPartition(@NotNull String partitionId, @NotNull List<SerializedClassifierInstance> nodes) {
    if (!partitionIDs.contains(partitionId)) {
      partitionIDs.add(partitionId);
    }
    coldPartitionIDs.remove(partitionId);
    RepositoryData rd = new RepositoryData(configuration);
    rd.partitionIDs.add(partitionId);
    rd.store(nodes);
    hotPartitions.put(partitionId, rd); // may trigger removeEldestEntry → eviction
    dirtyPartitions.add(partitionId);
    for (SerializedClassifierInstance n : nodes) {
      hotNodeIndex.put(n.getID(), partitionId);
    }
    rebuildPartitionCountIndices(partitionId, rd);
  }

  void deletePartition(@NotNull String partitionId) {
    partitionIDs.remove(partitionId);
    coldPartitionIDs.remove(partitionId);
    dirtyPartitions.remove(partitionId);
    RepositoryData rd = hotPartitions.remove(partitionId);
    if (rd != null) {
      rd.nodesByID.keySet().forEach(hotNodeIndex::remove);
    }
    removePartitionFromCountIndices(partitionId);
    try {
      Files.deleteIfExists(partitionFile(partitionId));
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  void store(@NotNull List<SerializedClassifierInstance> nodes) {
    nodes.stream()
        .filter(n -> n.getParentNodeID() == null)
        .forEach(
            n -> {
              if (!partitionIDs.contains(n.getID())) {
                throw new IllegalArgumentException(
                    "Node " + n.getID() + " should be registered as a partition");
              }
            });

    Map<String, SerializedClassifierInstance> batchMap = new HashMap<>();
    nodes.forEach(n -> batchMap.put(n.getID(), n));

    Map<String, List<SerializedClassifierInstance>> byPartition = new LinkedHashMap<>();
    for (SerializedClassifierInstance node : nodes) {
      String pid = resolvePartition(node, batchMap);
      byPartition.computeIfAbsent(pid, k -> new ArrayList<>()).add(node);
    }

    for (Map.Entry<String, List<SerializedClassifierInstance>> entry : byPartition.entrySet()) {
      String pid = entry.getKey();
      ensureHot(pid);
      RepositoryData rd = hotPartitions.get(pid);
      rd.store(entry.getValue());
      dirtyPartitions.add(pid);
      for (SerializedClassifierInstance n : entry.getValue()) {
        hotNodeIndex.put(n.getID(), pid);
      }
      // Rebuild counts after store — ChangeCalculator may have implicitly removed nodes
      rebuildPartitionCountIndices(pid, rd);
    }
  }

  void retrieve(
      @NotNull String nodeId,
      int limit,
      @NotNull List<SerializedClassifierInstance> result) {
    String pid = findHotPartitionFor(nodeId);
    if (pid == null) {
      pid = findAndLoadColdPartitionFor(nodeId);
    }
    if (pid == null) {
      throw new IllegalArgumentException("Node with id " + nodeId + " cannot be found");
    }
    hotPartitions.get(pid).retrieve(nodeId, limit, result);
  }

  /**
   * Returns the hot {@link RepositoryData} that contains the given node, loading its partition
   * from disk if necessary. Marks the partition dirty since the caller intends to mutate the
   * returned node in-place.
   */
  @NotNull
  RepositoryData hotRepositoryDataForNode(@NotNull String nodeId) {
    String pid = findHotPartitionFor(nodeId);
    if (pid == null) {
      pid = findAndLoadColdPartitionFor(nodeId);
    }
    if (pid == null) {
      throw new IllegalArgumentException("Node with id " + nodeId + " cannot be found");
    }
    dirtyPartitions.add(pid);
    return hotPartitions.get(pid);
  }

  /**
   * Returns accurate totals across ALL partitions (hot + cold) using the compact count index.
   * Node ID samples in the result are drawn from hot partitions only.
   */
  Map<ClassifierKey, ClassifierResult> nodesByClassifier(@Nullable Integer limit) {
    int actualLimit = (limit != null) ? limit : Integer.MAX_VALUE;

    // Collect IDs from hot partitions, capped at actualLimit per classifier
    Map<ClassifierKey, Set<String>> hotIds = new HashMap<>();
    for (RepositoryData rd : hotPartitions.values()) {
      for (SerializedClassifierInstance n : rd.nodesByID.values()) {
        ClassifierKey ck =
            new ClassifierKey(n.getClassifier().getLanguage(), n.getClassifier().getKey());
        Set<String> ids = hotIds.computeIfAbsent(ck, k -> new HashSet<>());
        if (ids.size() < actualLimit) {
          ids.add(n.getID());
        }
      }
    }

    // Build results using global count index for totals
    Map<ClassifierKey, ClassifierResult> result = new HashMap<>();
    for (Map.Entry<ClassifierKey, Map<String, Integer>> entry : classifierCountIndex.entrySet()) {
      int total = 0;
      for (int c : entry.getValue().values()) total += c;
      Set<String> ids = hotIds.getOrDefault(entry.getKey(), Collections.<String>emptySet());
      result.put(entry.getKey(), new ClassifierResult(ids, total));
    }
    return result;
  }

  /**
   * Returns accurate totals across ALL partitions (hot + cold) using the compact count index.
   * Node ID samples in the result are drawn from hot partitions only.
   */
  Map<String, ClassifierResult> nodesByLanguage(@Nullable Integer limit) {
    int actualLimit = (limit != null) ? limit : Integer.MAX_VALUE;

    // Collect IDs from hot partitions, capped at actualLimit per language
    Map<String, Set<String>> hotIds = new HashMap<>();
    for (RepositoryData rd : hotPartitions.values()) {
      for (SerializedClassifierInstance n : rd.nodesByID.values()) {
        String lang = n.getClassifier().getLanguage();
        Set<String> ids = hotIds.computeIfAbsent(lang, k -> new HashSet<>());
        if (ids.size() < actualLimit) {
          ids.add(n.getID());
        }
      }
    }

    // Build results using global count index for totals
    Map<String, ClassifierResult> result = new HashMap<>();
    for (Map.Entry<String, Map<String, Integer>> entry : languageCountIndex.entrySet()) {
      int total = 0;
      for (int c : entry.getValue().values()) total += c;
      Set<String> ids = hotIds.getOrDefault(entry.getKey(), Collections.<String>emptySet());
      result.put(entry.getKey(), new ClassifierResult(ids, total));
    }
    return result;
  }

  @NotNull
  ValidationResult checkConsistency() {
    ValidationResult result = new ValidationResult();
    for (RepositoryData rd : hotPartitions.values()) {
      result.getIssues().addAll(rd.checkConsistency().getIssues());
    }
    return result;
  }

  // --- private helpers ---

  /**
   * Rebuilds the classifier and language count entries for a single partition. Removes old counts
   * for this partition, then recomputes from the partition's current node set.
   */
  private void rebuildPartitionCountIndices(String partitionId, RepositoryData rd) {
    removePartitionFromCountIndices(partitionId);
    for (SerializedClassifierInstance n : rd.nodesByID.values()) {
      ClassifierKey ck =
          new ClassifierKey(n.getClassifier().getLanguage(), n.getClassifier().getKey());
      classifierCountIndex.computeIfAbsent(ck, k -> new HashMap<>())
          .merge(partitionId, 1, Integer::sum);
      languageCountIndex.computeIfAbsent(n.getClassifier().getLanguage(), k -> new HashMap<>())
          .merge(partitionId, 1, Integer::sum);
    }
  }

  private void removePartitionFromCountIndices(String partitionId) {
    for (Map<String, Integer> partitionCounts : classifierCountIndex.values()) {
      partitionCounts.remove(partitionId);
    }
    classifierCountIndex.values().removeIf(Map::isEmpty);
    for (Map<String, Integer> partitionCounts : languageCountIndex.values()) {
      partitionCounts.remove(partitionId);
    }
    languageCountIndex.values().removeIf(Map::isEmpty);
  }

  /**
   * Resolves which partition a node belongs to. For root nodes it's trivially the node itself.
   * For non-root nodes the parent chain is walked through the batch, then hot nodes, then (as a
   * last resort) cold partition files are scanned and automatically loaded.
   */
  private String resolvePartition(
      SerializedClassifierInstance node,
      Map<String, SerializedClassifierInstance> batchMap) {
    if (node.getParentNodeID() == null) {
      return node.getID();
    }
    String current = node.getParentNodeID();
    while (current != null) {
      if (partitionIDs.contains(current)) return current;
      String pid = findHotPartitionFor(current);
      if (pid != null) return pid;
      SerializedClassifierInstance parent = batchMap.get(current);
      if (parent != null) {
        if (parent.getParentNodeID() == null) {
          return parent.getID();
        }
        current = parent.getParentNodeID();
      } else {
        // Parent is neither in the batch nor in a hot partition — scan cold partitions
        String coldPid = findAndLoadColdPartitionFor(current);
        if (coldPid != null) return coldPid;
        break;
      }
    }
    throw new IllegalArgumentException(
        "Cannot determine partition for node " + node.getID() + ". "
            + "Ensure the partition exists before storing its nodes.");
  }

  @Nullable
  private String findHotPartitionFor(String nodeId) {
    String pid = hotNodeIndex.get(nodeId);
    if (pid == null) return null;
    RepositoryData rd = hotPartitions.get(pid);
    if (rd == null || !rd.nodesByID.containsKey(nodeId)) {
      hotNodeIndex.remove(nodeId);
      return null;
    }
    return pid;
  }

  @Nullable
  private String findAndLoadColdPartitionFor(String nodeId) {
    for (String coldPid : new ArrayList<>(coldPartitionIDs)) {
      Path file = partitionFile(coldPid);
      if (!Files.exists(file)) continue;
      try {
        String json = Files.readString(file, StandardCharsets.UTF_8);
        SerializationChunk chunk = SERIALIZATION.deserializeSerializationBlock(json);
        boolean found =
            chunk.getClassifierInstances().stream().anyMatch(n -> nodeId.equals(n.getID()));
        if (found) {
          ensureHot(coldPid);
          return coldPid;
        }
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    }
    return null;
  }

  private void ensureHot(String partitionId) {
    if (hotPartitions.containsKey(partitionId)) {
      hotPartitions.get(partitionId); // access to refresh LRU order
      return;
    }
    if (!coldPartitionIDs.contains(partitionId)) {
      throw new IllegalArgumentException("Partition " + partitionId + " not found");
    }
    RepositoryData rd = loadFromDisk(partitionId);
    coldPartitionIDs.remove(partitionId);
    hotPartitions.put(partitionId, rd); // may trigger eviction of another partition
    for (String nid : rd.nodesByID.keySet()) {
      hotNodeIndex.put(nid, partitionId);
    }
  }

  private void evictPartition(String partitionId, RepositoryData rd) {
    if (dirtyPartitions.remove(partitionId)) {
      writeToDisk(partitionId, rd);
    }
    rd.nodesByID.keySet().forEach(hotNodeIndex::remove);
    coldPartitionIDs.add(partitionId);
    // Count index entries remain — they are still valid for cold partitions
  }

  private void writeToDisk(String partitionId, RepositoryData rd) {
    List<SerializedClassifierInstance> nodes = new ArrayList<>(rd.nodesByID.values());
    if (nodes.isEmpty()) return;
    SerializationChunk chunk =
        SerializationChunk.fromNodes(configuration.getLionWebVersion(), nodes);
    String json = SERIALIZATION.serializeToJsonString(chunk);
    try {
      Files.writeString(partitionFile(partitionId), json, StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private RepositoryData loadFromDisk(String partitionId) {
    Path file = partitionFile(partitionId);
    try {
      String json = Files.readString(file, StandardCharsets.UTF_8);
      SerializationChunk chunk = SERIALIZATION.deserializeSerializationBlock(json);
      RepositoryData rd = new RepositoryData(configuration);
      rd.partitionIDs.add(partitionId);
      List<SerializedClassifierInstance> nodes = new ArrayList<>(chunk.getClassifierInstances());
      if (!nodes.isEmpty()) {
        rd.store(nodes);
      }
      return rd;
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private Path partitionFile(String partitionId) {
    String encoded = URLEncoder.encode(partitionId, StandardCharsets.UTF_8);
    if (encoded.length() > 200) {
      encoded = encoded.substring(0, 200);
    }
    return repoDir.resolve(encoded + ".json");
  }
}
