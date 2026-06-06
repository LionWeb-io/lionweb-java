package io.lionweb.serialization;

import io.lionweb.serialization.data.SerializationChunk;
import java.io.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

/**
 * JMH benchmark for protobuf byte serialization/deserialization via {@link
 * DirectProtoBufSerializer} / {@link DirectProtoBufDeserializer}.
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

    chunk = new LowLevelJsonSerialization().deserializeSerializationBlock(json);
    serializedBytes = DirectProtoBufSerializer.serialize(chunk, true);
  }

  @Benchmark
  public byte[] serializeDirect() {
    return DirectProtoBufSerializer.serialize(chunk, true);
  }

  @Benchmark
  public byte[] serializeDirectUnsorted() {
    return DirectProtoBufSerializer.serializeUnsorted(chunk, true);
  }

  @Benchmark
  public SerializationChunk deserializeDirect() throws IOException {
    return DirectProtoBufDeserializer.deserialize(serializedBytes, true);
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
