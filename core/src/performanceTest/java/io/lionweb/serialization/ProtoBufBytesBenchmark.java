package io.lionweb.serialization;

import io.lionweb.LionWebVersion;
import io.lionweb.model.Node;
import io.lionweb.serialization.data.SerializationChunk;
import java.io.*;
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
 * JMH benchmark comparing the direct protobuf byte serialization/deserialization (via {@link
 * DirectProtoBufSerializer} / {@link DirectProtoBufDeserializer}) against the previous path that
 * went through protobuf-generated objects (PBChunk, PBNode, etc.).
 *
 * <p>Two pairs of benchmarks:
 *
 * <ul>
 *   <li>{@link #serializeDirect} / {@link #serializeViaPbChunk} — serialize a {@link
 *       SerializationChunk} to bytes.
 *   <li>{@link #deserializeDirect} / {@link #deserializeViaPbChunk} — parse bytes back to a {@link
 *       SerializationChunk}.
 * </ul>
 *
 * <p>Run with {@link #main} to include the GC allocation profiler.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 2, timeUnit = TimeUnit.SECONDS)
@Fork(2)
@State(Scope.Thread)
public class ProtoBufBytesBenchmark {

  /** Pre-built SerializationChunk from LargeLanguage.json — the input to serialization. */
  private SerializationChunk chunk;

  /** Bytes produced by the direct serializer — the input to deserialization. */
  private byte[] serializedBytes;

  /** Reusable ProtoBufSerialization instance. */
  private ProtoBufSerialization pbSerialization;

  @Setup(Level.Trial)
  public void setup() throws IOException {
    InputStream is = getClass().getResourceAsStream("/serialization/LargeLanguage.json");
    if (is == null) {
      throw new IllegalStateException(
          "Resource /serialization/LargeLanguage.json not found on classpath");
    }
    String json;
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
      json = reader.lines().collect(Collectors.joining("\n"));
    }

    JsonSerialization setupJs =
        SerializationProvider.getStandardJsonSerialization(LionWebVersion.v2023_1);
    List<Node> nodes = setupJs.deserializeToNodes(json);

    pbSerialization = SerializationProvider.getStandardProtoBufSerialization();

    // Build the SerializationChunk once; both benchmarks reuse it
    chunk = pbSerialization.serializeNodesToSerializationChunk(new ArrayList<>(nodes));

    // Pre-serialize so deserialization benchmarks measure only the parsing side
    serializedBytes = DirectProtoBufSerializer.serialize(chunk, true);
  }

  // ---- Serialization benchmarks ----

  /** Direct serializer: SerializationChunk → byte[] without creating PBChunk/PBNode objects. */
  @Benchmark
  public byte[] serializeDirect() {
    return DirectProtoBufSerializer.serialize(chunk, true);
  }

  /** Legacy path: SerializationChunk → PBChunk → byte[]. */
  @Benchmark
  public byte[] serializeViaPbChunk() {
    return pbSerialization.serialize(chunk).toByteArray();
  }

  // ---- Deserialization benchmarks ----

  /** Direct deserializer: byte[] → SerializationChunk without creating PBChunk/PBNode objects. */
  @Benchmark
  public SerializationChunk deserializeDirect() throws IOException {
    return DirectProtoBufDeserializer.deserialize(serializedBytes, true);
  }

  /** Legacy path: byte[] → PBChunk → SerializationChunk. */
  @Benchmark
  public SerializationChunk deserializeViaPbChunk() throws IOException {
    return pbSerialization.deserializeToChunkViaPbChunk(serializedBytes);
  }

  public static void main(String[] args) throws RunnerException {
    Options opts =
        new OptionsBuilder()
            .include(ProtoBufBytesBenchmark.class.getSimpleName())
            .addProfiler(GCProfiler.class)
            .build();
    new Runner(opts).run();
  }
}
