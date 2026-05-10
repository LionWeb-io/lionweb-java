package io.lionweb.serialization;

import io.lionweb.LionWebVersion;
import io.lionweb.language.Concept;
import io.lionweb.language.Language;
import io.lionweb.language.LionCoreBuiltins;
import io.lionweb.language.Property;
import io.lionweb.model.ClassifierInstance;
import io.lionweb.model.Node;
import io.lionweb.model.impl.DynamicNode;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/**
 * JUnit-based comparison report for JSON, ProtoBuf, and LionBin serialisation formats.
 *
 * <p>Run via:
 *
 * <pre>
 *   ./gradlew :core:performanceTest --tests SerializationFormatComparisonReport
 * </pre>
 *
 * <p>Each format is measured across five dimensions per @Test:
 *
 * <ol>
 *   <li><b>Payload size</b> — byte count of the serialised output.
 *   <li><b>Serialisation time</b> — wall-clock median over {@value #N_ITER} warm iterations.
 *   <li><b>Deserialisation time</b> — wall-clock median over {@value #N_ITER} warm iterations.
 *   <li><b>Serialisation allocation</b> — bytes allocated on the calling thread per operation
 *       (via {@code com.sun.management.ThreadMXBean}; heap-delta fallback on other JVMs).
 *   <li><b>Deserialisation allocation</b> — same, for the deserialisation direction.
 * </ol>
 *
 * <p>The first {@value #N_WARMUP} iterations are discarded as JIT warm-up; the remaining
 * {@value #N_ITER} are trimmed (removing the top and bottom {@value #N_TRIM}) before computing
 * median and range.
 */
public class SerializationFormatComparisonReport {

  private static final int N_WARMUP = 5;
  private static final int N_ITER = 20;
  private static final int N_TRIM = 3;

  // =========================================================================
  // Main comparison test
  // =========================================================================

  @Test
  public void compareFormats() throws IOException {
    // --- Load and deserialise the fixture (not measured) ---
    InputStream is = getClass().getResourceAsStream("/serialization/LargeLanguage.json");
    if (is == null) throw new IllegalStateException("LargeLanguage.json not found on classpath");
    String rawJson;
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
      rawJson = reader.lines().collect(Collectors.joining("\n"));
    }
    JsonSerialization setupSer =
        SerializationProvider.getStandardJsonSerialization(LionWebVersion.v2023_1);
    List<Node> domainNodes = setupSer.deserializeToNodes(rawJson);
    List<ClassifierInstance<?>> allNodes = new ArrayList<>(domainNodes);
    int nodeCount = allNodes.size();

    // --- Create fresh serialisers ---
    JsonSerialization jsonSer =
        SerializationProvider.getStandardJsonSerialization(LionWebVersion.v2023_1);
    ProtoBufSerialization protobufSer =
        SerializationProvider.getStandardProtoBufSerialization(LionWebVersion.v2023_1);
    LionBinSerialization lionbinSer =
        SerializationProvider.getStandardLionBinSerialization(LionWebVersion.v2023_1);

    // --- Compute serialised payloads (used for size reporting and deserialization benchmarks) ---
    String jsonPayload = jsonSer.serializeNodesToJsonString(allNodes);
    byte[] protobufPayload = protobufSer.serializeNodesToByteArray(allNodes);
    byte[] lionbinPayload = lionbinSer.serializeNodesToByteArray(allNodes);

    int jsonBytes = jsonPayload.getBytes(StandardCharsets.UTF_8).length;
    int pbBytes = protobufPayload.length;
    int lbBytes = lionbinPayload.length;

    // --- Warm up all formats (JIT stabilisation, not measured) ---
    for (int i = 0; i < N_WARMUP; i++) {
      jsonSer.serializeNodesToJsonString(allNodes);
      protobufSer.serializeNodesToByteArray(allNodes);
      lionbinSer.serializeNodesToByteArray(allNodes);
      jsonSer.deserializeToNodes(jsonPayload);
      protobufSer.deserializeToNodes(protobufPayload);
      lionbinSer.deserializeToNodes(lionbinPayload);
    }

    // --- Measure serialisation ---
    TimingResult jsonSerTime = measureTime(() -> jsonSer.serializeNodesToJsonString(allNodes));
    TimingResult pbSerTime = measureTime(() -> protobufSer.serializeNodesToByteArray(allNodes));
    TimingResult lbSerTime = measureTime(() -> lionbinSer.serializeNodesToByteArray(allNodes));

    long jsonSerAlloc = measureAlloc(() -> jsonSer.serializeNodesToJsonString(allNodes));
    long pbSerAlloc = measureAlloc(() -> protobufSer.serializeNodesToByteArray(allNodes));
    long lbSerAlloc = measureAlloc(() -> lionbinSer.serializeNodesToByteArray(allNodes));

    // --- Measure deserialisation ---
    TimingResult jsonDeserTime = measureTime(() -> jsonSer.deserializeToNodes(jsonPayload));
    TimingResult pbDeserTime =
        measureTime(
            () -> {
              try {
                protobufSer.deserializeToNodes(protobufPayload);
              } catch (IOException e) {
                throw new UncheckedIOException(e);
              }
            });
    TimingResult lbDeserTime =
        measureTime(
            () -> {
              try {
                lionbinSer.deserializeToNodes(lionbinPayload);
              } catch (IOException e) {
                throw new UncheckedIOException(e);
              }
            });

    long jsonDeserAlloc = measureAlloc(() -> jsonSer.deserializeToNodes(jsonPayload));
    long pbDeserAlloc =
        measureAlloc(
            () -> {
              try {
                protobufSer.deserializeToNodes(protobufPayload);
              } catch (IOException e) {
                throw new UncheckedIOException(e);
              }
            });
    long lbDeserAlloc =
        measureAlloc(
            () -> {
              try {
                lionbinSer.deserializeToNodes(lionbinPayload);
              } catch (IOException e) {
                throw new UncheckedIOException(e);
              }
            });

    // --- Print comparison table ---
    System.out.println();
    System.out.printf("╔══════════════════════════════════════════════════════════════════════════╗%n");
    System.out.printf("║  Serialization format comparison  (%,d nodes from LargeLanguage.json)  ║%n", nodeCount);
    System.out.printf("╠══════════════════════════╦══════════════╦══════════════╦════════════════╣%n");
    System.out.printf("║  Metric                  ║     JSON     ║   ProtoBuf   ║    LionBin     ║%n");
    System.out.printf("╠══════════════════════════╬══════════════╬══════════════╬════════════════╣%n");

    // Sizes
    System.out.printf("║  Payload size (bytes)    ║ %,11d ║ %,11d ║ %,13d ║%n",
        jsonBytes, pbBytes, lbBytes);
    System.out.printf("║  Payload vs JSON         ║     100.0%%   ║    %5.1f%%    ║      %5.1f%%     ║%n",
        100.0 * pbBytes / jsonBytes, 100.0 * lbBytes / jsonBytes);

    System.out.printf("╠══════════════════════════╬══════════════╬══════════════╬════════════════╣%n");

    // Serialisation timing
    System.out.printf("║  Serialize median (ms)   ║ %,11.1f ║ %,11.1f ║ %,13.1f ║%n",
        jsonSerTime.medianMs, pbSerTime.medianMs, lbSerTime.medianMs);
    System.out.printf("║  Serialize range (ms)    ║ %5.1f–%5.1f  ║ %5.1f–%5.1f  ║  %5.1f–%5.1f   ║%n",
        jsonSerTime.minMs, jsonSerTime.maxMs,
        pbSerTime.minMs, pbSerTime.maxMs,
        lbSerTime.minMs, lbSerTime.maxMs);
    System.out.printf("║  Serialize vs JSON       ║     100%%     ║    %5.0f%%    ║      %5.0f%%     ║%n",
        100.0 * pbSerTime.medianMs / jsonSerTime.medianMs,
        100.0 * lbSerTime.medianMs / jsonSerTime.medianMs);

    System.out.printf("╠══════════════════════════╬══════════════╬══════════════╬════════════════╣%n");

    // Deserialisation timing
    System.out.printf("║  Deserialize median (ms) ║ %,11.1f ║ %,11.1f ║ %,13.1f ║%n",
        jsonDeserTime.medianMs, pbDeserTime.medianMs, lbDeserTime.medianMs);
    System.out.printf("║  Deserialize range (ms)  ║ %5.1f–%5.1f  ║ %5.1f–%5.1f  ║  %5.1f–%5.1f   ║%n",
        jsonDeserTime.minMs, jsonDeserTime.maxMs,
        pbDeserTime.minMs, pbDeserTime.maxMs,
        lbDeserTime.minMs, lbDeserTime.maxMs);
    System.out.printf("║  Deserialize vs JSON     ║     100%%     ║    %5.0f%%    ║      %5.0f%%     ║%n",
        100.0 * pbDeserTime.medianMs / jsonDeserTime.medianMs,
        100.0 * lbDeserTime.medianMs / jsonDeserTime.medianMs);

    System.out.printf("╠══════════════════════════╬══════════════╬══════════════╬════════════════╣%n");

    // Allocation per op
    System.out.printf("║  Serialize alloc (MB)    ║ %,11.1f ║ %,11.1f ║ %,13.1f ║%n",
        jsonSerAlloc / 1e6, pbSerAlloc / 1e6, lbSerAlloc / 1e6);
    System.out.printf("║  Serialize alloc vs JSON ║     100%%     ║    %5.0f%%    ║      %5.0f%%     ║%n",
        100.0 * pbSerAlloc / jsonSerAlloc, 100.0 * lbSerAlloc / jsonSerAlloc);
    System.out.printf("║  Deserialize alloc (MB)  ║ %,11.1f ║ %,11.1f ║ %,13.1f ║%n",
        jsonDeserAlloc / 1e6, pbDeserAlloc / 1e6, lbDeserAlloc / 1e6);
    System.out.printf("║  Deser alloc vs JSON     ║     100%%     ║    %5.0f%%    ║      %5.0f%%     ║%n",
        100.0 * pbDeserAlloc / jsonDeserAlloc, 100.0 * lbDeserAlloc / jsonDeserAlloc);

    System.out.printf("╚══════════════════════════╩══════════════╩══════════════╩════════════════╝%n");
    System.out.println();
    System.out.println("Note: allocation measured via ThreadMXBean (per-thread bytes allocated).");
    System.out.println("      For GC churn and alloc rate, use SerializationFormatComparisonBenchmark.main().");
    System.out.println();
  }

  // =========================================================================
  // Synthetic comparison test (50 000 nodes × 80 classifiers)
  // =========================================================================

  /**
   * Builds a synthetic language with {@code numConcepts} concepts, each having {@code propsPerConcept}
   * String properties, {@code contsPerConcept} containments (self-referential), and {@code
   * refsPerConcept} references (self-referential). Generates {@code totalNodes} flat {@link
   * DynamicNode} instances (no actual children / references wired up) with all properties set to a
   * short string value, distributed round-robin across the classifiers.
   */
  @Test
  public void compareSyntheticFormats() throws IOException {
    final int NUM_CONCEPTS = 80;
    final int PROPS_PER_CONCEPT = 3;
    final int CONTS_PER_CONCEPT = 2;
    final int REFS_PER_CONCEPT = 1;
    final int TOTAL_NODES = 50_000;

    // --- Build synthetic language ---
    Language lang = new Language();
    lang.setID("SyntheticLang");
    lang.setKey("SyntheticLang");
    lang.setName("SyntheticLang");
    lang.setVersion("1");

    Concept[] concepts = new Concept[NUM_CONCEPTS];
    for (int ci = 0; ci < NUM_CONCEPTS; ci++) {
      String conceptId = "SynConcept_" + ci;
      Concept concept = new Concept(lang, "Concept" + ci, conceptId);
      concept.setKey(conceptId);
      for (int pi = 0; pi < PROPS_PER_CONCEPT; pi++) {
        String propId = conceptId + "_prop" + pi;
        Property prop =
            Property.createRequired("prop" + pi, LionCoreBuiltins.getString(LionWebVersion.v2023_1))
                .setID(propId)
                .setKey(propId);
        concept.addFeature(prop);
      }
      for (int ki = 0; ki < CONTS_PER_CONCEPT; ki++) {
        String contId = conceptId + "_cont" + ki;
        io.lionweb.language.Containment cont =
            io.lionweb.language.Containment.createMultiple("cont" + ki, concept)
                .setID(contId)
                .setKey(contId);
        concept.addFeature(cont);
      }
      for (int ri = 0; ri < REFS_PER_CONCEPT; ri++) {
        String refId = conceptId + "_ref" + ri;
        io.lionweb.language.Reference ref =
            io.lionweb.language.Reference.createOptional("ref" + ri, concept)
                .setID(refId)
                .setKey(refId);
        concept.addFeature(ref);
      }
      lang.addElement(concept);
      concepts[ci] = concept;
    }

    // --- Create flat DynamicNode instances ---
    List<ClassifierInstance<?>> allNodes = new ArrayList<>(TOTAL_NODES);
    for (int ni = 0; ni < TOTAL_NODES; ni++) {
      Concept concept = concepts[ni % NUM_CONCEPTS];
      DynamicNode node = new DynamicNode("syn_node_" + ni, concept);
      List<Property> props = concept.allProperties();
      for (int pi = 0; pi < props.size(); pi++) {
        node.setPropertyValue(props.get(pi), "value_" + pi);
      }
      allNodes.add(node);
    }

    // --- Create serialisers and register the synthetic language ---
    JsonSerialization jsonSer =
        SerializationProvider.getStandardJsonSerialization(LionWebVersion.v2023_1);
    ProtoBufSerialization protobufSer =
        SerializationProvider.getStandardProtoBufSerialization(LionWebVersion.v2023_1);
    LionBinSerialization lionbinSer =
        SerializationProvider.getStandardLionBinSerialization(LionWebVersion.v2023_1);
    jsonSer.getClassifierResolver().registerLanguage(lang);
    protobufSer.getClassifierResolver().registerLanguage(lang);
    lionbinSer.getClassifierResolver().registerLanguage(lang);
    jsonSer.getInstantiator().enableDynamicNodes();
    protobufSer.getInstantiator().enableDynamicNodes();
    lionbinSer.getInstantiator().enableDynamicNodes();

    // --- Compute payloads ---
    String jsonPayload = jsonSer.serializeNodesToJsonString(allNodes);
    byte[] protobufPayload = protobufSer.serializeNodesToByteArray(allNodes);
    byte[] lionbinPayload = lionbinSer.serializeNodesToByteArray(allNodes);

    int jsonBytes = jsonPayload.getBytes(StandardCharsets.UTF_8).length;
    int pbBytes = protobufPayload.length;
    int lbBytes = lionbinPayload.length;

    // --- Warm up ---
    for (int i = 0; i < N_WARMUP; i++) {
      jsonSer.serializeNodesToJsonString(allNodes);
      protobufSer.serializeNodesToByteArray(allNodes);
      lionbinSer.serializeNodesToByteArray(allNodes);
      jsonSer.deserializeToNodes(jsonPayload);
      try {
        protobufSer.deserializeToNodes(protobufPayload);
        lionbinSer.deserializeToNodes(lionbinPayload);
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    }

    // --- Measure serialisation ---
    TimingResult jsonSerTime = measureTime(() -> jsonSer.serializeNodesToJsonString(allNodes));
    TimingResult pbSerTime = measureTime(() -> protobufSer.serializeNodesToByteArray(allNodes));
    TimingResult lbSerTime = measureTime(() -> lionbinSer.serializeNodesToByteArray(allNodes));

    long jsonSerAlloc = measureAlloc(() -> jsonSer.serializeNodesToJsonString(allNodes));
    long pbSerAlloc = measureAlloc(() -> protobufSer.serializeNodesToByteArray(allNodes));
    long lbSerAlloc = measureAlloc(() -> lionbinSer.serializeNodesToByteArray(allNodes));

    // --- Measure deserialisation ---
    TimingResult jsonDeserTime = measureTime(() -> jsonSer.deserializeToNodes(jsonPayload));
    TimingResult pbDeserTime =
        measureTime(
            () -> {
              try {
                protobufSer.deserializeToNodes(protobufPayload);
              } catch (IOException e) {
                throw new UncheckedIOException(e);
              }
            });
    TimingResult lbDeserTime =
        measureTime(
            () -> {
              try {
                lionbinSer.deserializeToNodes(lionbinPayload);
              } catch (IOException e) {
                throw new UncheckedIOException(e);
              }
            });

    long jsonDeserAlloc = measureAlloc(() -> jsonSer.deserializeToNodes(jsonPayload));
    long pbDeserAlloc =
        measureAlloc(
            () -> {
              try {
                protobufSer.deserializeToNodes(protobufPayload);
              } catch (IOException e) {
                throw new UncheckedIOException(e);
              }
            });
    long lbDeserAlloc =
        measureAlloc(
            () -> {
              try {
                lionbinSer.deserializeToNodes(lionbinPayload);
              } catch (IOException e) {
                throw new UncheckedIOException(e);
              }
            });

    // --- Print comparison table ---
    System.out.println();
    System.out.printf(
        "╔══════════════════════════════════════════════════════════════════════════════════════╗%n");
    System.out.printf(
        "║  SYNTHETIC: %,d nodes × %d classifiers (%d props + %d conts + %d refs each)  ║%n",
        TOTAL_NODES, NUM_CONCEPTS, PROPS_PER_CONCEPT, CONTS_PER_CONCEPT, REFS_PER_CONCEPT);
    System.out.printf(
        "╠══════════════════════════╦══════════════╦══════════════╦════════════════╣%n");
    System.out.printf(
        "║  Metric                  ║     JSON     ║   ProtoBuf   ║    LionBin     ║%n");
    System.out.printf(
        "╠══════════════════════════╬══════════════╬══════════════╬════════════════╣%n");

    System.out.printf(
        "║  Payload size (bytes)    ║ %,11d ║ %,11d ║ %,13d ║%n", jsonBytes, pbBytes, lbBytes);
    System.out.printf(
        "║  Payload vs JSON         ║     100.0%%   ║    %5.1f%%    ║      %5.1f%%     ║%n",
        100.0 * pbBytes / jsonBytes, 100.0 * lbBytes / jsonBytes);

    System.out.printf(
        "╠══════════════════════════╬══════════════╬══════════════╬════════════════╣%n");

    System.out.printf(
        "║  Serialize median (ms)   ║ %,11.1f ║ %,11.1f ║ %,13.1f ║%n",
        jsonSerTime.medianMs, pbSerTime.medianMs, lbSerTime.medianMs);
    System.out.printf(
        "║  Serialize range (ms)    ║ %5.1f–%5.1f  ║ %5.1f–%5.1f  ║  %5.1f–%5.1f   ║%n",
        jsonSerTime.minMs, jsonSerTime.maxMs,
        pbSerTime.minMs, pbSerTime.maxMs,
        lbSerTime.minMs, lbSerTime.maxMs);
    System.out.printf(
        "║  Serialize vs JSON       ║     100%%     ║    %5.0f%%    ║      %5.0f%%     ║%n",
        100.0 * pbSerTime.medianMs / jsonSerTime.medianMs,
        100.0 * lbSerTime.medianMs / jsonSerTime.medianMs);

    System.out.printf(
        "╠══════════════════════════╬══════════════╬══════════════╬════════════════╣%n");

    System.out.printf(
        "║  Deserialize median (ms) ║ %,11.1f ║ %,11.1f ║ %,13.1f ║%n",
        jsonDeserTime.medianMs, pbDeserTime.medianMs, lbDeserTime.medianMs);
    System.out.printf(
        "║  Deserialize range (ms)  ║ %5.1f–%5.1f  ║ %5.1f–%5.1f  ║  %5.1f–%5.1f   ║%n",
        jsonDeserTime.minMs, jsonDeserTime.maxMs,
        pbDeserTime.minMs, pbDeserTime.maxMs,
        lbDeserTime.minMs, lbDeserTime.maxMs);
    System.out.printf(
        "║  Deserialize vs JSON     ║     100%%     ║    %5.0f%%    ║      %5.0f%%     ║%n",
        100.0 * pbDeserTime.medianMs / jsonDeserTime.medianMs,
        100.0 * lbDeserTime.medianMs / jsonDeserTime.medianMs);

    System.out.printf(
        "╠══════════════════════════╬══════════════╬══════════════╬════════════════╣%n");

    System.out.printf(
        "║  Serialize alloc (MB)    ║ %,11.1f ║ %,11.1f ║ %,13.1f ║%n",
        jsonSerAlloc / 1e6, pbSerAlloc / 1e6, lbSerAlloc / 1e6);
    System.out.printf(
        "║  Serialize alloc vs JSON ║     100%%     ║    %5.0f%%    ║      %5.0f%%     ║%n",
        100.0 * pbSerAlloc / jsonSerAlloc, 100.0 * lbSerAlloc / jsonSerAlloc);
    System.out.printf(
        "║  Deserialize alloc (MB)  ║ %,11.1f ║ %,11.1f ║ %,13.1f ║%n",
        jsonDeserAlloc / 1e6, pbDeserAlloc / 1e6, lbDeserAlloc / 1e6);
    System.out.printf(
        "║  Deser alloc vs JSON     ║     100%%     ║    %5.0f%%    ║      %5.0f%%     ║%n",
        100.0 * pbDeserAlloc / jsonDeserAlloc, 100.0 * lbDeserAlloc / jsonDeserAlloc);

    System.out.printf(
        "╚══════════════════════════╩══════════════╩══════════════╩════════════════╝%n");
    System.out.println();
  }

  // =========================================================================
  // Measurement helpers
  // =========================================================================

  private static final class TimingResult {
    final double medianMs;
    final double minMs;
    final double maxMs;

    TimingResult(double medianMs, double minMs, double maxMs) {
      this.medianMs = medianMs;
      this.minMs = minMs;
      this.maxMs = maxMs;
    }
  }

  private TimingResult measureTime(Runnable op) {
    List<Long> samples = new ArrayList<>(N_ITER);
    for (int i = 0; i < N_ITER; i++) {
      long t0 = System.nanoTime();
      op.run();
      samples.add(System.nanoTime() - t0);
    }
    Collections.sort(samples);
    List<Long> trimmed = samples.subList(N_TRIM, samples.size() - N_TRIM);
    long median = trimmed.get(trimmed.size() / 2);
    long min = trimmed.get(0);
    long max = trimmed.get(trimmed.size() - 1);
    return new TimingResult(median / 1e6, min / 1e6, max / 1e6);
  }

  /**
   * Returns bytes allocated by the current thread while executing {@code op}. Uses {@code
   * com.sun.management.ThreadMXBean} for per-thread accuracy on HotSpot; falls back to heap-delta
   * on other JVMs (less accurate, may include GC effects).
   *
   * <p>Reports the median over {@value #N_ITER} iterations after trimming outliers, consistent with
   * the timing measurement.
   */
  private long measureAlloc(Runnable op) {
    ThreadMXBean bean = ManagementFactory.getThreadMXBean();
    if (bean instanceof com.sun.management.ThreadMXBean) {
      com.sun.management.ThreadMXBean sunBean = (com.sun.management.ThreadMXBean) bean;
      long tid = Thread.currentThread().getId();
      List<Long> samples = new ArrayList<>(N_ITER);
      for (int i = 0; i < N_ITER; i++) {
        long before = sunBean.getThreadAllocatedBytes(tid);
        op.run();
        samples.add(sunBean.getThreadAllocatedBytes(tid) - before);
      }
      Collections.sort(samples);
      List<Long> trimmed = samples.subList(N_TRIM, samples.size() - N_TRIM);
      return trimmed.get(trimmed.size() / 2);
    }
    // Heap-delta fallback
    System.gc();
    Runtime rt = Runtime.getRuntime();
    long before = rt.totalMemory() - rt.freeMemory();
    op.run();
    return Math.max(0L, rt.totalMemory() - rt.freeMemory() - before);
  }
}
