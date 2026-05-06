package io.lionweb.client.partitioned;

import io.lionweb.LionWebVersion;
import io.lionweb.client.api.*;
import io.lionweb.model.ClassifierInstance;
import io.lionweb.model.Node;
import io.lionweb.serialization.AbstractSerialization;
import io.lionweb.serialization.data.SerializationChunk;
import io.lionweb.serialization.data.SerializedClassifierInstance;
import io.lionweb.utils.ValidationResult;
import java.io.Closeable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A disk-backed, partition-oriented replacement for {@code InMemoryServer}.
 *
 * <p>The public API is designed to be a drop-in replacement for {@code InMemoryServer}: all method
 * names, parameters, and return types are identical. Two additional lifecycle methods are provided:
 * {@link #flush()} and {@link #close()}.
 *
 * <p>Storage is delegated to a {@link RepositoryBackend} (default: {@link DiskRepositoryBackend})
 * which persists each partition as a ProtoBuf file. Only a bounded window of partitions is held in
 * the JVM heap at any time, controlled by {@link CacheConfig}.
 *
 * <p>IOException from the backend is re-thrown as {@link UncheckedIOException} so that the method
 * signatures remain compatible with {@code InMemoryServer}.
 *
 * <p><b>Thread safety:</b> concurrent reads are safe, but mutation methods are not thread-safe
 * (same contract as {@code InMemoryServer}).
 */
public class PartitionedServer implements Closeable {

  private final Map<String, PartitionedRepositoryData> repositories = new ConcurrentHashMap<>();
  private final RepositoryBackend backend;
  private final CacheConfig cacheConfig;
  private final boolean materializeClassifierIndex;
  private final PartitionCachingPolicy cachingPolicy;

  /** Non-null only when this instance owns the directory and must delete it at shutdown. */
  private final Path ownedTempDir;

  // -------------------------------------------------------------------------
  // Constructors
  // -------------------------------------------------------------------------

  /**
   * Creates a server that stores partition files under {@code storageDir}, using the default cache
   * configuration ({@link CacheConfig#DEFAULT}). The directory is <em>not</em> deleted on close.
   */
  public PartitionedServer(@NotNull Path storageDir) {
    this(
        new DiskRepositoryBackend(storageDir),
        CacheConfig.DEFAULT,
        null,
        true,
        PartitionCachingPolicy.ALWAYS_CACHE);
  }

  /**
   * Creates a server that stores partition files under {@code storageDir} with an explicit cache
   * configuration. The directory is <em>not</em> deleted on close.
   */
  public PartitionedServer(@NotNull Path storageDir, @NotNull CacheConfig cacheConfig) {
    this(
        new DiskRepositoryBackend(storageDir),
        cacheConfig,
        null,
        true,
        PartitionCachingPolicy.ALWAYS_CACHE);
  }

  /**
   * Creates a server with a custom backend and cache configuration. Useful for testing or
   * alternative storage strategies.
   */
  public PartitionedServer(@NotNull RepositoryBackend backend, @NotNull CacheConfig cacheConfig) {
    this(backend, cacheConfig, null, true, PartitionCachingPolicy.ALWAYS_CACHE);
  }

  /**
   * Creates a temporary server that allocates its own directory under the system temp folder and
   * deletes it automatically when the JVM exits (via a shutdown hook) or when {@link #close()} is
   * called explicitly.
   *
   * <p>This constructor is convenient for tests and short-lived processing jobs that need
   * disk-backed partition storage without managing a directory lifecycle manually.
   *
   * <pre>{@code
   * try (PartitionedServer server = new PartitionedServer()) {
   *     server.createRepository(...);
   *     // use server
   * }
   * }</pre>
   */
  public PartitionedServer() {
    this(CacheConfig.DEFAULT);
  }

  /**
   * Like {@link #PartitionedServer()} but with an explicit cache configuration. A temp directory is
   * allocated automatically and deleted on {@link #close()} or JVM exit.
   */
  public PartitionedServer(@NotNull CacheConfig cacheConfig) {
    this(createTempDir(), cacheConfig, true);
  }

  /**
   * @deprecated Use {@link #PartitionedServer(CacheConfig)} instead.
   */
  public static PartitionedServer withTempStorage() {
    return new PartitionedServer();
  }

  /**
   * @deprecated Use {@link #PartitionedServer(CacheConfig)} instead.
   */
  public static PartitionedServer withTempStorage(@NotNull CacheConfig cacheConfig) {
    return new PartitionedServer(cacheConfig);
  }

  private static Path createTempDir() {
    try {
      return Files.createTempDirectory("lionweb-partitioned-");
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  /**
   * Bridges {@link #PartitionedServer(CacheConfig)} — creates backend from an already-allocated
   * dir.
   */
  private PartitionedServer(
      @NotNull Path storageDir, @NotNull CacheConfig cacheConfig, boolean ownsDir) {
    this(new DiskRepositoryBackend(storageDir), cacheConfig, ownsDir ? storageDir : null);
  }

  private PartitionedServer(
      @NotNull RepositoryBackend backend,
      @NotNull CacheConfig cacheConfig,
      @Nullable Path ownedTempDir) {
    this(backend, cacheConfig, ownedTempDir, true, PartitionCachingPolicy.ALWAYS_CACHE);
  }

  private PartitionedServer(
      @NotNull RepositoryBackend backend,
      @NotNull CacheConfig cacheConfig,
      @Nullable Path ownedTempDir,
      boolean materializeClassifierIndex,
      @NotNull PartitionCachingPolicy cachingPolicy) {
    this.backend = backend;
    this.cacheConfig = cacheConfig;
    this.ownedTempDir = ownedTempDir;
    this.materializeClassifierIndex = materializeClassifierIndex;
    this.cachingPolicy = cachingPolicy;
    if (ownedTempDir != null) {
      Runtime.getRuntime().addShutdownHook(new Thread(() -> deleteDirectoryQuietly(ownedTempDir)));
    }
  }

  public PartitionedServer(
      @NotNull RepositoryBackend backend,
      @NotNull CacheConfig cacheConfig,
      boolean materializeClassifierIndex) {
    this(
        backend,
        cacheConfig,
        null,
        materializeClassifierIndex,
        PartitionCachingPolicy.ALWAYS_CACHE);
  }

  /**
   * Creates a server that stores partition files under {@code storageDir} with an explicit cache
   * configuration and caching policy. The directory is <em>not</em> deleted on close.
   */
  public PartitionedServer(
      @NotNull Path storageDir,
      @NotNull CacheConfig cacheConfig,
      @NotNull PartitionCachingPolicy cachingPolicy) {
    this(new DiskRepositoryBackend(storageDir), cacheConfig, null, true, cachingPolicy);
  }

  /**
   * Creates a server with a custom backend, cache configuration, and caching policy. Useful when
   * some partitions should be stored and immediately evicted from the heap (e.g. write-once
   * archives).
   */
  public PartitionedServer(
      @NotNull RepositoryBackend backend,
      @NotNull CacheConfig cacheConfig,
      @NotNull PartitionCachingPolicy cachingPolicy) {
    this(backend, cacheConfig, null, true, cachingPolicy);
  }

  // -------------------------------------------------------------------------
  // Repository management
  // -------------------------------------------------------------------------

  public @NotNull RepositoryConfiguration getRepositoryConfiguration(
      @NotNull String repositoryName) {
    return getRepository(repositoryName).configuration;
  }

  public @NotNull Set<RepositoryConfiguration> listRepositories() {
    return repositories.values().stream().map(r -> r.configuration).collect(Collectors.toSet());
  }

  public void createRepository(@NotNull RepositoryConfiguration repositoryConfiguration) {
    Objects.requireNonNull(repositoryConfiguration);
    if (repositoryConfiguration.getHistorySupport() == HistorySupport.ENABLED) {
      throw new IllegalArgumentException(
          "The PartitionedServer does not support History for the time being");
    }
    repositories.put(
        repositoryConfiguration.getName(),
        new PartitionedRepositoryData(
            repositoryConfiguration,
            backend,
            cacheConfig,
            materializeClassifierIndex,
            cachingPolicy));
  }

  public void deleteRepository(@NotNull String repositoryName) {
    Objects.requireNonNull(repositoryName);
    if (!repositories.containsKey(repositoryName)) {
      throw new IllegalArgumentException("Repository not found: " + repositoryName);
    }
    try {
      PartitionedRepositoryData data = repositories.remove(repositoryName);
      data.close();
      backend.deleteRepository(repositoryName);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  // -------------------------------------------------------------------------
  // ID generation
  // -------------------------------------------------------------------------

  public @NotNull List<String> ids(@NotNull String repositoryName, int count) {
    if (count < 0) throw new IllegalArgumentException("One can ask for zero or more ids");
    return getRepository(repositoryName).ids(count);
  }

  // -------------------------------------------------------------------------
  // Partition management
  // -------------------------------------------------------------------------

  public @NotNull List<String> listPartitionIDs(@NotNull String repositoryName) {
    Objects.requireNonNull(repositoryName);
    return getRepository(repositoryName).partitionIds;
  }

  public @NotNull RepositoryVersionToken createPartitionFromChunk(
      @NotNull String repositoryName, @NotNull List<SerializedClassifierInstance> partitions) {
    Objects.requireNonNull(partitions);
    PartitionedRepositoryData data = getRepository(repositoryName);
    try {
      data.createPartition(partitions);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    return data.bumpVersion();
  }

  public @NotNull RepositoryVersionToken createPartition(
      @NotNull String repositoryName,
      @NotNull Node partition,
      @NotNull AbstractSerialization serialization) {
    Objects.requireNonNull(repositoryName);
    Objects.requireNonNull(partition);
    Objects.requireNonNull(serialization);
    if (partition.getParent() != null) {
      throw new IllegalArgumentException("Partition should not have a parent");
    }
    SerializationChunk chunk = serialization.serializeNodesToSerializationChunk(partition);
    return createPartitionFromChunk(repositoryName, chunk.getClassifierInstances());
  }

  public @NotNull RepositoryVersionToken deletePartitions(
      @NotNull String repositoryName, @NotNull List<String> partitionIds) {
    Objects.requireNonNull(partitionIds);
    PartitionedRepositoryData data = getRepository(repositoryName);
    try {
      data.deletePartitions(partitionIds);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    // Also remove from backend
    for (String pid : partitionIds) {
      try {
        backend.deletePartition(repositoryName, pid);
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    }
    return data.bumpVersion();
  }

  // -------------------------------------------------------------------------
  // Node storage and retrieval
  // -------------------------------------------------------------------------

  public @NotNull List<SerializedClassifierInstance> retrieve(
      @NotNull String repositoryName, @NotNull List<String> nodeIds, int limit) {
    Objects.requireNonNull(repositoryName);
    PartitionedRepositoryData data = getRepository(repositoryName);
    List<SerializedClassifierInstance> retrieved = new ArrayList<>();
    try {
      for (String nodeId : nodeIds) {
        data.retrieve(nodeId, limit, retrieved);
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    return retrieved;
  }

  public @Nullable ClassifierInstance<?> retrieveAsClassifierInstance(
      @NotNull String repositoryName,
      @NotNull String nodeId,
      @NotNull AbstractSerialization serialization) {
    Objects.requireNonNull(repositoryName);
    Objects.requireNonNull(nodeId);
    Objects.requireNonNull(serialization);
    List<SerializedClassifierInstance> serializedNodes =
        retrieve(repositoryName, Collections.singletonList(nodeId), 1);
    if (serializedNodes.isEmpty()) return null;
    LionWebVersion version = getRepository(repositoryName).configuration.getLionWebVersion();
    List<ClassifierInstance<?>> nodes =
        serialization.deserializeSerializationChunk(
            SerializationChunk.fromNodes(version, serializedNodes));
    return nodes.stream().filter(n -> Objects.equals(n.getID(), nodeId)).findFirst().orElse(null);
  }

  public @NotNull RepositoryVersionToken store(
      @NotNull String repositoryName, @NotNull List<SerializedClassifierInstance> nodes) {
    Objects.requireNonNull(repositoryName);
    Objects.requireNonNull(nodes);
    PartitionedRepositoryData data = getRepository(repositoryName);
    try {
      data.store(nodes);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    return data.bumpVersion();
  }

  // -------------------------------------------------------------------------
  // Inspection
  // -------------------------------------------------------------------------

  public @NotNull Map<ClassifierKey, ClassifierResult> nodesByClassifier(
      @NotNull String repositoryName) {
    return nodesByClassifier(repositoryName, Integer.MAX_VALUE);
  }

  public @NotNull Map<ClassifierKey, ClassifierResult> nodesByClassifier(
      @NotNull String repositoryName, @Nullable Integer limit) {
    return getRepository(repositoryName).nodesByClassifier(limit);
  }

  public @NotNull Map<String, ClassifierResult> nodesByLanguage(@NotNull String repositoryName) {
    return nodesByLanguage(repositoryName, Integer.MAX_VALUE);
  }

  public @NotNull Map<String, ClassifierResult> nodesByLanguage(
      @NotNull String repositoryName, @Nullable Integer limit) {
    return getRepository(repositoryName).nodesByLanguage(limit);
  }

  public ClassifierResult nodesByClassifier(@NotNull String repositoryName, ClassifierKey key) {
    return getRepository(repositoryName).nodesByClassifier(null, key);
  }

  // -------------------------------------------------------------------------
  // Consistency
  // -------------------------------------------------------------------------

  public @NotNull ValidationResult checkConsistency() {
    ValidationResult result = new ValidationResult();
    try {
      for (PartitionedRepositoryData data : repositories.values()) {
        ValidationResult partial = data.checkConsistency();
        result.getIssues().addAll(partial.getIssues());
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    return result;
  }

  // -------------------------------------------------------------------------
  // Lifecycle
  // -------------------------------------------------------------------------

  /** Writes all dirty partitions to disk without closing the server. */
  public void flush() {
    try {
      for (PartitionedRepositoryData data : repositories.values()) {
        data.flush();
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  /**
   * Flushes all dirty partitions and releases resources. After calling {@code close()}, the server
   * must not be used.
   *
   * <p>If this server was created via {@link #withTempStorage()}, the temporary directory is also
   * deleted here and the JVM shutdown hook is cancelled.
   */
  @Override
  public void close() {
    try {
      for (PartitionedRepositoryData data : repositories.values()) {
        data.close();
      }
      backend.close();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    if (ownedTempDir != null) {
      deleteDirectoryQuietly(ownedTempDir);
    }
  }

  // -------------------------------------------------------------------------
  // Private helpers
  // -------------------------------------------------------------------------

  private static void deleteDirectoryQuietly(Path dir) {
    try {
      if (!Files.exists(dir)) return;
      java.nio.file.Files.walk(dir)
          .sorted(java.util.Comparator.reverseOrder())
          .forEach(
              p -> {
                try {
                  Files.deleteIfExists(p);
                } catch (IOException ignored) {
                }
              });
    } catch (IOException ignored) {
    }
  }

  private @NotNull PartitionedRepositoryData getRepository(@NotNull String repositoryName) {
    Objects.requireNonNull(repositoryName);
    PartitionedRepositoryData data = repositories.get(repositoryName);
    if (data == null) {
      throw new IllegalArgumentException("Cannot find repository named " + repositoryName);
    }
    return data;
  }
}
