package io.lionweb.serialization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.lionweb.LionWebVersion;
import io.lionweb.model.Node;
import java.io.*;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("performance")
public class PerformanceTestOnSerialization {

  @Test
  public void deserializeLargeLanguage() {
    InputStream is = this.getClass().getResourceAsStream("/serialization/LargeLanguage.json");
    String json = readInputStreamToString(is);
    JsonSerialization js =
        SerializationProvider.getStandardJsonSerialization(LionWebVersion.v2023_1);

    // Baseline: 119–141 ms (trimmed); thresholds have ~50% headroom for CI variance
    performanceMeasure(() -> js.deserializeToNodes(json), 180, 220);
  }

  @Test
  public void serializeLargeLanguage() {
    InputStream is = this.getClass().getResourceAsStream("/serialization/LargeLanguage.json");
    String json = readInputStreamToString(is);
    JsonSerialization js =
        SerializationProvider.getStandardJsonSerialization(LionWebVersion.v2023_1);
    List<Node> roots =
        js.deserializeToNodes(json).stream()
            .filter(n -> n.getParent() == null)
            .collect(Collectors.toList());
    assertEquals(2, roots.size());

    // Let's create a separate JsonSerialization, just in case some caches could affect the result
    final JsonSerialization js2 =
        SerializationProvider.getStandardJsonSerialization(LionWebVersion.v2023_1);
    // Baseline: 53–59 ms (trimmed); thresholds have ~50% headroom for CI variance
    performanceMeasure(() -> js2.serializeTreesToJsonElement(roots.get(0), roots.get(1)), 80, 95);
  }

  @Test
  public void deserializeLargeLanguageMemoryAllocation() {
    InputStream is = this.getClass().getResourceAsStream("/serialization/LargeLanguage.json");
    String json = readInputStreamToString(is);
    JsonSerialization js =
        SerializationProvider.getStandardJsonSerialization(LionWebVersion.v2023_1);

    // Baseline: ~112.6 MB per call; threshold has ~20% headroom
    measureMemoryAllocation(() -> js.deserializeToNodes(json), 130 * 1024 * 1024L);
  }

  @Test
  public void serializeLargeLanguageMemoryAllocation() {
    InputStream is = this.getClass().getResourceAsStream("/serialization/LargeLanguage.json");
    String json = readInputStreamToString(is);
    JsonSerialization js =
        SerializationProvider.getStandardJsonSerialization(LionWebVersion.v2023_1);
    List<Node> roots =
        js.deserializeToNodes(json).stream()
            .filter(n -> n.getParent() == null)
            .collect(Collectors.toList());
    assertEquals(2, roots.size());

    // Let's create a separate JsonSerialization, just in case some caches could affect the result
    final JsonSerialization js2 =
        SerializationProvider.getStandardJsonSerialization(LionWebVersion.v2023_1);
    // Baseline: ~84.3 MB per call; threshold has ~20% headroom
    measureMemoryAllocation(
        () -> js2.serializeTreesToJsonElement(roots.get(0), roots.get(1)), 100 * 1024 * 1024L);
  }

  private String readInputStreamToString(InputStream inputStream) {
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
      return reader.lines().collect(Collectors.joining("\n"));
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private void performanceMeasure(Runnable runnable, long thresholdMin, long thresholdMax) {
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
    System.out.println("Range: " + min + " to " + max);
    assertTrue(
        min < thresholdMin, "Expected min time to be under " + thresholdMin + " but it was " + min);
    assertTrue(
        max < thresholdMax, "Expected max time to be under " + thresholdMax + " but it was " + max);
  }

  /**
   * Measures memory allocation per iteration using per-thread allocation tracking when available
   * (HotSpot JVM), falling back to heap delta otherwise. Outliers are trimmed the same way as in
   * {@link #performanceMeasure}.
   */
  private void measureMemoryAllocation(Runnable runnable, long thresholdMaxBytes) {
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
    System.out.println(
        "Allocation range: "
            + (min / 1024)
            + " KB to "
            + (max / 1024)
            + " KB"
            + " (threshold: "
            + (thresholdMaxBytes / 1024)
            + " KB)");
    assertTrue(
        max < thresholdMaxBytes,
        "Expected max allocation to be under "
            + (thresholdMaxBytes / 1024)
            + " KB but it was "
            + (max / 1024)
            + " KB");
  }

  /**
   * Returns the number of bytes allocated on the heap by the current thread while executing {@code
   * runnable}. On HotSpot JVMs this uses {@code com.sun.management.ThreadMXBean} for per-thread
   * accuracy; on other JVMs it falls back to a heap-delta measurement (less precise).
   */
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
    // Fallback: heap delta after encouraging GC (less accurate)
    System.gc();
    Runtime rt = Runtime.getRuntime();
    long before = rt.totalMemory() - rt.freeMemory();
    runnable.run();
    long after = rt.totalMemory() - rt.freeMemory();
    return Math.max(0L, after - before);
  }
}
