package io.lionweb.serialization;

import io.lionweb.LionWebVersion;
import io.lionweb.model.ClassifierInstance;
import io.lionweb.model.Node;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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
 * JMH benchmark comparing JSON, ProtoBuf, and LionBin serialization formats across three
 * dimensions:
 *
 * <ul>
 *   <li><b>Serialization time</b> (µs/op) — {@code serializeJson/Protobuf/LionBin}
 *   <li><b>Deserialization time</b> (µs/op) — {@code deserializeJson/Protobuf/LionBin}
 *   <li><b>Allocation per operation</b> (bytes/op) — reported by {@link GCProfiler} as {@code
 *       gc.alloc.rate.norm} when run via {@link #main}
 * </ul>
 *
 * <p>Payload sizes (bytes) are printed once per trial in {@link #setup}. The GC churn metrics
 * ({@code gc.churn.*}) indicate short-lived-object pressure from each format.
 *
 * <p>Input: {@code /serialization/LargeLanguage.json} (same fixture used by the existing
 * benchmarks). All nodes are collected into a flat list once during setup so that the serialisation
 * benchmarks measure the format's work, not the tree-traversal step.
 *
 * <p>Run standalone (recommended — includes GCProfiler):
 *
 * <pre>
 *   ./gradlew :core:compilePerformanceTestJava
 *   java -cp ... io.lionweb.serialization.SerializationFormatComparisonBenchmark
 * </pre>
 *
 * Or drive via {@code main()} below.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 3, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Thread)
public class SerializationFormatComparisonBenchmark {

  /**
   * All domain nodes, pre-collected into a flat list. Fed to {@code serializeNodes*} so the
   * benchmark measures the serialisation format's work without tree-traversal overhead.
   */
  private List<ClassifierInstance<?>> allNodes;

  // Pre-serialized payloads for the deserialization benchmarks.
  private String jsonPayload;
  private byte[] protobufPayload;
  private byte[] lionbinPayload;

  // Serializer instances, kept separate from the setup serializer so their
  // internal caches do not carry state from the setup phase into measurements.
  private JsonSerialization jsonSer;
  private ProtoBufSerialization protobufSer;
  private LionBinSerialization lionbinSer;

  @Setup(Level.Trial)
  public void setup() {
    InputStream is = getClass().getResourceAsStream("/serialization/LargeLanguage.json");
    if (is == null) {
      throw new IllegalStateException("LargeLanguage.json not found on classpath");
    }
    String json;
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
      json = reader.lines().collect(Collectors.joining("\n"));
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }

    // Deserialize to domain nodes (setup only — not measured).
    JsonSerialization setupSer =
        SerializationProvider.getStandardJsonSerialization(LionWebVersion.v2023_1);
    List<Node> nodes = setupSer.deserializeToNodes(json);
    allNodes = new ArrayList<>(nodes);

    // Fresh serialisers for benchmarking.
    jsonSer = SerializationProvider.getStandardJsonSerialization(LionWebVersion.v2023_1);
    protobufSer = SerializationProvider.getStandardProtoBufSerialization(LionWebVersion.v2023_1);
    lionbinSer = SerializationProvider.getStandardLionBinSerialization(LionWebVersion.v2023_1);

    // Pre-serialise payloads for the deserialization benchmarks.
    jsonPayload = jsonSer.serializeNodesToJsonString(allNodes);
    protobufPayload = protobufSer.serializeNodesToByteArray(allNodes);
    lionbinPayload = lionbinSer.serializeNodesToByteArray(allNodes);

    // Print payload sizes once per trial so they appear in the output.
    int jsonBytes = jsonPayload.getBytes(StandardCharsets.UTF_8).length;
    System.out.printf("%n=== Payload sizes  (%,d nodes) ===%n", allNodes.size());
    System.out.printf("  %-10s  %,10d bytes%n", "JSON", jsonBytes);
    System.out.printf(
        "  %-10s  %,10d bytes  (%5.1f%% of JSON)%n",
        "ProtoBuf", protobufPayload.length, 100.0 * protobufPayload.length / jsonBytes);
    System.out.printf(
        "  %-10s  %,10d bytes  (%5.1f%% of JSON)%n",
        "LionBin", lionbinPayload.length, 100.0 * lionbinPayload.length / jsonBytes);
    System.out.println();
  }

  // ===========================================================================
  // Serialization benchmarks: domain nodes → wire format
  // ===========================================================================

  @Benchmark
  public String serializeJson() {
    return jsonSer.serializeNodesToJsonString(allNodes);
  }

  @Benchmark
  public byte[] serializeProtobuf() {
    return protobufSer.serializeNodesToByteArray(allNodes);
  }

  @Benchmark
  public byte[] serializeLionBin() {
    return lionbinSer.serializeNodesToByteArray(allNodes);
  }

  // ===========================================================================
  // Deserialization benchmarks: wire format → domain nodes
  // ===========================================================================

  @Benchmark
  public List<Node> deserializeJson() {
    return jsonSer.deserializeToNodes(jsonPayload);
  }

  @Benchmark
  public List<Node> deserializeProtobuf() throws IOException {
    return protobufSer.deserializeToNodes(protobufPayload);
  }

  @Benchmark
  public List<Node> deserializeLionBin() throws IOException {
    return lionbinSer.deserializeToNodes(lionbinPayload);
  }

  // ===========================================================================
  // Standalone JMH entry point — runs with GCProfiler for allocation metrics
  // ===========================================================================

  public static void main(String[] args) throws RunnerException {
    Options opts =
        new OptionsBuilder()
            .include(SerializationFormatComparisonBenchmark.class.getSimpleName())
            .addProfiler(GCProfiler.class)
            .build();
    new Runner(opts).run();
  }
}
