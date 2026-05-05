package io.lionweb.client.partitioned;

import io.lionweb.LionWebVersion;
import io.lionweb.client.api.HistorySupport;
import io.lionweb.client.api.RepositoryConfiguration;
import io.lionweb.serialization.data.MetaPointer;
import io.lionweb.serialization.data.SerializedClassifierInstance;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

/**
 * JMH benchmark for {@link PartitionedServer}.
 *
 * <p>Scenarios:
 *
 * <ul>
 *   <li>{@link #storePartition} — write one partition with many nodes.
 *   <li>{@link #retrieveWarmPartition} — read from an already-cached partition.
 *   <li>{@link #retrieveColdPartition} — read from a partition evicted from cache (disk read).
 *   <li>{@link #sequentialScanPartitions} — iterate all partitions sequentially.
 *   <li>{@link #classifierIndexQuery} — {@code nodesByClassifier()} on large repo.
 * </ul>
 *
 * <p>Run via Gradle: {@code ./gradlew :client:performanceTest --tests
 * "io.lionweb.client.partitioned.PartitionedServerBenchmark"}
 *
 * <p>Or standalone via {@link #main(String[])}.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 3, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Thread)
public class PartitionedServerBenchmark {

  private static final int NODES_PER_PARTITION = 1_000;
  private static final int PARTITION_COUNT = 20;
  private static final MetaPointer MP = MetaPointer.get("bench-lang", "1.0", "BenchNode");

  private Path tempDir;
  private PartitionedServer warmServer; // cache large enough for all partitions
  private PartitionedServer coldServer; // cache of 1 partition (forces eviction)

  @Setup(Level.Trial)
  public void setup() throws IOException {
    tempDir = Files.createTempDirectory("partitioned-bench");

    RepositoryConfiguration cfg =
        new RepositoryConfiguration("bench", LionWebVersion.v2023_1, HistorySupport.DISABLED);

    // Warm server: cache holds everything
    warmServer = new PartitionedServer(tempDir, new CacheConfig(PARTITION_COUNT + 4, 10_000_000));
    warmServer.createRepository(cfg);

    // Cold server: cache of 1 forces frequent disk I/O
    coldServer = new PartitionedServer(tempDir, new CacheConfig(1, 10_000_000));
    coldServer.createRepository(cfg);

    // Pre-populate both servers with partitions
    for (int p = 0; p < PARTITION_COUNT; p++) {
      String rootId = "p" + p;
      List<SerializedClassifierInstance> nodes = buildPartitionNodes(rootId, NODES_PER_PARTITION);
      warmServer.createPartitionFromChunk("bench", nodes);
    }
    warmServer.flush();

    // Reload into cold server from disk
    DiskRepositoryBackend disk = new DiskRepositoryBackend(tempDir);
    for (int p = 0; p < PARTITION_COUNT; p++) {
      String rootId = "p" + p;
      List<SerializedClassifierInstance> nodes = disk.loadPartition("bench", rootId);
      coldServer.createPartitionFromChunk("bench", nodes);
    }
    coldServer.flush();
  }

  @TearDown(Level.Trial)
  public void tearDown() throws IOException {
    warmServer.close();
    coldServer.close();
    deleteRecursively(tempDir);
  }

  // -------------------------------------------------------------------------
  // Benchmarks
  // -------------------------------------------------------------------------

  @Benchmark
  public Object retrieveWarmPartition() {
    return warmServer.retrieve("bench", Collections.singletonList("p0"), Integer.MAX_VALUE);
  }

  @Benchmark
  public Object retrieveColdPartition() {
    return coldServer.retrieve("bench", Collections.singletonList("p0"), Integer.MAX_VALUE);
  }

  @Benchmark
  public Object storePartition() {
    List<SerializedClassifierInstance> nodes = buildPartitionNodes("bench-store", 100);
    warmServer.store("bench", nodes);
    return nodes;
  }

  @Benchmark
  public Object classifierIndexQuery() {
    return warmServer.nodesByClassifier("bench");
  }

  @Benchmark
  public Object sequentialScanPartitions() {
    List<String> partitionIds = warmServer.listPartitionIDs("bench");
    List<SerializedClassifierInstance> all = new ArrayList<>();
    for (String pid : partitionIds) {
      all.addAll(warmServer.retrieve("bench", Collections.singletonList(pid), Integer.MAX_VALUE));
    }
    return all;
  }

  // -------------------------------------------------------------------------
  // Helpers
  // -------------------------------------------------------------------------

  private static List<SerializedClassifierInstance> buildPartitionNodes(String rootId, int count) {
    List<SerializedClassifierInstance> nodes = new ArrayList<>(count);
    SerializedClassifierInstance root = new SerializedClassifierInstance(rootId, MP);
    nodes.add(root);
    List<String> childIds = new ArrayList<>();
    for (int i = 1; i < count; i++) {
      String childId = rootId + "-n" + i;
      SerializedClassifierInstance child = new SerializedClassifierInstance(childId, MP);
      child.setParentNodeID(rootId);
      childIds.add(childId);
      nodes.add(child);
    }
    if (!childIds.isEmpty()) {
      root.unsafeAppendContainmentValue(MetaPointer.get("bench-lang", "1.0", "ch"), childIds);
    }
    return nodes;
  }

  private static void deleteRecursively(Path dir) throws IOException {
    if (!Files.exists(dir)) return;
    try (java.util.stream.Stream<Path> stream = Files.walk(dir)) {
      stream
          .sorted(Comparator.reverseOrder())
          .forEach(
              p -> {
                try {
                  Files.deleteIfExists(p);
                } catch (IOException ignored) {
                }
              });
    }
  }

  public static void main(String[] args) throws RunnerException {
    Options opts =
        new OptionsBuilder()
            .include(PartitionedServerBenchmark.class.getSimpleName())
            .addProfiler(GCProfiler.class)
            .build();
    new Runner(opts).run();
  }
}
