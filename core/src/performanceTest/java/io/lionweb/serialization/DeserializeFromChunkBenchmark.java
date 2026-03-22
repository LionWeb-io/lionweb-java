package io.lionweb.serialization;

import io.lionweb.LionWebVersion;
import io.lionweb.model.ClassifierInstance;
import io.lionweb.model.Node;
import io.lionweb.serialization.data.SerializationChunk;
import java.io.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

/**
 * JMH benchmark for {@link AbstractSerialization#deserializeSerializationChunk}.
 *
 * <p>Two benchmarks:
 *
 * <ul>
 *   <li>{@link #deserializeFromChunk} – the method under test, called with a pre-parsed {@link
 *       SerializationChunk} so that JSON parsing time is excluded.
 *   <li>{@link #deserializeFromJson} – baseline that starts from the raw JSON string, including the
 *       cost of {@link LowLevelJsonSerialization} parsing.
 * </ul>
 *
 * <p>With {@code GCProfiler}, {@code main} also prints {@code gc.alloc.rate.norm} (bytes allocated
 * per operation), which is the key memory metric for comparing pre-allocation optimisations.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 2, timeUnit = TimeUnit.SECONDS)
@Fork(2)
@State(Scope.Thread)
public class DeserializeFromChunkBenchmark {

  /** Pre-parsed chunk; fed directly to {@code deserializeSerializationChunk}. */
  private SerializationChunk chunk;

  /** Raw JSON string; used by {@link #deserializeFromJson} to include parsing overhead. */
  private String json;

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
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
      json = reader.lines().collect(Collectors.joining("\n"));
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }

    chunk = new LowLevelJsonSerialization().deserializeSerializationBlock(json);
    serialization = SerializationProvider.getStandardJsonSerialization(LionWebVersion.v2023_1);
  }

  /**
   * Method under test: {@link SerializationChunk} → list of {@link ClassifierInstance}.
   *
   * <p>JSON parsing is excluded; only the instantiation, sorting, and map-population phases are
   * measured. This isolates the impact of pre-sizing {@code HashMap} and {@code IdentityHashMap}.
   *
   * <p>Returning the result prevents the JIT from eliminating the call as dead code.
   */
  @Benchmark
  public List<ClassifierInstance<?>> deserializeFromChunk() {
    return serialization.deserializeSerializationChunk(chunk);
  }

  /**
   * Baseline: starts from the raw JSON string, so JSON parsing cost is included alongside
   * deserialization. The time difference relative to {@link #deserializeFromChunk} reflects the
   * cost of {@link LowLevelJsonSerialization#deserializeSerializationBlock(String)}.
   */
  @Benchmark
  public List<Node> deserializeFromJson() {
    return serialization.deserializeToNodes(json);
  }

  /**
   * Entry point for standalone execution with GCProfiler.
   *
   * <p>The output includes {@code gc.alloc.rate.norm} (bytes allocated per operation), which allows
   * quantifying the memory improvement introduced by pre-sizing the internal maps in {@code
   * deserializeClassifierInstances}.
   */
  public static void main(String[] args) throws RunnerException {
    Options opts =
        new OptionsBuilder()
            .include(DeserializeFromChunkBenchmark.class.getSimpleName())
            .addProfiler(GCProfiler.class)
            .build();
    new Runner(opts).run();
  }
}
