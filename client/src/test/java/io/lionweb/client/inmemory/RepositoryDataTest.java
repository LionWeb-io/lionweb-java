package io.lionweb.client.inmemory;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.lionweb.LionWebVersion;
import io.lionweb.client.api.HistorySupport;
import io.lionweb.client.api.RepositoryConfiguration;
import io.lionweb.language.LionCoreBuiltins;
import io.lionweb.serialization.data.MetaPointer;
import io.lionweb.serialization.data.SerializedClassifierInstance;

import java.util.*;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

public class RepositoryDataTest {

  @Test
  public void addSingleNode() {
    RepositoryData repositoryData =
        new RepositoryData(
            new RepositoryConfiguration("repo1", LionWebVersion.v2023_1, HistorySupport.DISABLED));
    assertEquals(Collections.emptySet(), repositoryData.nodesByID.keySet());

    SerializedClassifierInstance n1 =
        new SerializedClassifierInstance("n1", MetaPointer.get("l1", "1.0", "c1"));

    repositoryData.partitionIDs.add("n1");
    repositoryData.store(Collections.singletonList(n1));
    assertEquals(new HashSet<>(Collections.singletonList("n1")), repositoryData.nodesByID.keySet());
  }

  @Test
  public void addTrees() {
    RepositoryData repositoryData =
        new RepositoryData(
            new RepositoryConfiguration("repo1", LionWebVersion.v2023_1, HistorySupport.DISABLED));
    SerializedClassifierInstance n1 =
        new SerializedClassifierInstance("n1", MetaPointer.get("l1", "1.0", "c1"));
    SerializedClassifierInstance n2 =
        new SerializedClassifierInstance("n2", MetaPointer.get("l1", "1.0", "c1"));
    SerializedClassifierInstance n3 =
        new SerializedClassifierInstance("n3", MetaPointer.get("l1", "1.0", "c1"));
    SerializedClassifierInstance n4 =
        new SerializedClassifierInstance("n4", MetaPointer.get("l1", "1.0", "c1"));
    n1.addChildren(MetaPointer.get("l1", "1.0", "containmentA"), Collections.singletonList("n2"));
    n2.addChildren(MetaPointer.get("l1", "1.0", "containmentA"), Arrays.asList("n3", "n4"));
    n2.setParentNodeID("n1");
    n3.setParentNodeID("n2");
    n4.setParentNodeID("n2");

    repositoryData.partitionIDs.add("n1");
    repositoryData.store(Arrays.asList(n1, n2, n3, n4));
    assertEquals(
        new HashSet<>(Arrays.asList("n1", "n2", "n3", "n4")), repositoryData.nodesByID.keySet());
  }

  @Test
  public void implicitlyRemoveChildren() {
    RepositoryData repositoryData =
        new RepositoryData(
            new RepositoryConfiguration("repo1", LionWebVersion.v2023_1, HistorySupport.DISABLED));
    SerializedClassifierInstance n1 =
        new SerializedClassifierInstance("n1", MetaPointer.get("l1", "1.0", "c1"));
    SerializedClassifierInstance n2 =
        new SerializedClassifierInstance("n2", MetaPointer.get("l1", "1.0", "c1"));
    SerializedClassifierInstance n3 =
        new SerializedClassifierInstance("n3", MetaPointer.get("l1", "1.0", "c1"));
    SerializedClassifierInstance n4 =
        new SerializedClassifierInstance("n4", MetaPointer.get("l1", "1.0", "c1"));
    n1.addChildren(MetaPointer.get("l1", "1.0", "containmentA"), Collections.singletonList("n2"));
    n2.addChildren(MetaPointer.get("l1", "1.0", "containmentA"), Arrays.asList("n3", "n4"));
    n2.setParentNodeID("n1");
    n3.setParentNodeID("n2");
    n4.setParentNodeID("n2");

    repositoryData.partitionIDs.add("n1");
    repositoryData.store(Arrays.asList(n1, n2, n3, n4));
    assertEquals(
        new HashSet<>(Arrays.asList("n1", "n2", "n3", "n4")), repositoryData.nodesByID.keySet());

    SerializedClassifierInstance n1b =
        new SerializedClassifierInstance("n1", MetaPointer.get("l1", "1.0", "c1"));
    SerializedClassifierInstance n3b =
        new SerializedClassifierInstance("n3", MetaPointer.get("l1", "1.0", "c1"));
    SerializedClassifierInstance n5b =
        new SerializedClassifierInstance("n5", MetaPointer.get("l1", "1.0", "c1"));
    n1b.addChildren(MetaPointer.get("l1", "1.0", "containmentA"), Arrays.asList("n3", "n5"));
    n3b.setParentNodeID("n1");
    n5b.setParentNodeID("n1");
    repositoryData.store(Arrays.asList(n1b, n3b, n5b));

    // n2 is not anymore a child of n1, so it should be removed
    // n2 has two children: n3 and n4. n3 has been replaced under n1
    // however n4 should disappear
    assertEquals(new HashSet<>(Arrays.asList("n1", "n3", "n5")), repositoryData.nodesByID.keySet());
  }

  @Test
  public void idsAssignation() {
    RepositoryData repoData =
        new RepositoryData(
            new RepositoryConfiguration("repo1", LionWebVersion.v2023_1, HistorySupport.DISABLED));
    assertEquals(Collections.singletonList("id-1"), repoData.ids(1));
    // If I store a node with id-2, the system should not assign me such id later on
    repoData.partitionIDs.add("id-2");
    repoData.store(
        Collections.singletonList(
            new SerializedClassifierInstance(
                "id-2", MetaPointer.from(LionCoreBuiltins.getNode(LionWebVersion.v2023_1)))));
    assertEquals(Collections.singletonList("id-3"), repoData.ids(1));

    // If I ask again for IDs to be assigned to me I should get different IDs
    assertEquals(Collections.singletonList("id-4"), repoData.ids(1));
  }

    @Test
    public void addAnnotationToExistingNode() {
        RepositoryData repositoryData =
                new RepositoryData(
                        new RepositoryConfiguration("repo1", LionWebVersion.v2023_1, HistorySupport.DISABLED));
        SerializedClassifierInstance n1 =
                new SerializedClassifierInstance("n1", MetaPointer.get("l1", "1.0", "c1"));
        SerializedClassifierInstance n2 =
                new SerializedClassifierInstance("n2", MetaPointer.get("l1", "1.0", "c1"));
        SerializedClassifierInstance n3 =
                new SerializedClassifierInstance("n3", MetaPointer.get("l1", "1.0", "c1"));
        SerializedClassifierInstance n4 =
                new SerializedClassifierInstance("n4", MetaPointer.get("l1", "1.0", "c1"));
        n1.addChildren(MetaPointer.get("l1", "1.0", "containmentA"), Collections.singletonList("n2"));
        n2.addChildren(MetaPointer.get("l1", "1.0", "containmentA"), Arrays.asList("n3", "n4"));
        n2.setParentNodeID("n1");
        n3.setParentNodeID("n2");
        n4.setParentNodeID("n2");

        repositoryData.partitionIDs.add("n1");
        repositoryData.store(Arrays.asList(n1, n2, n3, n4));
        SerializedClassifierInstance ann1 =
                new SerializedClassifierInstance("ann1", MetaPointer.get("lAnnotations", "1.0", "a1"));
        ann1.setParentNodeID("n1");
        n1.addAnnotation("ann1");
        repositoryData.store(Arrays.asList(n1, n2, n3, n4, ann1));

        assertEquals(
                new HashSet<>(Arrays.asList("n1", "n2", "n3", "n4", "ann1")), repositoryData.nodesByID.keySet());
        List<SerializedClassifierInstance> retrieved = new ArrayList<>();
        repositoryData.retrieve("n1", 2, retrieved);
        assertEquals(new HashSet<>(Arrays.asList("n1", "n2", "n3", "n4", "ann1")), retrieved.stream().map(n -> n.getID()).collect(Collectors.toSet()));
    }
}
