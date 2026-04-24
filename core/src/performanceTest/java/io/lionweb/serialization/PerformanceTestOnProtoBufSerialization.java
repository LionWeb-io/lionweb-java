package io.lionweb.serialization;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.lionweb.serialization.data.SerializationChunk;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Performance tests for the ProtoBuf serialization path, mirroring the methodology of {@link
 * PerformanceTestOnSerialization}.
 *
 * <p>These tests are the primary vehicle for measuring the impact of the {@link
 * io.lionweb.serialization.data.ClassifierSchema} optimization. The schema-backed compact
 * representation is only activated in the ProtoBuf deserialization path ({@link
 * ProtoBufSerialization#deserializeToChunk}), so the JSON benchmarks in {@link
 * PerformanceTestOnSerialization} do not exercise it.
 *
 * <p>The benchmark measures the chunk-level round-trip ({@link SerializationChunk} ↔ ProtoBuf
 * bytes) rather than the high-level node round-trip, which isolates the deserialization code we
 * optimized from the type-registration machinery.
 */
@Tag("performance")
public class PerformanceTestOnProtoBufSerialization {

  /**
   * Loads LargeLanguage.json, converts it to a {@link SerializationChunk} via {@link
   * LowLevelJsonSerialization}, then serializes that chunk to ProtoBuf bytes once. This gives us a
   * realistic payload without needing language type registrations.
   */
  private byte[] buildProtoBufBytes() {
    InputStream is = getClass().getResourceAsStream("/serialization/LargeLanguage.json");
    if (is == null) throw new IllegalStateException("LargeLanguage.json not found on classpath");
    String json;
    try (BufferedReader r = new BufferedReader(new InputStreamReader(is))) {
      json = r.lines().collect(Collectors.joining("\n"));
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    // Parse JSON → SerializationChunk (low-level, no type registration needed)
    SerializationChunk chunk = new LowLevelJsonSerialization().deserializeSerializationBlock(json);
    // Convert to ProtoBuf bytes
    return new ProtoBufSerialization().serializeToByteArray(chunk);
  }

  /**
   * Benchmarks ProtoBuf → {@link SerializationChunk} deserialization time.
   *
   * <p>This is the hot path that the {@link io.lionweb.serialization.data.ClassifierSchema}
   * optimization targets: every {@link io.lionweb.serialization.data.SerializedClassifierInstance}
   * in the resulting chunk is created via {@link
   * io.lionweb.serialization.data.SerializedClassifierInstance#compact}, avoiding wrapper-object
   * allocation for features.
   */
  @Test
  public void deserializeLargeLanguageProtoBuf() throws Exception {
    byte[] pbBytes = buildProtoBufBytes();
    ProtoBufSerialization pbs = new ProtoBufSerialization();

    System.out.println("ProtoBuf payload size: " + pbBytes.length + " bytes");
    performanceMeasure(
        () -> {
          try {
            pbs.deserializeToChunk(pbBytes);
          } catch (IOException e) {
            throw new UncheckedIOException(e);
          }
        });
  }

  /** Benchmarks per-call heap allocation during ProtoBuf → {@link SerializationChunk}. */
  @Test
  public void deserializeLargeLanguageProtoBufMemoryAllocation() throws Exception {
    byte[] pbBytes = buildProtoBufBytes();
    ProtoBufSerialization pbs = new ProtoBufSerialization();

    System.out.println("ProtoBuf payload size: " + pbBytes.length + " bytes");
    measureMemoryAllocation(
        () -> {
          try {
            pbs.deserializeToChunk(pbBytes);
          } catch (IOException e) {
            throw new UncheckedIOException(e);
          }
        });
  }

  // --------------------------------------------------------------------------
  // Shared infrastructure (same methodology as PerformanceTestOnSerialization)
  // --------------------------------------------------------------------------

  private void performanceMeasure(Runnable runnable) {
    List<Long> elapsedList = new ArrayList<>();
    int N_ITERATIONS = 25;
    int N_TOP_REMOVED = 4;
    int N_BOTTOM_REMOVED = 4;
    for (int i = 0; i < N_ITERATIONS; i++) {
      long t0 = System.currentTimeMillis();
      runnable.run();
      long t1 = System.currentTimeMillis();
      long elapsed = t1 - t0;
      System.out.println("Elapsed: " + elapsed + " ms");
      elapsedList.add(elapsed);
    }
    elapsedList = elapsedList.stream().sorted().collect(Collectors.toList());
    elapsedList = elapsedList.subList(N_TOP_REMOVED, elapsedList.size() - N_BOTTOM_REMOVED);
    assertEquals(N_ITERATIONS - N_TOP_REMOVED - N_BOTTOM_REMOVED, elapsedList.size());
    long min = elapsedList.get(0);
    long max = elapsedList.get(elapsedList.size() - 1);
    System.out.println("Range: " + min + " ms to " + max + " ms");
  }

  private void measureMemoryAllocation(Runnable runnable) {
    List<Long> allocationList = new ArrayList<>();
    int N_ITERATIONS = 25;
    int N_TOP_REMOVED = 4;
    int N_BOTTOM_REMOVED = 4;
    for (int i = 0; i < N_ITERATIONS; i++) {
      long allocated = measureAllocatedBytes(runnable);
      System.out.println("Allocated: " + (allocated / 1024) + " KB");
      allocationList.add(allocated);
    }
    allocationList = allocationList.stream().sorted().collect(Collectors.toList());
    allocationList =
        allocationList.subList(N_TOP_REMOVED, allocationList.size() - N_BOTTOM_REMOVED);
    assertEquals(N_ITERATIONS - N_TOP_REMOVED - N_BOTTOM_REMOVED, allocationList.size());
    long min = allocationList.get(0);
    long max = allocationList.get(allocationList.size() - 1);
    System.out.println("Allocation range: " + (min / 1024) + " KB to " + (max / 1024) + " KB");
  }

  private long measureAllocatedBytes(Runnable runnable) {
    ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
    if (threadBean instanceof com.sun.management.ThreadMXBean) {
      com.sun.management.ThreadMXBean sunBean = (com.sun.management.ThreadMXBean) threadBean;
      long threadId = Thread.currentThread().getId();
      long before = sunBean.getThreadAllocatedBytes(threadId);
      runnable.run();
      long after = sunBean.getThreadAllocatedBytes(threadId);
      return after - before;
    }
    System.gc();
    Runtime rt = Runtime.getRuntime();
    long before = rt.totalMemory() - rt.freeMemory();
    runnable.run();
    long after = rt.totalMemory() - rt.freeMemory();
    return Math.max(0L, after - before);
  }
}
