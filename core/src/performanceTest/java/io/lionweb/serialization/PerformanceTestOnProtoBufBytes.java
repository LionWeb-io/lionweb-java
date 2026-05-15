package io.lionweb.serialization;

import static org.junit.jupiter.api.Assertions.*;

import io.lionweb.serialization.data.SerializationChunk;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Performance comparison between the direct protobuf byte serializer/deserializer and the legacy
 * path that goes through protobuf-generated objects (PBChunk, PBNode, etc.).
 *
 * <p>Validates that:
 *
 * <ul>
 *   <li>Direct serialization allocates less memory than the legacy path.
 *   <li>Direct deserialization allocates less memory than the legacy path.
 * </ul>
 *
 * <p>Reports wall-clock timings and allocation rates for manual verification.
 */
@Tag("performance")
class PerformanceTestOnProtoBufBytes {

  private static SerializationChunk chunk;
  private static byte[] serializedBytes;
  private static ProtoBufSerialization pbSerialization;

  @BeforeAll
  static void setup() throws IOException {
    InputStream is =
        PerformanceTestOnProtoBufBytes.class.getResourceAsStream(
            "/serialization/LargeLanguage.json");
    assertNotNull(is, "LargeLanguage.json must be on classpath");

    String json;
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
      json = reader.lines().collect(Collectors.joining("\n"));
    }

    chunk = new LowLevelJsonSerialization().deserializeSerializationBlock(json);
    pbSerialization = SerializationProvider.getStandardProtoBufSerialization();
    serializedBytes = DirectProtoBufSerializer.serialize(chunk, true);
  }

  // ---- Serialization ----

  @Test
  void directSerializationFasterAndLessMemory() {
    Runnable directOp = () -> DirectProtoBufSerializer.serialize(chunk, true);
    Runnable legacyOp = () -> pbSerialization.serialize(chunk).toByteArray();

    long directAlloc = measureAllocation(directOp);
    long legacyAlloc = measureAllocation(legacyOp);

    System.out.printf(
        "[Serialize] direct: %,d B/op  legacy: %,d B/op  ratio: %.2fx%n",
        directAlloc, legacyAlloc, (double) legacyAlloc / directAlloc);

    // Direct path must allocate less than the legacy path
    assertTrue(
        directAlloc < legacyAlloc,
        String.format(
            "Direct serializer should allocate less than legacy: direct=%,d  legacy=%,d",
            directAlloc, legacyAlloc));
  }

  @Test
  void directSerializationTime() {
    int warmup = 20, measure = 30;
    long directMs =
        measureTime(() -> DirectProtoBufSerializer.serialize(chunk, true), warmup, measure);
    long legacyMs =
        measureTime(() -> pbSerialization.serialize(chunk).toByteArray(), warmup, measure);

    System.out.printf(
        "[Serialize time] direct: %d ms  legacy: %d ms  speedup: %.2fx%n",
        directMs, legacyMs, (double) legacyMs / directMs);
  }

  // ---- Deserialization ----

  @Test
  void directDeserializationFasterAndLessMemory() throws IOException {
    Runnable directOp =
        () -> {
          try {
            DirectProtoBufDeserializer.deserialize(serializedBytes, true);
          } catch (IOException e) {
            throw new UncheckedIOException(e);
          }
        };
    Runnable legacyOp =
        () -> {
          try {
            pbSerialization.deserializeToChunkViaPbChunk(serializedBytes);
          } catch (IOException e) {
            throw new UncheckedIOException(e);
          }
        };

    long directAlloc = measureAllocation(directOp);
    long legacyAlloc = measureAllocation(legacyOp);

    System.out.printf(
        "[Deserialize] direct: %,d B/op  legacy: %,d B/op  ratio: %.2fx%n",
        directAlloc, legacyAlloc, (double) legacyAlloc / directAlloc);

    // Direct path must allocate less than the legacy path
    assertTrue(
        directAlloc < legacyAlloc,
        String.format(
            "Direct deserializer should allocate less than legacy: direct=%,d  legacy=%,d",
            directAlloc, legacyAlloc));
  }

  @Test
  void directDeserializationTime() {
    Runnable directOp =
        () -> {
          try {
            DirectProtoBufDeserializer.deserialize(serializedBytes, true);
          } catch (IOException e) {
            throw new UncheckedIOException(e);
          }
        };
    Runnable legacyOp =
        () -> {
          try {
            pbSerialization.deserializeToChunkViaPbChunk(serializedBytes);
          } catch (IOException e) {
            throw new UncheckedIOException(e);
          }
        };

    int warmup = 20, measure = 30;
    long directMs = measureTime(directOp, warmup, measure);
    long legacyMs = measureTime(legacyOp, warmup, measure);

    System.out.printf(
        "[Deserialize time] direct: %d ms  legacy: %d ms  speedup: %.2fx%n",
        directMs, legacyMs, (double) legacyMs / directMs);
  }

  // ---- Measurement helpers ----

  private static long measureAllocation(Runnable op) {
    // Warm up the JIT
    for (int i = 0; i < 10; i++) op.run();

    List<Long> samples = new ArrayList<>(20);
    for (int i = 0; i < 20; i++) {
      samples.add(threadAllocatedBytes(op));
    }
    samples.sort(Long::compareTo);
    // Trim top and bottom 4 outliers
    List<Long> trimmed = samples.subList(4, samples.size() - 4);
    return trimmed.stream().mapToLong(Long::longValue).sum() / trimmed.size();
  }

  private static long measureTime(Runnable op, int warmupRuns, int measureRuns) {
    for (int i = 0; i < warmupRuns; i++) op.run();
    List<Long> times = new ArrayList<>(measureRuns);
    for (int i = 0; i < measureRuns; i++) {
      long t0 = System.currentTimeMillis();
      op.run();
      times.add(System.currentTimeMillis() - t0);
    }
    times.sort(Long::compareTo);
    List<Long> trimmed = times.subList(4, times.size() - 4);
    return trimmed.stream().mapToLong(Long::longValue).sum() / trimmed.size();
  }

  private static long threadAllocatedBytes(Runnable op) {
    ThreadMXBean bean = ManagementFactory.getThreadMXBean();
    if (bean instanceof com.sun.management.ThreadMXBean) {
      com.sun.management.ThreadMXBean sunBean = (com.sun.management.ThreadMXBean) bean;
      long id = Thread.currentThread().getId();
      long before = sunBean.getThreadAllocatedBytes(id);
      op.run();
      return sunBean.getThreadAllocatedBytes(id) - before;
    }
    // Fallback: GC-based heap delta (less accurate)
    System.gc();
    Runtime rt = Runtime.getRuntime();
    long before = rt.totalMemory() - rt.freeMemory();
    op.run();
    return Math.max(0L, rt.totalMemory() - rt.freeMemory() - before);
  }
}
