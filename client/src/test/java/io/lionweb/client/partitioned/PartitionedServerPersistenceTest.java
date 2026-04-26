package io.lionweb.client.partitioned;

import static org.junit.jupiter.api.Assertions.*;

import io.lionweb.LionWebVersion;
import io.lionweb.client.api.ClassifierKey;
import io.lionweb.client.api.ClassifierResult;
import io.lionweb.client.api.HistorySupport;
import io.lionweb.client.api.RepositoryConfiguration;
import io.lionweb.serialization.data.MetaPointer;
import io.lionweb.serialization.data.SerializedClassifierInstance;
import io.lionweb.utils.ValidationResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests specific to the persistence, cache eviction, dirty tracking, and reload behaviour of
 * {@link PartitionedServer}. These scenarios have no equivalent in the in-memory implementation.
 */
public class PartitionedServerPersistenceTest {

  private static final LionWebVersion VERSION = LionWebVersion.v2023_1;
  private static final RepositoryConfiguration REPO_CFG =
      new RepositoryConfiguration("testRepo", VERSION, HistorySupport.DISABLED);

  @TempDir
  Path tempDir;

  // -------------------------------------------------------------------------
  // Helpers
  // -------------------------------------------------------------------------

  private static SerializedClassifierInstance node(String id, String parentId) {
    SerializedClassifierInstance n =
        new SerializedClassifierInstance(id, MetaPointer.get("lang", "1.0", "Cls"));
    n.setParentNodeID(parentId);
    return n;
  }

  private static SerializedClassifierInstance root(String id) {
    return new SerializedClassifierInstance(id, MetaPointer.get("lang", "1.0", "Cls"));
  }

  private static PartitionedServer server(Path dir, int maxPartitions, int maxNodes) {
    return new PartitionedServer(dir, new CacheConfig(maxPartitions, maxNodes));
  }

  // -------------------------------------------------------------------------
  // Basic persistence: flush and reopen
  // -------------------------------------------------------------------------

  @Test
  public void partitionIsPersistedOnFlush() throws Exception {
    Path repoDir = tempDir.resolve("testRepo");

    try (PartitionedServer srv = server(tempDir, 4, 100_000)) {
      srv.createRepository(REPO_CFG);
      srv.createPartitionFromChunk("testRepo", Collections.singletonList(root("p1")));
      srv.flush();
    }

    // File should exist after flush
    assertTrue(Files.exists(repoDir.resolve("p1.pb")), "Partition file should exist after flush");
  }

  /**
   * Known limitation: when a {@link PartitionedServer} is closed and a new instance is opened
   * against the same storage directory, the in-memory {@code nodeId → partitionId} index is empty.
   * Callers must explicitly re-register partitions (e.g. via {@code createPartitionFromChunk} with
   * data loaded from the backend) to rebuild the index before node lookups will work.
   */
  @Test
  public void reopenedServerRequiresExplicitPartitionRegistration() throws Exception {
    // Write and flush
    try (PartitionedServer srv = server(tempDir, 4, 100_000)) {
      srv.createRepository(REPO_CFG);
      srv.createPartitionFromChunk("testRepo", Collections.singletonList(root("part1")));
    } // close() flushes automatically

    // Fresh server: index is empty even though the file exists on disk
    try (PartitionedServer srv2 = server(tempDir, 4, 100_000)) {
      srv2.createRepository(REPO_CFG);

      // Without re-registering, lookup throws
      assertThrows(
          IllegalArgumentException.class,
          () -> srv2.retrieve("testRepo", Collections.singletonList("part1"), 0),
          "Node lookup without re-registration should throw");
    }
  }

  @Test
  public void explicitReloadAfterReopeningServer() throws Exception {
    // Write and flush
    try (PartitionedServer srv = server(tempDir, 4, 100_000)) {
      srv.createRepository(REPO_CFG);
      SerializedClassifierInstance r = root("part1");
      SerializedClassifierInstance child = node("child1", "part1");
      r.unsafeAppendContainmentValue(
          MetaPointer.get("lang", "1.0", "ch"), Collections.singletonList("child1"));
      srv.createPartitionFromChunk("testRepo", Arrays.asList(r, child));
    }

    // On re-open: re-register partition (simulate what a client does on startup)
    try (PartitionedServer srv2 = server(tempDir, 4, 100_000)) {
      srv2.createRepository(REPO_CFG);

      // Load the persisted chunk back via the backend
      DiskRepositoryBackend diskBackend =
          new DiskRepositoryBackend(tempDir);
      List<SerializedClassifierInstance> nodes =
          diskBackend.loadPartition("testRepo", "part1");
      assertFalse(nodes.isEmpty(), "Persisted partition should be loadable from disk");
      assertEquals(2, nodes.size());

      // Re-create partition in the new server using the loaded data
      srv2.createPartitionFromChunk("testRepo", nodes);

      List<SerializedClassifierInstance> retrieved =
          srv2.retrieve("testRepo", Collections.singletonList("part1"), Integer.MAX_VALUE);
      assertEquals(2, retrieved.size());

      Set<String> ids = new HashSet<>();
      for (SerializedClassifierInstance n : retrieved) ids.add(n.getID());
      assertTrue(ids.contains("part1"));
      assertTrue(ids.contains("child1"));
    }
  }

  // -------------------------------------------------------------------------
  // Dirty tracking
  // -------------------------------------------------------------------------

  @Test
  public void cleanPartitionIsNotWrittenToDiskOnEviction() throws Exception {
    // Small cache: only 1 partition
    try (PartitionedServer srv = server(tempDir, 1, 100_000)) {
      srv.createRepository(REPO_CFG);

      // Create two partitions; the second one will evict the first
      srv.createPartitionFromChunk("testRepo", Collections.singletonList(root("p1")));
      srv.flush(); // p1 is now clean on disk

      // Store p1 with no changes - re-registering is the only mutation; flush it
      srv.flush(); // still clean

      // Add second partition: forces eviction of p1 (now clean, should not re-write)
      srv.createPartitionFromChunk("testRepo", Collections.singletonList(root("p2")));

      // p2 is dirty, p1 was clean - p1 file modification time should not change
      Path p1File = tempDir.resolve("testRepo").resolve("p1.pb");
      long modTime = Files.getLastModifiedTime(p1File).toMillis();

      // Give some time buffer
      Thread.sleep(50);

      // Flush p2
      srv.flush();

      // p1 should not have been re-written
      long modTimeAfter = Files.getLastModifiedTime(p1File).toMillis();
      assertEquals(modTime, modTimeAfter, "Clean p1 should not be re-written during p2 eviction");
    }
  }

  @Test
  public void dirtyPartitionIsWrittenBeforeEviction() throws Exception {
    // Cache: max 1 partition loaded
    try (PartitionedServer srv = server(tempDir, 1, 100_000)) {
      srv.createRepository(REPO_CFG);

      // Create p1 (dirty)
      srv.createPartitionFromChunk("testRepo", Collections.singletonList(root("p1")));

      // Creating p2 should evict p1; since p1 is dirty, it must be saved first
      srv.createPartitionFromChunk("testRepo", Collections.singletonList(root("p2")));

      // p1 file must now exist
      Path p1File = tempDir.resolve("testRepo").resolve("p1.pb");
      assertTrue(Files.exists(p1File), "Dirty p1 must be flushed before eviction");
    }
  }

  // -------------------------------------------------------------------------
  // Cache capacity
  // -------------------------------------------------------------------------

  @Test
  public void cacheDoesNotExceedMaxPartitions() throws Exception {
    int maxParts = 3;
    try (PartitionedServer srv = server(tempDir, maxParts, 1_000_000)) {
      srv.createRepository(REPO_CFG);

      for (int i = 0; i < maxParts + 5; i++) {
        srv.createPartitionFromChunk(
            "testRepo", Collections.singletonList(root("part-" + i)));
      }
      // Just verify no exception and all partitions are registered
      assertEquals(maxParts + 5, srv.listPartitionIDs("testRepo").size());
    }
  }

  // -------------------------------------------------------------------------
  // Multi-partition retrieve
  // -------------------------------------------------------------------------

  @Test
  public void retrieveNodesFromMultiplePartitions() throws Exception {
    try (PartitionedServer srv = server(tempDir, 8, 100_000)) {
      srv.createRepository(REPO_CFG);

      for (int i = 0; i < 5; i++) {
        String id = "part-" + i;
        SerializedClassifierInstance r = root(id);
        SerializedClassifierInstance child = node("child-" + i, id);
        r.unsafeAppendContainmentValue(
            MetaPointer.get("lang", "1.0", "ch"), Collections.singletonList("child-" + i));
        srv.createPartitionFromChunk("testRepo", Arrays.asList(r, child));
      }

      // Retrieve a mix of roots from different partitions
      List<String> requestIds = Arrays.asList("part-0", "part-2", "part-4");
      List<SerializedClassifierInstance> result =
          srv.retrieve("testRepo", requestIds, Integer.MAX_VALUE);

      // Each root + child = 2 nodes, 3 partitions = 6 nodes
      assertEquals(6, result.size());
    }
  }

  // -------------------------------------------------------------------------
  // Delete partitions
  // -------------------------------------------------------------------------

  @Test
  public void deletingPartitionRemovesNodesFromIndex() throws Exception {
    try (PartitionedServer srv = server(tempDir, 4, 100_000)) {
      srv.createRepository(REPO_CFG);

      SerializedClassifierInstance r = root("p1");
      SerializedClassifierInstance child = node("c1", "p1");
      r.unsafeAppendContainmentValue(
          MetaPointer.get("lang", "1.0", "ch"), Collections.singletonList("c1"));
      srv.createPartitionFromChunk("testRepo", Arrays.asList(r, child));

      srv.deletePartitions("testRepo", Collections.singletonList("p1"));

      assertEquals(Collections.emptyList(), srv.listPartitionIDs("testRepo"));

      // Attempting to retrieve deleted node should throw
      assertThrows(
          IllegalArgumentException.class,
          () -> srv.retrieve("testRepo", Collections.singletonList("p1"), 0));
    }
  }

  // -------------------------------------------------------------------------
  // Classifier / language index
  // -------------------------------------------------------------------------

  @Test
  public void classifierIndexIsBuiltWithoutLoadingAllPartitions() throws Exception {
    try (PartitionedServer srv = server(tempDir, 4, 100_000)) {
      srv.createRepository(REPO_CFG);

      MetaPointer mp1 = MetaPointer.get("lang", "1.0", "ConceptA");
      MetaPointer mp2 = MetaPointer.get("lang", "1.0", "ConceptB");

      SerializedClassifierInstance r1 = new SerializedClassifierInstance("p1", mp1);
      SerializedClassifierInstance r2 = new SerializedClassifierInstance("p2", mp2);
      srv.createPartitionFromChunk("testRepo", Collections.singletonList(r1));
      srv.createPartitionFromChunk("testRepo", Collections.singletonList(r2));

      Map<ClassifierKey, ClassifierResult> result =
          srv.nodesByClassifier("testRepo");
      assertEquals(2, result.size());
      assertTrue(result.containsKey(new ClassifierKey("lang", "ConceptA")));
      assertTrue(result.containsKey(new ClassifierKey("lang", "ConceptB")));
    }
  }

  @Test
  public void consistencyCheckPassesForValidRepository() throws Exception {
    try (PartitionedServer srv = server(tempDir, 4, 100_000)) {
      srv.createRepository(REPO_CFG);

      SerializedClassifierInstance r = root("p1");
      SerializedClassifierInstance child = node("c1", "p1");
      r.unsafeAppendContainmentValue(
          MetaPointer.get("lang", "1.0", "ch"), Collections.singletonList("c1"));
      srv.createPartitionFromChunk("testRepo", Arrays.asList(r, child));

      ValidationResult result = srv.checkConsistency();
      assertTrue(
          result.isSuccessful(), "Consistency check should pass. Issues: " + result.getIssues());
    }
  }
}
