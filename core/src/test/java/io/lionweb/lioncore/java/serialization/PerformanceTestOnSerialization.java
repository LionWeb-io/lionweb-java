package io.lionweb.lioncore.java.serialization;

import static org.junit.Assert.assertEquals;

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
    for (int i=0;i<20;i++) {
      long t0 = System.currentTimeMillis();
      js.deserializeToNodes(json);
      long t1 = System.currentTimeMillis();
      long elapsed = t1 - t0;
      // Elapsed: 879 ms
      System.out.println("Elapsed: " + elapsed + " ms");
      elapsedList.add(elapsed);
    }
    elapsedList = elapsedList.stream().sorted().collect(Collectors.toList());
    elapsedList = elapsedList.subList(1, elapsedList.size() - 1);
    assertEquals(18, elapsedList.size());
    System.out.println("Range: " + elapsedList.get(0) + " to " + elapsedList.get(17));
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
