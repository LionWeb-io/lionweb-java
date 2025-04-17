package io.lionweb.lioncore.java.serialization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import io.lionweb.lioncore.java.LionWebVersion;
import io.lionweb.lioncore.java.model.Node;
import java.io.*;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;

public class PerformanceTestOnSerialization {

  @Test
  public void deserializeLargeLanguage() {
    InputStream is = this.getClass().getResourceAsStream("/serialization/LargeLanguage.json");
    String json = readInputStreamToString(is);
    JsonSerialization js =
        SerializationProvider.getStandardJsonSerialization(LionWebVersion.v2023_1);

    performanceMeasure(() -> js.deserializeToNodes(json), 250, 350);
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
    performanceMeasure(
        () -> js2.serializeTreesToJsonElement(roots.get(0), roots.get(1)), 6500, 7000);
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
    int N_TOP_REMOVED = 3;
    int N_BOTTOM_REMOVED = 3;
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
        "Expected min time to be under " + thresholdMin + " but it was " + min, min < thresholdMin);
    assertTrue(
        "Expected max time to be under " + thresholdMax + " but it was " + max, max < thresholdMax);
  }
}
