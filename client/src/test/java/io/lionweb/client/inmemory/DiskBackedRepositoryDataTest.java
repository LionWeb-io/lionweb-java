package io.lionweb.client.inmemory;

import static org.junit.jupiter.api.Assertions.*;

import io.lionweb.LionWebVersion;
import io.lionweb.client.api.ClassifierKey;
import io.lionweb.client.api.ClassifierResult;
import io.lionweb.client.api.HistorySupport;
import io.lionweb.client.api.RepositoryConfiguration;
import io.lionweb.language.LionCoreBuiltins;
import io.lionweb.serialization.data.MetaPointer;
import io.lionweb.serialization.data.SerializedClassifierInstance;
import java.util.*;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

public class DiskBackedRepositoryDataTest {

  private DiskBackedRepositoryData newRepo() {
    return new DiskBackedRepositoryData(
        new RepositoryConfiguration("repo1", LionWebVersion.v2023_1, HistorySupport.DISABLED));
  }

  @Test
  public void addSingleNode() {
    DiskBackedRepositoryData repoData = newRepo();
    assertEquals(Collections.emptySet(), repoData.nodeIDs());

    SerializedClassifierInstance n1 =
        new SerializedClassifierInstance("n1", MetaPointer.get("l1", "1.0", "c1"));

    repoData.addPartition("n1", Collections.singletonList(n1));
    assertEquals(new HashSet<>(Collections.singletonList("n1")), repoData.nodeIDs());
  }

  @Test
  public void addTrees() {
    DiskBackedRepositoryData repoData = newRepo();
    SerializedClassifierInstance n1 =
        new SerializedClassifierInstance("n1", MetaPointer.get("l1", "1.0", "c1"));
    SerializedClassifierInstance n2 =
        new SerializedClassifierInstance("n2", MetaPointer.get("l1", "1.0", "c1"));
    SerializedClassifierInstance n3 =
        new SerializedClassifierInstance("n3", MetaPointer.get("l1", "1.0", "c1"));
    SerializedClassifierInstance n4 =
        new SerializedClassifierInstance("n4", MetaPointer.get("l1", "1.0", "c1"));
    n1.unsafeAppendContainmentValue(
        MetaPointer.get("l1", "1.0", "containmentA"), Collections.singletonList("n2"));
    n2.unsafeAppendContainmentValue(
        MetaPointer.get("l1", "1.0", "containmentA"), Arrays.asList("n3", "n4"));
    n2.setParentNodeID("n1");
    n3.setParentNodeID("n2");
    n4.setParentNodeID("n2");

    repoData.addPartition("n1", Arrays.asList(n1, n2, n3, n4));
    assertEquals(new HashSet<>(Arrays.asList("n1", "n2", "n3", "n4")), repoData.nodeIDs());
  }

  @Test
  public void implicitlyRemoveChildren() {
    DiskBackedRepositoryData repoData = newRepo();
    SerializedClassifierInstance n1 =
        new SerializedClassifierInstance("n1", MetaPointer.get("l1", "1.0", "c1"));
    SerializedClassifierInstance n2 =
        new SerializedClassifierInstance("n2", MetaPointer.get("l1", "1.0", "c1"));
    SerializedClassifierInstance n3 =
        new SerializedClassifierInstance("n3", MetaPointer.get("l1", "1.0", "c1"));
    SerializedClassifierInstance n4 =
        new SerializedClassifierInstance("n4", MetaPointer.get("l1", "1.0", "c1"));
    n1.unsafeAppendContainmentValue(
        MetaPointer.get("l1", "1.0", "containmentA"), Collections.singletonList("n2"));
    n2.unsafeAppendContainmentValue(
        MetaPointer.get("l1", "1.0", "containmentA"), Arrays.asList("n3", "n4"));
    n2.setParentNodeID("n1");
    n3.setParentNodeID("n2");
    n4.setParentNodeID("n2");

    repoData.addPartition("n1", Arrays.asList(n1, n2, n3, n4));
    assertEquals(new HashSet<>(Arrays.asList("n1", "n2", "n3", "n4")), repoData.nodeIDs());

    SerializedClassifierInstance n1b =
        new SerializedClassifierInstance("n1", MetaPointer.get("l1", "1.0", "c1"));
    SerializedClassifierInstance n3b =
        new SerializedClassifierInstance("n3", MetaPointer.get("l1", "1.0", "c1"));
    SerializedClassifierInstance n5b =
        new SerializedClassifierInstance("n5", MetaPointer.get("l1", "1.0", "c1"));
    n1b.unsafeAppendContainmentValue(
        MetaPointer.get("l1", "1.0", "containmentA"), Arrays.asList("n3", "n5"));
    n3b.setParentNodeID("n1");
    n5b.setParentNodeID("n1");
    repoData.store(Arrays.asList(n1b, n3b, n5b));

    assertEquals(new HashSet<>(Arrays.asList("n1", "n3", "n5")), repoData.nodeIDs());
  }

  @Test
  public void idsAssignation() {
    DiskBackedRepositoryData repoData = newRepo();
    assertEquals(Collections.singletonList("id-1"), repoData.ids(1));
    // If I store a node with id-2, the system should not assign me such id later on
    repoData.addPartition(
        "id-2",
        Collections.singletonList(
            new SerializedClassifierInstance(
                "id-2", MetaPointer.from(LionCoreBuiltins.getNode(LionWebVersion.v2023_1)))));
    assertEquals(Collections.singletonList("id-3"), repoData.ids(1));

    // If I ask again for IDs I should get different ones
    assertEquals(Collections.singletonList("id-4"), repoData.ids(1));
  }

  // ========== classifier index tests ==========

  @Test
  public void classifierIndexPopulatedOnStore() {
    DiskBackedRepositoryData repoData = newRepo();
    MetaPointer mp = MetaPointer.get("l1", "1.0", "c1");
    SerializedClassifierInstance n1 = new SerializedClassifierInstance("n1", mp);
    repoData.addPartition("n1", Collections.singletonList(n1));

    ClassifierKey key = new ClassifierKey("l1", "c1");
    Map<ClassifierKey, ClassifierResult> result = repoData.nodesByClassifier(null);
    assertTrue(result.containsKey(key));
    assertEquals(Set.of("n1"), result.get(key).getIds());
    assertEquals(1, result.get(key).getSize());
  }

  @Test
  public void classifierIndexUpdatedOnDelete() {
    DiskBackedRepositoryData repoData = newRepo();
    MetaPointer mp = MetaPointer.get("l1", "1.0", "c1");
    SerializedClassifierInstance n1 = new SerializedClassifierInstance("n1", mp);
    SerializedClassifierInstance n2 = new SerializedClassifierInstance("n2", mp);
    n1.unsafeAppendContainmentValue(
        MetaPointer.get("l1", "1.0", "children"), Collections.singletonList("n2"));
    n2.setParentNodeID("n1");

    repoData.addPartition("n1", Arrays.asList(n1, n2));

    // Delete n2 by updating n1 to have no children
    SerializedClassifierInstance n1b = new SerializedClassifierInstance("n1", mp);
    repoData.store(Collections.singletonList(n1b));

    ClassifierKey key = new ClassifierKey("l1", "c1");
    Map<ClassifierKey, ClassifierResult> result = repoData.nodesByClassifier(null);
    assertTrue(result.containsKey(key));
    assertEquals(Set.of("n1"), result.get(key).getIds());
    assertEquals(1, result.get(key).getSize());
  }

  @Test
  public void classifierIndexRemovedWhenLastNodeDeleted() {
    DiskBackedRepositoryData repoData = newRepo();
    MetaPointer mp = MetaPointer.get("l1", "1.0", "c1");
    SerializedClassifierInstance n1 = new SerializedClassifierInstance("n1", mp);
    repoData.addPartition("n1", Collections.singletonList(n1));

    repoData.deletePartition("n1");

    ClassifierKey key = new ClassifierKey("l1", "c1");
    Map<ClassifierKey, ClassifierResult> result = repoData.nodesByClassifier(null);
    assertFalse(result.containsKey(key));
  }

  @Test
  public void classifierIndexMultipleClassifiers() {
    DiskBackedRepositoryData repoData = newRepo();
    MetaPointer mp1 = MetaPointer.get("l1", "1.0", "c1");
    MetaPointer mp2 = MetaPointer.get("l1", "1.0", "c2");
    SerializedClassifierInstance n1 = new SerializedClassifierInstance("n1", mp1);
    SerializedClassifierInstance n2 = new SerializedClassifierInstance("n2", mp2);
    SerializedClassifierInstance n3 = new SerializedClassifierInstance("n3", mp1);
    n1.unsafeAppendContainmentValue(MetaPointer.get("l1", "1.0", "ch"), Arrays.asList("n2", "n3"));
    n2.setParentNodeID("n1");
    n3.setParentNodeID("n1");

    repoData.addPartition("n1", Arrays.asList(n1, n2, n3));

    Map<ClassifierKey, ClassifierResult> result = repoData.nodesByClassifier(null);
    assertEquals(2, result.size());
    assertEquals(Set.of("n1", "n3"), result.get(new ClassifierKey("l1", "c1")).getIds());
    assertEquals(Set.of("n2"), result.get(new ClassifierKey("l1", "c2")).getIds());
  }

  @Test
  public void nodesByClassifierLimitReturnsCorrectTotal() {
    DiskBackedRepositoryData repoData = newRepo();
    MetaPointer mp = MetaPointer.get("l1", "1.0", "c1");
    SerializedClassifierInstance n1 = new SerializedClassifierInstance("n1", mp);
    SerializedClassifierInstance n2 = new SerializedClassifierInstance("n2", mp);
    SerializedClassifierInstance n3 = new SerializedClassifierInstance("n3", mp);
    n1.unsafeAppendContainmentValue(MetaPointer.get("l1", "1.0", "ch"), Arrays.asList("n2", "n3"));
    n2.setParentNodeID("n1");
    n3.setParentNodeID("n1");

    repoData.addPartition("n1", Arrays.asList(n1, n2, n3));

    ClassifierKey key = new ClassifierKey("l1", "c1");
    Map<ClassifierKey, ClassifierResult> result = repoData.nodesByClassifier(2);
    ClassifierResult cr = result.get(key);
    assertEquals(3, cr.getSize());
    assertEquals(2, cr.getIds().size());
  }

  @Test
  public void classifierIndexUpdatedOnClassifierChange() {
    DiskBackedRepositoryData repoData = newRepo();
    MetaPointer mp1 = MetaPointer.get("l1", "1.0", "c1");
    MetaPointer mp2 = MetaPointer.get("l1", "1.0", "c2");
    SerializedClassifierInstance n1 = new SerializedClassifierInstance("n1", mp1);
    repoData.addPartition("n1", Collections.singletonList(n1));

    // Re-store n1 with a different classifier
    SerializedClassifierInstance n1b = new SerializedClassifierInstance("n1", mp2);
    repoData.store(Collections.singletonList(n1b));

    Map<ClassifierKey, ClassifierResult> result = repoData.nodesByClassifier(null);
    assertFalse(result.containsKey(new ClassifierKey("l1", "c1")));
    assertTrue(result.containsKey(new ClassifierKey("l1", "c2")));
    assertEquals(Set.of("n1"), result.get(new ClassifierKey("l1", "c2")).getIds());
  }

  @Test
  public void addAnnotationToExistingNode() {
    DiskBackedRepositoryData repoData = newRepo();
    SerializedClassifierInstance n1 =
        new SerializedClassifierInstance("n1", MetaPointer.get("l1", "1.0", "c1"));
    SerializedClassifierInstance n2 =
        new SerializedClassifierInstance("n2", MetaPointer.get("l1", "1.0", "c1"));
    SerializedClassifierInstance n3 =
        new SerializedClassifierInstance("n3", MetaPointer.get("l1", "1.0", "c1"));
    SerializedClassifierInstance n4 =
        new SerializedClassifierInstance("n4", MetaPointer.get("l1", "1.0", "c1"));
    n1.unsafeAppendContainmentValue(
        MetaPointer.get("l1", "1.0", "containmentA"), Collections.singletonList("n2"));
    n2.unsafeAppendContainmentValue(
        MetaPointer.get("l1", "1.0", "containmentA"), Arrays.asList("n3", "n4"));
    n2.setParentNodeID("n1");
    n3.setParentNodeID("n2");
    n4.setParentNodeID("n2");

    repoData.addPartition("n1", Arrays.asList(n1, n2, n3, n4));

    SerializedClassifierInstance ann1 =
        new SerializedClassifierInstance("ann1", MetaPointer.get("lAnnotations", "1.0", "a1"));
    ann1.setParentNodeID("n1");
    n1.addAnnotation("ann1");
    repoData.store(Arrays.asList(n1, n2, n3, n4, ann1));

    assertEquals(new HashSet<>(Arrays.asList("n1", "n2", "n3", "n4", "ann1")), repoData.nodeIDs());
    List<SerializedClassifierInstance> retrieved = new ArrayList<>();
    repoData.retrieve("n1", 2, retrieved);
    assertEquals(
        new HashSet<>(Arrays.asList("n1", "n2", "n3", "n4", "ann1")),
        retrieved.stream().map(SerializedClassifierInstance::getID).collect(Collectors.toSet()));
  }
}
