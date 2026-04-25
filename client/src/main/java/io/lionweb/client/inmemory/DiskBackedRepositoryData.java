package io.lionweb.client.inmemory;

import io.lionweb.client.api.ClassifierKey;
import io.lionweb.client.api.ClassifierResult;
import io.lionweb.client.api.RepositoryConfiguration;
import io.lionweb.client.api.RepositoryVersionToken;
import io.lionweb.serialization.ProtoBufSerialization;
import io.lionweb.serialization.data.SerializationChunk;
import io.lionweb.serialization.data.SerializedClassifierInstance;
import io.lionweb.utils.ValidationResult;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Holds all node data for a single repository using a two-tier hot/cold strategy.
 *
 * <h3>Hot / cold tiers</h3>
 *
 * Hot partitions are kept as {@link RepositoryData} instances in memory. Cold partitions are
 * serialized to disk as Protobuf files and evicted from memory using an LRU policy bounded by
 * {@code maxHotPartitions}. Promoting a cold partition back to hot deserializes the file and
 * rebuilds its {@link RepositoryData}.
 *
 * <h3>Bloom filters</h3>
 *
 * Each cold partition has a {@link PartitionBloomFilter} built at eviction time (while nodes are
 * still in memory). On a cold-node lookup, every cold partition's filter is checked first. A
 * definitive "not present" from the filter skips the disk read entirely. Only "maybe present"
 * partitions are actually read from disk. This reduces cold lookups from O(cold_partitions × disk)
 * to O(cold_partitions × memory) in the common case.
 *
 * <h3>Count indexes</h3>
 *
 * Two compact count indexes — {@code classifierCountIndex} and {@code languageCountIndex} — cover
 * all partitions (hot and cold) and survive eviction. They let {@code nodesByClassifier} and {@code
 * nodesByLanguage} return accurate totals without loading cold partitions.
 *
 * <p><b>Thread safety:</b> same caveats as {@link RepositoryData} — mutations are not thread-safe.
 */
class DiskBackedRepositoryData {

  private static final ProtoBufSerialization SERIALIZATION = new ProtoBufSerialization();

  @NotNull final RepositoryConfiguration configuration;

  /** All partition IDs known to this repository (hot + cold). */
  final List<String> partitionIDs = new ArrayList<>();

  /**
   * Access-order LinkedHashMap gives LRU eviction: the eldest entry is the least-recently accessed
   * partition and is evicted to disk when the hot tier is full.
   */
  private final Map<String, RepositoryData> hotPartitions;

  private final Set<String> coldPartitionIDs = new HashSet<>();

  /**
   * Bloom filter per cold partition, built at eviction time. A "definitely absent" answer lets us
   * skip the disk read entirely during cold-node lookup.
   */
  private final Map<String, PartitionBloomFilter> coldPartitionFilters = new HashMap<>();

  private final Set<String> dirtyPartitions = new HashSet<>();

  /**
   * nodeId → partitionId for nodes currently in hot partitions. May have stale entries for deleted
   * nodes; entries are validated and cleaned up on lookup.
   */
  private final Map<String, String> hotNodeIndex = new HashMap<>();

  /**
   * Compact count index covering ALL partitions (hot + cold). ClassifierKey → (partitionId → node
   * count in that partition). Kept in sync on every mutation; entries survive eviction.
   */
  private final Map<ClassifierKey, Map<String, Integer>> classifierCountIndex = new HashMap<>();

  /**
   * Compact count index covering ALL partitions (hot + cold). language string → (partitionId → node
   * count in that partition).
   */
  private final Map<String, Map<String, Integer>> languageCountIndex = new HashMap<>();

  private final Path repoDir;
  private final int maxHotPartitions;
  private int currentVersion = 0;
  private int nextId = 1;

  DiskBackedRepositoryData(@NotNull RepositoryConfiguration configuration) {
    this(configuration, createDefaultRepoDir(configuration.getName()), 200);
  }

  private static Path createDefaultRepoDir(String repositoryName) {
    try {
      return Files.createTempDirectory("lionweb-diskbacked-" + sanitize(repositoryName) + "-");
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private static String sanitize(String name) {
    return name.replaceAll("[^a-zA-Z0-9._-]", "_");
  }

  DiskBackedRepositoryData(
      @NotNull RepositoryConfiguration configuration, @NotNull Path repoDir, int maxHotPartitions) {
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

  void addPartition(
      @NotNull String partitionId, @NotNull List<SerializedClassifierInstance> nodes) {
    if (!partitionIDs.contains(partitionId)) {
      partitionIDs.add(partitionId);
    }
    coldPartitionIDs.remove(partitionId);
    coldPartitionFilters.remove(partitionId);
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
    coldPartitionFilters.remove(partitionId);
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
      rebuildPartitionCountIndices(pid, rd);
    }
  }

  void retrieve(
      @NotNull String nodeId, int limit, @NotNull List<SerializedClassifierInstance> result) {
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
   * Returns the hot {@link RepositoryData} that contains the given node, loading its partition from
   * disk if necessary. Marks the partition dirty since the caller intends to mutate the returned
   * node in-place.
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
   * Returns accurate totals across ALL partitions (hot + cold) using the compact count index. Node
   * ID samples in the result are drawn from hot partitions only.
   */
  Map<ClassifierKey, ClassifierResult> nodesByClassifier(@Nullable Integer limit) {
    int actualLimit = (limit != null) ? limit : Integer.MAX_VALUE;

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
   * Returns accurate totals across ALL partitions (hot + cold) using the compact count index. Node
   * ID samples in the result are drawn from hot partitions only.
   */
  Map<String, ClassifierResult> nodesByLanguage(@Nullable Integer limit) {
    int actualLimit = (limit != null) ? limit : Integer.MAX_VALUE;

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

    Map<String, ClassifierResult> result = new HashMap<>();
    for (Map.Entry<String, Map<String, Integer>> entry : languageCountIndex.entrySet()) {
      int total = 0;
      for (int c : entry.getValue().values()) total += c;
      Set<String> ids = hotIds.getOrDefault(entry.getKey(), Collections.<String>emptySet());
      result.put(entry.getKey(), new ClassifierResult(ids, total));
    }
    return result;
  }

  /** Returns all node IDs currently in hot partitions. */
  Set<String> nodeIDs() {
    Set<String> result = new HashSet<>();
    for (RepositoryData rd : hotPartitions.values()) {
      result.addAll(rd.nodesByID.keySet());
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

  private void rebuildPartitionCountIndices(String partitionId, RepositoryData rd) {
    removePartitionFromCountIndices(partitionId);
    for (SerializedClassifierInstance n : rd.nodesByID.values()) {
      ClassifierKey ck =
          new ClassifierKey(n.getClassifier().getLanguage(), n.getClassifier().getKey());
      classifierCountIndex
          .computeIfAbsent(ck, k -> new HashMap<>())
          .merge(partitionId, 1, Integer::sum);
      languageCountIndex
          .computeIfAbsent(n.getClassifier().getLanguage(), k -> new HashMap<>())
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

  private String resolvePartition(
      SerializedClassifierInstance node, Map<String, SerializedClassifierInstance> batchMap) {
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
        // Parent not in batch and not hot — scan cold partitions (auto-load)
        String coldPid = findAndLoadColdPartitionFor(current);
        if (coldPid != null) return coldPid;
        break;
      }
    }
    throw new IllegalArgumentException(
        "Cannot determine partition for node "
            + node.getID()
            + ". Ensure the partition exists before storing its nodes.");
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

  /**
   * Scans cold partitions for the given node ID using Bloom filters to skip candidates that
   * definitely do not contain the node. Loads and promotes the matching partition on success.
   *
   * <p>Partition roots are short-circuited: if the node ID is itself a cold partition ID, the
   * partition is loaded directly without any Bloom filter scan. This covers the dominant access
   * pattern of {@code retrieve(partitionRootId, depth)}.
   */
  @Nullable
  private String findAndLoadColdPartitionFor(String nodeId) {
    // Fast path: partition roots are their own partition ID
    if (coldPartitionIDs.contains(nodeId)) {
      ensureHot(nodeId);
      return nodeId;
    }
    for (String coldPid : new ArrayList<>(coldPartitionIDs)) {
      PartitionBloomFilter filter = coldPartitionFilters.get(coldPid);
      if (filter != null && !filter.mightContain(nodeId)) {
        continue; // definitely not in this partition — skip disk read
      }
      // Filter says "maybe" — verify by reading the file
      Path file = partitionFile(coldPid);
      if (!Files.exists(file)) continue;
      try {
        byte[] bytes = Files.readAllBytes(file);
        SerializationChunk chunk = SERIALIZATION.deserializeToChunk(bytes);
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
    coldPartitionFilters.remove(partitionId);
    hotPartitions.put(partitionId, rd); // may trigger eviction of another partition
    for (String nid : rd.nodesByID.keySet()) {
      hotNodeIndex.put(nid, partitionId);
    }
  }

  private void evictPartition(String partitionId, RepositoryData rd) {
    if (dirtyPartitions.remove(partitionId)) {
      writeToDisk(partitionId, rd);
    }
    // Build Bloom filter while nodes are still in memory
    PartitionBloomFilter filter = new PartitionBloomFilter(rd.nodesByID.size());
    rd.nodesByID.keySet().forEach(filter::add);
    coldPartitionFilters.put(partitionId, filter);

    rd.nodesByID.keySet().forEach(hotNodeIndex::remove);
    coldPartitionIDs.add(partitionId);
    // Count index entries survive eviction — they remain valid for cold partitions
  }

  private void writeToDisk(String partitionId, RepositoryData rd) {
    List<SerializedClassifierInstance> nodes = new ArrayList<>(rd.nodesByID.values());
    if (nodes.isEmpty()) return;
    SerializationChunk chunk =
        SerializationChunk.fromNodes(configuration.getLionWebVersion(), nodes);
    byte[] bytes = SERIALIZATION.serializeToByteArray(chunk);
    try {
      Files.write(partitionFile(partitionId), bytes);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private RepositoryData loadFromDisk(String partitionId) {
    Path file = partitionFile(partitionId);
    try {
      byte[] bytes = Files.readAllBytes(file);
      SerializationChunk chunk = SERIALIZATION.deserializeToChunk(bytes);
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
    return repoDir.resolve(encoded + ".pb");
  }
}
