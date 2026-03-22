package io.lionweb.serialization;

// Gradle dependencies to add in core/build.gradle.kts:
//
//   performanceTestImplementation("org.openjdk.jmh:jmh-core:1.37")
//   performanceTestAnnotationProcessor("org.openjdk.jmh:jmh-generator-annprocess:1.37")
//
// To run from the command line:
//   ./gradlew :core:performanceTestClasses
//   java -cp core/build/classes/java/performanceTest:<classpath> \
//        org.openjdk.jmh.Main SerializeNodesToChunkBenchmark
//
// Or directly from IDEA: run SerializeNodesToChunkBenchmark.main()

import io.lionweb.LionWebVersion;
import io.lionweb.model.ClassifierInstance;
import io.lionweb.model.Node;
import io.lionweb.serialization.data.SerializationChunk;
import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

/**
 * JMH benchmark for {@link AbstractSerialization#serializeNodesToSerializationChunk}.
 *
 * <p>Two benchmarks:
 *
 * <ul>
 *   <li>{@link #serializeNodesToChunk} – the method under test, called with the flat collection of
 *       all already-materialised nodes (as happens in every real call).
 *   <li>{@link #serializeTreesToChunk} – baseline that passes only the roots: includes the extra
 *       cost of {@code collectSelfAndDescendants} + {@code LinkedHashSet}, then calls the same
 *       method.
 * </ul>
 *
 * <p>With {@code GCProfiler}, {@code main} also prints {@code gc.alloc.rate.norm} (bytes allocated
 * per operation), which is the key memory metric for comparing pre-allocation optimisations.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Thread)
public class SerializeNodesToChunkBenchmark {

  /**
   * All nodes from LargeLanguage, widened to {@code ClassifierInstance<?>} so they can be passed
   * directly to {@code serializeNodesToSerializationChunk} without an extra copy per iteration.
   */
  private Collection<ClassifierInstance<?>> allNodes;

  /** Root nodes only; used by {@link #serializeTreesToChunk} as the baseline. */
  private List<Node> roots;

  /**
   * Serializer instance reused across iterations, consistent with real-world usage. Kept separate
   * from the setup serializer so that its internal caches do not influence measurements.
   */
  private JsonSerialization serialization;

  @Setup(Level.Trial)
  public void setup() {
    InputStream is = getClass().getResourceAsStream("/serialization/LargeLanguage.json");
    if (is == null) {
      throw new IllegalStateException(
          "Resource /serialization/LargeLanguage.json not found on classpath");
    }
    String json;
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
      json = reader.lines().collect(Collectors.joining("\n"));
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }

    // Separate serializer for setup: its internal caches (registerLanguage, etc.)
    // do not interfere with the serializer used in the benchmark.
    JsonSerialization setupJs =
        SerializationProvider.getStandardJsonSerialization(LionWebVersion.v2023_1);
    List<Node> nodes = setupJs.deserializeToNodes(json);

    // ArrayList<ClassifierInstance<?>> built once per Trial; the constructor accepts
    // Collection<? extends ClassifierInstance<?>> and Node satisfies that bound.
    allNodes = new ArrayList<ClassifierInstance<?>>(nodes);
    roots = nodes.stream().filter(n -> n.getParent() == null).collect(Collectors.toList());

    serialization = SerializationProvider.getStandardJsonSerialization(LionWebVersion.v2023_1);
  }

  /**
   * Method under test: flat collection → {@link SerializationChunk}.
   *
   * <p>Optimisations measured:
   *
   * <ul>
   *   <li>{@code SerializationChunk} constructed with {@code initialCapacity = collection.size()},
   *       avoiding HashMap rehashing and ArrayList resizing.
   *   <li>The collection is copied into a {@code HashSet} once to check annotations in O(1) instead
   *       of O(n) with {@code List.contains}.
   * </ul>
   *
   * <p>Returning the result prevents the JIT from eliminating the call as dead code.
   */
  @Benchmark
  public SerializationChunk serializeNodesToChunk() {
    return serialization.serializeNodesToSerializationChunk(allNodes);
  }

  /**
   * Baseline: starts from the roots, performs tree traversal via {@code
   * collectSelfAndDescendants} (which builds a {@code LinkedHashSet}), then calls {@code
   * serializeNodesToSerializationChunk}.
   *
   * <p>The time difference relative to {@link #serializeNodesToChunk} reflects the cost of the
   * traversal and the {@code LinkedHashSet} allocation.
   */
  @Benchmark
  public SerializationChunk serializeTreesToChunk() {
    return serialization.serializeTreesToSerializationChunk(roots);
  }

  /**
   * Entry point for standalone execution with GCProfiler.
   *
   * <p>The output includes {@code gc.alloc.rate.norm} (bytes allocated per operation), which allows
   * quantifying the memory improvement introduced by pre-allocating {@code SerializationChunk}.
   */
  public static void main(String[] args) throws RunnerException {
    Options opts =
        new OptionsBuilder()
            .include(SerializeNodesToChunkBenchmark.class.getSimpleName())
            .addProfiler(GCProfiler.class)
            .build();
    new Runner(opts).run();
  }
}
