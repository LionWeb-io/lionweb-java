package io.lionweb.client.inmemory;

import io.lionweb.LionWebVersion;
import io.lionweb.client.api.HistorySupport;
import io.lionweb.client.api.RepositoryConfiguration;
import io.lionweb.serialization.ProtoBufSerialization;
import io.lionweb.serialization.data.*;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Benchmarks ProtoBuf deserialization on a synthetic homogeneous model: many nodes that all share
 * the same classifier (and therefore the same feature set). This is the workload the {@link
 * ClassifierSchema} optimization was designed for — in a real AST, thousands of nodes of the same
 * type (MethodDeclaration, VariableDeclarator, …) appear together.
 *
 * <p>Compare against {@link PerformanceTestOnProtoBufSerialization}, which uses LargeLanguage.json
 * (a language <em>definition</em>) where each classifier type appears only a handful of times.
 */
@Tag("performance")
public class PerformanceTestHomogeneousModelRepositoryData {

  /** Number of homogeneous nodes in the synthetic model. */
  private static final int CHUNKS_COUNT = 70;
  private static final int NODE_PER_CKUNK_COUNT = 1500;

  // -------------------------------------------------------------------------
  // Shared MetaPointers — interned once, reused across all nodes
  // -------------------------------------------------------------------------

  private static final String LANG_KEY = "com.example.testlang";
  private static final String LANG_VER = "1.0";

  private static final MetaPointer MP_CLASSIFIER =
      MetaPointer.get(LANG_KEY, LANG_VER, "MethodDeclaration");
    private static final MetaPointer MP_PARAM =
        MetaPointer.get(LANG_KEY, LANG_VER, "Param");
    private static final MetaPointer MP_STMT =
        MetaPointer.get(LANG_KEY, LANG_VER, "Stmt");

  // Properties
  private static final MetaPointer MP_NAME = MetaPointer.get(LANG_KEY, LANG_VER, "name");
  private static final MetaPointer MP_VISIBILITY =
      MetaPointer.get(LANG_KEY, LANG_VER, "visibility");
  private static final MetaPointer MP_IS_STATIC = MetaPointer.get(LANG_KEY, LANG_VER, "isStatic");
  private static final MetaPointer MP_RETURN_TYPE =
      MetaPointer.get(LANG_KEY, LANG_VER, "returnType");
  private static final MetaPointer MP_DOC = MetaPointer.get(LANG_KEY, LANG_VER, "documentation");

  // Containments
  private static final MetaPointer MP_PARAMS = MetaPointer.get(LANG_KEY, LANG_VER, "parameters");
  private static final MetaPointer MP_BODY = MetaPointer.get(LANG_KEY, LANG_VER, "body");

  // References
  private static final MetaPointer MP_CALLEE = MetaPointer.get(LANG_KEY, LANG_VER, "callee");
  private static final MetaPointer MP_OVERRIDES = MetaPointer.get(LANG_KEY, LANG_VER, "overrides");

  // Visibility values — reused strings (mirrors what an intern table would hold)
  private static final String[] VISIBILITIES = {"public", "protected", "private", "package"};
  private static final String[] RETURN_TYPES = {"void", "int", "String", "boolean", "Object"};

  // -------------------------------------------------------------------------
  // Benchmark tests
  // -------------------------------------------------------------------------

//  @Test
//  public void deserializeHomogeneousModelProtoBuf() throws Exception {
//    byte[] pbBytes = buildProtoBufBytes();
//    ProtoBufSerialization pbs = new ProtoBufSerialization();
//    System.out.println(
//        "Homogeneous model ("
//            + NODE_COUNT
//            + " nodes), ProtoBuf payload: "
//            + pbBytes.length
//            + " bytes");
//    performanceMeasure(
//        () -> {
//          try {
//            pbs.deserializeToChunk(pbBytes);
//          } catch (IOException e) {
//            throw new UncheckedIOException(e);
//          }
//        });
//  }

  @Test
  public void repositoryDataMemoryAllocation() {
    System.out.println(
        "Homogeneous model ("
            + NODE_PER_CKUNK_COUNT * CHUNKS_COUNT * 4
            + " nodes)");
    measureMemoryAllocation(
        () -> {
              buildRepositoryData();
        });
  }

  // -------------------------------------------------------------------------
  // Synthetic model builder
  // -------------------------------------------------------------------------

  /**
   * Builds a {@link SerializationChunk} containing {@link #NODE_COUNT} nodes that all share {@link
   * #MP_CLASSIFIER} and the same 5 properties, 2 containments, and 2 references, then serializes it
   * to ProtoBuf bytes.
   *
   * <p>The node structure is intentionally flat (no deep parent–child tree) so that the benchmark
   * measures deserialization throughput without confounding tree-reconstruction overhead.
   */
  private InMemoryServer buildRepositoryData() {
    InMemoryServer inMemoryServer = new InMemoryServer();
    inMemoryServer.createRepository(new RepositoryConfiguration("MyRepo", LionWebVersion.v2023_1, HistorySupport.DISABLED));
    for (int i = 0; i < CHUNKS_COUNT; i++) {
//        SerializationChunk chunk = new SerializationChunk();
//        chunk.setSerializationFormatVersion("2023.1");
//        chunk.addLanguage(LanguageVersion.of(LANG_KEY, LANG_VER));
        List<SerializedClassifierInstance> nodesInChunk = new ArrayList<>(NODE_PER_CKUNK_COUNT * 4);

        for (int j = 0; j < NODE_PER_CKUNK_COUNT; j++) {
            SerializedClassifierInstance sci = new SerializedClassifierInstance("n-" + j, MP_CLASSIFIER);
            nodesInChunk.add(sci);

            // 5 properties
            sci.unsafeAppendPropertyValue(SerializedPropertyValue.get(MP_NAME, "method_" + j));
            sci.unsafeAppendPropertyValue(
                SerializedPropertyValue.get(MP_VISIBILITY, VISIBILITIES[j & 3]));
            sci.unsafeAppendPropertyValue(
                SerializedPropertyValue.get(MP_IS_STATIC, (j & 1) == 0 ? "false" : "true"));
            sci.unsafeAppendPropertyValue(
                SerializedPropertyValue.get(MP_RETURN_TYPE, RETURN_TYPES[j % RETURN_TYPES.length]));
            sci.unsafeAppendPropertyValue(SerializedPropertyValue.get(MP_DOC, "Javadoc for method " + j));

            // 2 containments (each with 2 children)
            String p1 = "param-" + (j * 2);
            String p2 = "param-" + (j * 2 + 1);
            sci.unsafeAppendContainmentValue(new SerializedContainmentValue(MP_PARAMS, p1, p2));
            String s1 = "stmt-" + j;
            sci.unsafeAppendContainmentValue(new SerializedContainmentValue(MP_BODY, s1));

            SerializedClassifierInstance p1Ci = new SerializedClassifierInstance(p1, Schema.fromMetaPointer(MP_PARAM));
            nodesInChunk.add(p1Ci);

            SerializedClassifierInstance p2Ci = new SerializedClassifierInstance(p2, Schema.fromMetaPointer(MP_PARAM));
            nodesInChunk.add(p2Ci);

            SerializedClassifierInstance stmtCi = new SerializedClassifierInstance(s1, Schema.fromMetaPointer(MP_STMT));
            nodesInChunk.add(stmtCi);

            // 2 references
            int calleeIdx = (j + 1) % NODE_PER_CKUNK_COUNT;
            SerializedReferenceValue callee = new SerializedReferenceValue(MP_CALLEE);
            callee.addValue(new SerializedReferenceValue.Entry("n-" + calleeIdx, "method_" + calleeIdx));
            sci.unsafeAppendReferenceValue(callee);

            int overridesIdx = (j + NODE_PER_CKUNK_COUNT / 2) % NODE_PER_CKUNK_COUNT;
            SerializedReferenceValue overrides = new SerializedReferenceValue(MP_OVERRIDES);
            overrides.addValue(
                new SerializedReferenceValue.Entry("n-" + overridesIdx, "method_" + overridesIdx));
            sci.unsafeAppendReferenceValue(overrides);

            //chunk.addClassifierInstance(sci);

        }
        inMemoryServer.createPartitionFromChunk("MyRepo", nodesInChunk);
    }
    return inMemoryServer;
  }

  // -------------------------------------------------------------------------
  // Measurement helpers (same methodology as PerformanceTestOnSerialization)
  // -------------------------------------------------------------------------

  private static final int N_ITERATIONS = 10;
  private static final int N_DROP_EACH_END = 4;

  private void performanceMeasure(Runnable r) {
    List<Long> times = new ArrayList<>(N_ITERATIONS);
    for (int i = 0; i < N_ITERATIONS; i++) {
      long t0 = System.currentTimeMillis();
      r.run();
      long elapsed = System.currentTimeMillis() - t0;
      System.out.println("Elapsed: " + elapsed + " ms");
      times.add(elapsed);
    }
    times = times.stream().sorted().collect(Collectors.toList());
    times = times.subList(N_DROP_EACH_END, times.size() - N_DROP_EACH_END);
    assertEquals(N_ITERATIONS - 2 * N_DROP_EACH_END, times.size());
    System.out.println("Range: " + times.get(0) + " ms to " + times.get(times.size() - 1) + " ms");
  }

  private void measureMemoryAllocation(Runnable r) {
    List<Long> allocs = new ArrayList<>(N_ITERATIONS);
    for (int i = 0; i < N_ITERATIONS; i++) {
      long bytes = measureAllocatedBytes(r);
      System.out.println("Allocated: " + (bytes / 1024) + " KB");
      allocs.add(bytes);
    }
    allocs = allocs.stream().sorted().collect(Collectors.toList());
    allocs = allocs.subList(N_DROP_EACH_END, allocs.size() - N_DROP_EACH_END);
    assertEquals(N_ITERATIONS - 2 * N_DROP_EACH_END, allocs.size());
    System.out.println(
        "Allocation range: "
            + (allocs.get(0) / 1024)
            + " KB to "
            + (allocs.get(allocs.size() - 1) / 1024)
            + " KB");
  }

  private long measureAllocatedBytes(Runnable r) {
    ThreadMXBean bean = ManagementFactory.getThreadMXBean();
    if (bean instanceof com.sun.management.ThreadMXBean) {
      com.sun.management.ThreadMXBean sun = (com.sun.management.ThreadMXBean) bean;
      long id = Thread.currentThread().getId();
      long before = sun.getThreadAllocatedBytes(id);
      r.run();
      return sun.getThreadAllocatedBytes(id) - before;
    }
    System.gc();
    Runtime rt = Runtime.getRuntime();
    long before = rt.totalMemory() - rt.freeMemory();
    r.run();
    return Math.max(0L, rt.totalMemory() - rt.freeMemory() - before);
  }
}
