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

    List<Long> elapsedList = new ArrayList<>();
    int N_ITERATIONS = 25;
    int N_TOP_REMOVED = 3;
    int N_BOTTOM_REMOVED = 3;
    for (int i = 0; i < N_ITERATIONS; i++) {
      long t0 = System.currentTimeMillis();
      js.deserializeToNodes(json);
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
    // Range: 233 to 282
    // Range: 193 to 220
    long THRESHOLD_MIN = 250;
    long THRESHOLD_MAX = 300;
    System.out.println("Range: " + min + " to " + max);
    assertTrue(
        "Expected min time to be under " + THRESHOLD_MIN + " but it was " + min,
        min < THRESHOLD_MIN);
    assertTrue(
        "Expected max time to be under " + THRESHOLD_MAX + " but it was " + max,
        max < THRESHOLD_MAX);
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
    js = SerializationProvider.getStandardJsonSerialization(LionWebVersion.v2023_1);
    long t0 = System.currentTimeMillis();
    js.serializeTreesToJsonElement(roots.get(0), roots.get(1));
    long t1 = System.currentTimeMillis();
    long elapsed = t1 - t0;
    // Elapsed: 6387 ms
    System.out.println("Elapsed: " + elapsed + " ms");
  }

  private String readInputStreamToString(InputStream inputStream) {
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
      return reader.lines().collect(Collectors.joining("\n"));
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
