package io.lionweb.client.partitioned;

import static org.junit.jupiter.api.Assertions.*;

import io.lionweb.LionWebVersion;
import io.lionweb.serialization.data.*;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class DiskRepositoryBackendTest {

  @TempDir Path tempDir;

  // ---------------------------------------------------------------------------
  // Helpers
  // ---------------------------------------------------------------------------

  private static SerializationChunk chunk(SerializedClassifierInstance... nodes) {
    return SerializationChunk.fromNodes(LionWebVersion.v2023_1, Arrays.asList(nodes));
  }

  private static MetaPointer mp(String lang, String version, String key) {
    return MetaPointer.get(lang, version, key);
  }

  private static SerializedClassifierInstance node(String id, MetaPointer classifier) {
    return new SerializedClassifierInstance(id, classifier);
  }

  private List<SerializedClassifierInstance> roundtrip(SerializedClassifierInstance... nodes)
      throws IOException {
    DiskRepositoryBackend backend = new DiskRepositoryBackend(tempDir);
    backend.savePartition("repo", "p1", chunk(nodes));
    return backend.loadPartition("repo", "p1");
  }

  private static SerializedClassifierInstance findById(
      List<SerializedClassifierInstance> nodes, String id) {
    return nodes.stream()
        .filter(n -> id.equals(n.getID()))
        .findFirst()
        .orElseThrow(() -> new AssertionError("No node with id=" + id));
  }

  // ---------------------------------------------------------------------------
  // Basic roundtrip
  // ---------------------------------------------------------------------------

  @Test
  void minimalNode() throws IOException {
    MetaPointer cls = mp("lang", "1.0", "Concept");
    SerializedClassifierInstance n = node("id-1", cls);

    List<SerializedClassifierInstance> loaded = roundtrip(n);

    assertEquals(1, loaded.size());
    SerializedClassifierInstance l = loaded.get(0);
    assertEquals("id-1", l.getID());
    assertNull(l.getParentNodeID());
    assertEquals(cls, l.getClassifier());
    assertTrue(l.getProperties().isEmpty());
    assertTrue(l.getContainments().isEmpty());
    assertTrue(l.getReferences().isEmpty());
    assertTrue(l.getAnnotations().isEmpty());
  }

  @Test
  void nodeWithParent() throws IOException {
    MetaPointer cls = mp("lang", "1.0", "Concept");
    SerializedClassifierInstance parent = node("parent-id", cls);
    SerializedClassifierInstance child = node("child-id", cls);
    child.setParentNodeID("parent-id");
    parent.unsafeAppendContainmentValue(
        mp("lang", "1.0", "children"), Collections.singletonList("child-id"));

    List<SerializedClassifierInstance> loaded = roundtrip(parent, child);

    assertEquals(2, loaded.size());
    SerializedClassifierInstance loadedChild = findById(loaded, "child-id");
    assertEquals("parent-id", loadedChild.getParentNodeID());
  }

  // ---------------------------------------------------------------------------
  // Properties
  // ---------------------------------------------------------------------------

  @Test
  void propertyWithNonNullValue() throws IOException {
    MetaPointer cls = mp("lang", "1.0", "Concept");
    MetaPointer propMp = mp("lang", "1.0", "name");
    SerializedClassifierInstance n = node("n1", cls);
    n.setPropertyValue(propMp, "hello");

    List<SerializedClassifierInstance> loaded = roundtrip(n);

    SerializedClassifierInstance l = loaded.get(0);
    assertEquals(1, l.getProperties().size());
    assertEquals(propMp, l.getProperties().get(0).getMetaPointer());
    assertEquals("hello", l.getProperties().get(0).getValue());
  }

  @Test
  void propertyWithNullValue() throws IOException {
    MetaPointer cls = mp("lang", "1.0", "Concept");
    MetaPointer propMp = mp("lang", "1.0", "name");
    SerializedClassifierInstance n = node("n1", cls);
    n.setPropertyValue(propMp, null);

    List<SerializedClassifierInstance> loaded = roundtrip(n);

    SerializedClassifierInstance l = loaded.get(0);
    assertEquals(1, l.getProperties().size());
    assertNull(l.getProperties().get(0).getValue());
  }

  @Test
  void multipleProperties() throws IOException {
    MetaPointer cls = mp("lang", "1.0", "Concept");
    SerializedClassifierInstance n = node("n1", cls);
    n.setPropertyValue(mp("lang", "1.0", "p1"), "v1");
    n.setPropertyValue(mp("lang", "1.0", "p2"), "v2");
    n.setPropertyValue(mp("lang", "1.0", "p3"), "v3");

    List<SerializedClassifierInstance> loaded = roundtrip(n);

    SerializedClassifierInstance l = loaded.get(0);
    assertEquals(3, l.getProperties().size());
    assertEquals("v1", l.getPropertyValue(mp("lang", "1.0", "p1")));
    assertEquals("v2", l.getPropertyValue(mp("lang", "1.0", "p2")));
    assertEquals("v3", l.getPropertyValue(mp("lang", "1.0", "p3")));
  }

  @Test
  void longPropertyValue() throws IOException {
    // Exceeds the old DataOutputStream.writeUTF limit of 65535 encoded bytes
    String longValue = "x".repeat(70_000);
    MetaPointer cls = mp("lang", "1.0", "Concept");
    SerializedClassifierInstance n = node("n1", cls);
    n.setPropertyValue(mp("lang", "1.0", "body"), longValue);

    List<SerializedClassifierInstance> loaded = roundtrip(n);

    assertEquals(longValue, loaded.get(0).getPropertyValue(mp("lang", "1.0", "body")));
  }

  // ---------------------------------------------------------------------------
  // Containments
  // ---------------------------------------------------------------------------

  @Test
  void containmentWithChildren() throws IOException {
    MetaPointer cls = mp("lang", "1.0", "Concept");
    MetaPointer contMp = mp("lang", "1.0", "items");
    SerializedClassifierInstance parent = node("p", cls);
    parent.unsafeAppendContainmentValue(contMp, Arrays.asList("c1", "c2", "c3"));

    SerializedClassifierInstance c1 = node("c1", cls);
    c1.setParentNodeID("p");
    SerializedClassifierInstance c2 = node("c2", cls);
    c2.setParentNodeID("p");
    SerializedClassifierInstance c3 = node("c3", cls);
    c3.setParentNodeID("p");

    List<SerializedClassifierInstance> loaded = roundtrip(parent, c1, c2, c3);

    SerializedClassifierInstance lp = findById(loaded, "p");
    assertEquals(1, lp.getContainments().size());
    assertEquals(contMp, lp.getContainments().get(0).getMetaPointer());
    assertEquals(Arrays.asList("c1", "c2", "c3"), lp.getContainments().get(0).getChildrenIds());
  }

  @Test
  void multipleContainments() throws IOException {
    MetaPointer cls = mp("lang", "1.0", "Concept");
    SerializedClassifierInstance parent = node("p", cls);
    parent.unsafeAppendContainmentValue(
        mp("lang", "1.0", "left"), Collections.singletonList("l"));
    parent.unsafeAppendContainmentValue(
        mp("lang", "1.0", "right"), Collections.singletonList("r"));

    SerializedClassifierInstance l = node("l", cls);
    l.setParentNodeID("p");
    SerializedClassifierInstance r = node("r", cls);
    r.setParentNodeID("p");

    List<SerializedClassifierInstance> loaded = roundtrip(parent, l, r);

    SerializedClassifierInstance lp = findById(loaded, "p");
    assertEquals(2, lp.getContainments().size());
  }

  // ---------------------------------------------------------------------------
  // References
  // ---------------------------------------------------------------------------

  @Test
  void referenceWithResolveInfoAndTarget() throws IOException {
    MetaPointer cls = mp("lang", "1.0", "Concept");
    MetaPointer refMp = mp("lang", "1.0", "target");
    SerializedClassifierInstance n = node("n1", cls);
    SerializedReferenceValue ref = new SerializedReferenceValue(refMp);
    SerializedReferenceValue.Entry entry = new SerializedReferenceValue.Entry("ref-id", "MyName");
    ref.addValue(entry);
    n.unsafeAppendReferenceValue(ref);

    List<SerializedClassifierInstance> loaded = roundtrip(n);

    SerializedClassifierInstance l = loaded.get(0);
    assertEquals(1, l.getReferences().size());
    assertEquals(refMp, l.getReferences().get(0).getMetaPointer());
    assertEquals(1, l.getReferences().get(0).getValue().size());
    SerializedReferenceValue.Entry le = l.getReferences().get(0).getValue().get(0);
    assertEquals("ref-id", le.getReference());
    assertEquals("MyName", le.getResolveInfo());
  }

  @Test
  void referenceWithNullTargetAndNullResolveInfo() throws IOException {
    MetaPointer cls = mp("lang", "1.0", "Concept");
    MetaPointer refMp = mp("lang", "1.0", "target");
    SerializedClassifierInstance n = node("n1", cls);
    SerializedReferenceValue ref = new SerializedReferenceValue(refMp);
    SerializedReferenceValue.Entry entry = new SerializedReferenceValue.Entry(null, null);
    ref.addValue(entry);
    n.unsafeAppendReferenceValue(ref);

    List<SerializedClassifierInstance> loaded = roundtrip(n);

    SerializedReferenceValue.Entry le =
        loaded.get(0).getReferences().get(0).getValue().get(0);
    assertNull(le.getReference());
    assertNull(le.getResolveInfo());
  }

  @Test
  void referenceWithNullTargetOnly() throws IOException {
    MetaPointer cls = mp("lang", "1.0", "Concept");
    MetaPointer refMp = mp("lang", "1.0", "target");
    SerializedClassifierInstance n = node("n1", cls);
    SerializedReferenceValue ref = new SerializedReferenceValue(refMp);
    ref.addValue(new SerializedReferenceValue.Entry(null, "SomeName"));
    n.unsafeAppendReferenceValue(ref);

    List<SerializedClassifierInstance> loaded = roundtrip(n);

    SerializedReferenceValue.Entry le =
        loaded.get(0).getReferences().get(0).getValue().get(0);
    assertNull(le.getReference());
    assertEquals("SomeName", le.getResolveInfo());
  }

  // ---------------------------------------------------------------------------
  // Annotations
  // ---------------------------------------------------------------------------

  @Test
  void annotations() throws IOException {
    MetaPointer cls = mp("lang", "1.0", "Concept");
    SerializedClassifierInstance n = node("n1", cls);
    n.addAnnotation("ann-1");
    n.addAnnotation("ann-2");

    List<SerializedClassifierInstance> loaded = roundtrip(n);

    assertEquals(Arrays.asList("ann-1", "ann-2"), loaded.get(0).getAnnotations());
  }

  // ---------------------------------------------------------------------------
  // Everything at once
  // ---------------------------------------------------------------------------

  @Test
  void nodeWithAllFeatures() throws IOException {
    MetaPointer cls = mp("lang", "1.0", "Concept");
    SerializedClassifierInstance n = node("n1", cls);
    n.setParentNodeID("parent-x");
    n.setPropertyValue(mp("lang", "1.0", "name"), "Alice");
    n.unsafeAppendContainmentValue(
        mp("lang", "1.0", "items"), Arrays.asList("c1", "c2"));
    SerializedReferenceValue ref = new SerializedReferenceValue(mp("lang", "1.0", "ref"));
    ref.addValue(new SerializedReferenceValue.Entry("target-id", "TargetName"));
    n.unsafeAppendReferenceValue(ref);
    n.addAnnotation("ann-1");

    // We need the containment children to exist in the chunk
    SerializedClassifierInstance c1 = node("c1", cls);
    c1.setParentNodeID("n1");
    SerializedClassifierInstance c2 = node("c2", cls);
    c2.setParentNodeID("n1");

    // Use a chunk that also has a fake root so fromNodes is valid
    SerializedClassifierInstance root = node("parent-x", cls);
    root.unsafeAppendContainmentValue(
        mp("lang", "1.0", "children"), Collections.singletonList("n1"));

    List<SerializedClassifierInstance> loaded = roundtrip(root, n, c1, c2);

    SerializedClassifierInstance l = findById(loaded, "n1");
    assertEquals("parent-x", l.getParentNodeID());
    assertEquals("Alice", l.getPropertyValue(mp("lang", "1.0", "name")));
    assertEquals(
        Arrays.asList("c1", "c2"),
        l.getContainments().get(0).getChildrenIds());
    assertEquals("target-id", l.getReferences().get(0).getValue().get(0).getReference());
    assertEquals("TargetName", l.getReferences().get(0).getValue().get(0).getResolveInfo());
    assertEquals(Collections.singletonList("ann-1"), l.getAnnotations());
  }

  // ---------------------------------------------------------------------------
  // Multiple nodes / multiple partitions
  // ---------------------------------------------------------------------------

  @Test
  void manyNodes() throws IOException {
    MetaPointer cls = mp("lang", "1.0", "Concept");
    int count = 200;
    SerializedClassifierInstance[] nodes = new SerializedClassifierInstance[count];
    for (int i = 0; i < count; i++) {
      nodes[i] = node("id-" + i, cls);
      nodes[i].setPropertyValue(mp("lang", "1.0", "index"), String.valueOf(i));
    }

    List<SerializedClassifierInstance> loaded = roundtrip(nodes);

    assertEquals(count, loaded.size());
    for (SerializedClassifierInstance l : loaded) {
      String idx = l.getPropertyValue(mp("lang", "1.0", "index"));
      assertNotNull(idx);
      assertEquals("id-" + idx, l.getID());
    }
  }

  @Test
  void multiplePartitions() throws IOException {
    MetaPointer cls = mp("lang", "1.0", "Concept");
    DiskRepositoryBackend backend = new DiskRepositoryBackend(tempDir);

    backend.savePartition("repo", "p1", chunk(node("a", cls)));
    backend.savePartition("repo", "p2", chunk(node("b", cls)));

    List<SerializedClassifierInstance> p1 = backend.loadPartition("repo", "p1");
    List<SerializedClassifierInstance> p2 = backend.loadPartition("repo", "p2");

    assertEquals(1, p1.size());
    assertEquals("a", p1.get(0).getID());
    assertEquals(1, p2.size());
    assertEquals("b", p2.get(0).getID());
  }

  // ---------------------------------------------------------------------------
  // Backend operations
  // ---------------------------------------------------------------------------

  @Test
  void missingPartitionReturnsEmpty() throws IOException {
    DiskRepositoryBackend backend = new DiskRepositoryBackend(tempDir);
    List<SerializedClassifierInstance> result = backend.loadPartition("repo", "nonexistent");
    assertTrue(result.isEmpty());
  }

  @Test
  void hasPartition() throws IOException {
    MetaPointer cls = mp("lang", "1.0", "Concept");
    DiskRepositoryBackend backend = new DiskRepositoryBackend(tempDir);

    assertFalse(backend.hasPartition("repo", "p1"));
    backend.savePartition("repo", "p1", chunk(node("n1", cls)));
    assertTrue(backend.hasPartition("repo", "p1"));
  }

  @Test
  void deletePartition() throws IOException {
    MetaPointer cls = mp("lang", "1.0", "Concept");
    DiskRepositoryBackend backend = new DiskRepositoryBackend(tempDir);

    backend.savePartition("repo", "p1", chunk(node("n1", cls)));
    assertTrue(backend.hasPartition("repo", "p1"));
    backend.deletePartition("repo", "p1");
    assertFalse(backend.hasPartition("repo", "p1"));
    assertTrue(backend.loadPartition("repo", "p1").isEmpty());
  }

  @Test
  void listPersistedPartitionIds() throws IOException {
    MetaPointer cls = mp("lang", "1.0", "Concept");
    DiskRepositoryBackend backend = new DiskRepositoryBackend(tempDir);

    assertTrue(backend.listPersistedPartitionIds("repo").isEmpty());
    backend.savePartition("repo", "p1", chunk(node("n1", cls)));
    backend.savePartition("repo", "p2", chunk(node("n2", cls)));

    List<String> ids = backend.listPersistedPartitionIds("repo");
    assertEquals(2, ids.size());
    assertTrue(ids.contains("p1"));
    assertTrue(ids.contains("p2"));
  }

  @Test
  void overwritePartition() throws IOException {
    MetaPointer cls = mp("lang", "1.0", "Concept");
    DiskRepositoryBackend backend = new DiskRepositoryBackend(tempDir);

    backend.savePartition("repo", "p1", chunk(node("old", cls)));
    backend.savePartition("repo", "p1", chunk(node("new", cls)));

    List<SerializedClassifierInstance> loaded = backend.loadPartition("repo", "p1");
    assertEquals(1, loaded.size());
    assertEquals("new", loaded.get(0).getID());
  }
}
