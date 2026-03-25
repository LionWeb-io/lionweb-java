package io.lionweb.client.inmemory;

import io.lionweb.LionWebVersion;
import io.lionweb.client.api.ClassifierKey;
import io.lionweb.client.api.ClassifierResult;
import io.lionweb.client.api.HistorySupport;
import io.lionweb.client.api.RepositoryConfiguration;
import io.lionweb.model.Node;
import io.lionweb.serialization.data.MetaPointer;
import io.lionweb.serialization.data.SerializedClassifierInstance;
import java.util.*;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

/**
 * JMH benchmark for {@link RepositoryData#nodesByClassifier}.
 *
 * <p>This was the top CPU hotspot (1051 samples out of ~3500 total lionweb samples) identified via
 * JFR profiling. The old implementation scanned all nodes on every call (O(N)); the new
 * implementation uses an incremental index, making the query O(1).
 *
 * <p>Two scenarios are benchmarked:
 *
 * <ul>
 *   <li>{@link #nodesByClassifierSmall} — 100 nodes, 5 classifiers (typical small repo)
 *   <li>{@link #nodesByClassifierLarge} — 10 000 nodes, 20 classifiers (large repo stress test)
 * </ul>
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 2, timeUnit = TimeUnit.SECONDS)
@Fork(2)
@State(Scope.Thread)
public class NodesByClassifierBenchmark {

  private RepositoryData smallRepo;
  private RepositoryData largeRepo;

  @Setup(Level.Trial)
  public void setup() {
    smallRepo = buildRepo(100, 5);
    largeRepo = buildRepo(10_000, 20);
  }

  private RepositoryData buildRepo(int nodeCount, int classifierCount) {
    RepositoryData repo =
        new RepositoryData(
            new RepositoryConfiguration("bench", LionWebVersion.v2023_1, HistorySupport.DISABLED));

    MetaPointer[] classifiers = new MetaPointer[classifierCount];
    for (int c = 0; c < classifierCount; c++) {
      classifiers[c] = MetaPointer.get("bench-lang", "1.0", "Classifier" + c);
    }

    int containmentsCount = 13;
    MetaPointer[] containments = new MetaPointer[containmentsCount];
    for (int c = 0; c < containmentsCount; c++) {
      containments[c] = MetaPointer.get("bench-lang", "1.0", "Containment" + c);
    }

    List<SerializedClassifierInstance> nodes = new ArrayList<>(nodeCount);
    SerializedClassifierInstance root = null;
    for (int i = 0; i < nodeCount; i++) {
      MetaPointer mp = classifiers[i % classifierCount];
      SerializedClassifierInstance newNode = new SerializedClassifierInstance("node-" + i, mp);
      if (i == 0) {
        root = newNode;
      } else {
        root.addChild(containments[i % containmentsCount], newNode.getID());
        newNode.setParentNodeID(root.getID());
      }
      nodes.add(newNode);
    }

    // Store the first node as partition root; the rest as flat children of root
    repo.partitionIDs.add("node-0");
    repo.store(nodes);
    return repo;
  }

  @Benchmark
  public Map<ClassifierKey, ClassifierResult> nodesByClassifierSmall() {
    return smallRepo.nodesByClassifier(null);
  }

  @Benchmark
  public Map<ClassifierKey, ClassifierResult> nodesByClassifierLarge() {
    return largeRepo.nodesByClassifier(null);
  }

  public static void main(String[] args) throws RunnerException {
    Options opts =
        new OptionsBuilder()
            .include(NodesByClassifierBenchmark.class.getSimpleName())
            .addProfiler(GCProfiler.class)
            .build();
    new Runner(opts).run();
  }
}
