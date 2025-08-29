package io.lionweb.serialization.data;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

public class SerializedClassifierInstanceTest {

  private static MetaPointer simpleMetaPointer(String key) {
    return MetaPointer.get("L", "1", key);
  }

  @Test
  public void defaultsAreEmptyAndUnmodifiable() {
    SerializedClassifierInstance sci = new SerializedClassifierInstance();

    assertNull(sci.getID());
    assertNull(sci.getClassifier());
    assertNull(sci.getParentNodeID());

    assertTrue(sci.getProperties().isEmpty());
    assertTrue(sci.getContainments().isEmpty());
    assertTrue(sci.getChildren().isEmpty());
    assertTrue(sci.getReferences().isEmpty());
    assertTrue(sci.getAnnotations().isEmpty());

    assertThrows(
        UnsupportedOperationException.class,
        () -> sci.getProperties().add(SerializedPropertyValue.get(simpleMetaPointer("foo"), "a")));
    assertThrows(
        UnsupportedOperationException.class,
        () ->
            sci.getContainments()
                .add(
                    new SerializedContainmentValue(
                        simpleMetaPointer("bar"), Collections.singletonList("child1"))));
    assertThrows(
        UnsupportedOperationException.class,
        () ->
            sci.getReferences()
                .add(
                    new SerializedReferenceValue(
                        simpleMetaPointer("zum"),
                        Arrays.asList(new SerializedReferenceValue.Entry("a", "b")))));
    assertThrows(UnsupportedOperationException.class, () -> sci.getAnnotations().add("ann-1"));
    assertThrows(UnsupportedOperationException.class, () -> sci.getChildren().add("x"));
  }

  @Test
  public void constructorSetsIdAndClassifier() {
    MetaPointer classifier = simpleMetaPointer("C");
    SerializedClassifierInstance sci = new SerializedClassifierInstance("n1", classifier);

    assertEquals("n1", sci.getID());
    assertEquals(classifier, sci.getClassifier());
  }

  @Test
  public void idClassifierParentSettersGetters() {
    SerializedClassifierInstance sci = new SerializedClassifierInstance();
    MetaPointer classifier = simpleMetaPointer("C");

    sci.setID("id-123");
    sci.setClassifier(classifier);
    sci.setParentNodeID("parent-1");

    assertEquals("id-123", sci.getID());
    assertEquals(classifier, sci.getClassifier());
    assertEquals("parent-1", sci.getParentNodeID());
  }

  @Test
  public void propertiesAddAndGetByKeyAndMeta() {
    SerializedClassifierInstance sci = new SerializedClassifierInstance();
    MetaPointer pA = simpleMetaPointer("propA");
    MetaPointer pB = simpleMetaPointer("propB");

    sci.addPropertyValue(SerializedPropertyValue.get(pA, "VA"));
    sci.setPropertyValue(pB, "VB"); // via convenience

    assertEquals(2, sci.getProperties().size());
    assertEquals("VA", sci.getPropertyValue("propA"));
    assertEquals("VB", sci.getPropertyValue("propB"));
    assertNull(sci.getPropertyValue("nope"));

    assertEquals("VA", sci.getPropertyValue(pA));
    assertEquals("VB", sci.getPropertyValue(pB));
    assertNull(sci.getPropertyValue(simpleMetaPointer("other")));
  }

  @Test
  public void containmentsAddChildrenGetChildrenAndClear() {
    SerializedClassifierInstance sci = new SerializedClassifierInstance();
    MetaPointer contA = simpleMetaPointer("contA");
    MetaPointer contB = simpleMetaPointer("contB");

    sci.addChildren(contA, Arrays.asList("c1", "c2"));
    sci.addChildren(contB, Collections.singletonList("c3"));

    assertEquals(2, sci.getContainments().size());
    assertEquals(Arrays.asList("c1", "c2"), sci.getContainmentValues(contA));
    assertEquals(Collections.singletonList("c3"), sci.getContainmentValues(contB));
    assertEquals(Arrays.asList("c1", "c2", "c3"), sci.getChildren());

    sci.clearContainments();
    assertTrue(sci.getContainments().isEmpty());
    assertTrue(sci.getChildren().isEmpty());
    assertTrue(sci.getContainmentValues(contA).isEmpty());
  }

  @Test
  public void addChildAppendsOrCreatesEntry() {
    SerializedClassifierInstance sci = new SerializedClassifierInstance();
    MetaPointer cont = simpleMetaPointer("cont");

    // When containment absent, addChild creates it
    sci.addChild(cont, "x1");
    assertEquals(Collections.singletonList("x1"), sci.getContainmentValues(cont));

    // When containment present, addChild appends
    sci.addChild(cont, "x2");
    assertEquals(Arrays.asList("x1", "x2"), sci.getContainmentValues(cont));
  }

  @Test
  public void removeContainmentValueByMeta() {
    SerializedClassifierInstance sci = new SerializedClassifierInstance();
    MetaPointer cont = simpleMetaPointer("cont");
    MetaPointer other = simpleMetaPointer("other");
    sci.addChildren(cont, Arrays.asList("a", "b"));

    assertFalse(sci.removeContainmentValue(other));
    assertTrue(sci.removeContainmentValue(cont));
    assertTrue(sci.getContainments().isEmpty());
  }

  @Test
  public void removeChildRemovesFromAnyContainment() {
    SerializedClassifierInstance sci = new SerializedClassifierInstance();
    MetaPointer c1 = simpleMetaPointer("c1");
    MetaPointer c2 = simpleMetaPointer("c2");
    sci.addChildren(c1, Arrays.asList("a1", "a2"));
    sci.addChildren(c2, Arrays.asList("b1", "b2"));

    assertTrue(sci.removeChild("a2"));
    assertEquals(Collections.singletonList("a1"), sci.getContainmentValues(c1));
    assertEquals(Arrays.asList("b1", "b2"), sci.getContainmentValues(c2));

    assertFalse(sci.removeChild("nope"));
  }

  @Test
  public void referencesAddAndGet() {
    SerializedClassifierInstance sci = new SerializedClassifierInstance();
    MetaPointer r1 = simpleMetaPointer("ref1");
    MetaPointer r2 = simpleMetaPointer("ref2");

    SerializedReferenceValue.Entry e11 = new SerializedReferenceValue.Entry("RID-1", "RI-1");
    SerializedReferenceValue.Entry e12 = new SerializedReferenceValue.Entry("RID-2", "RI-2");
    SerializedReferenceValue.Entry e21 = new SerializedReferenceValue.Entry("RID-3", "RI-3");

    // Add via value list
    sci.addReferenceValue(r1, Arrays.asList(e11));
    // Add via entry (append)
    sci.addReferenceValue(r1, e12);
    // Add via object
    sci.addReferenceValue(new SerializedReferenceValue(r2, Collections.singletonList(e21)));

    List<SerializedReferenceValue.Entry> r1ValsByKey = sci.getReferenceValues("ref1");
    assertNotNull(r1ValsByKey);
    assertEquals(Arrays.asList(e11, e12), r1ValsByKey);

    List<SerializedReferenceValue.Entry> r2ValsByMP = sci.getReferenceValues(r2);
    assertEquals(Collections.singletonList(e21), r2ValsByMP);

    assertNull(sci.getReferenceValues("unknown"));
    assertTrue(sci.getReferenceValues(simpleMetaPointer("unknown")).isEmpty());

    // Unmodifiable
    assertThrows(UnsupportedOperationException.class, () -> r1ValsByKey.add(e21));
  }

  @Test
  public void annotationsSetAddRemoveGet() {
    SerializedClassifierInstance sci = new SerializedClassifierInstance();

    sci.setAnnotations(Arrays.asList("a1", "a2"));
    assertEquals(Arrays.asList("a1", "a2"), sci.getAnnotations());

    sci.addAnnotation("a3");
    assertEquals(Arrays.asList("a1", "a2", "a3"), sci.getAnnotations());

    assertTrue(sci.removeAnnotation("a2"));
    assertEquals(Arrays.asList("a1", "a3"), sci.getAnnotations());

    assertFalse(sci.removeAnnotation("nope"));

    // Unmodifiable
    assertThrows(UnsupportedOperationException.class, () -> sci.getAnnotations().add("x"));
  }

  @Test
  public void equalsAndHashCodeConsiderAllFields() {
    MetaPointer classifier = simpleMetaPointer("C");
    MetaPointer p = simpleMetaPointer("prop");
    MetaPointer cont = simpleMetaPointer("cont");
    MetaPointer ref = simpleMetaPointer("ref");

    SerializedClassifierInstance a = new SerializedClassifierInstance("id", classifier);
    a.setParentNodeID("parent");
    a.setPropertyValue(p, "V");
    a.addChildren(cont, Arrays.asList("c1", "c2"));
    a.addReferenceValue(
        ref, Collections.singletonList(new SerializedReferenceValue.Entry("RID", "RI")));
    a.setAnnotations(Arrays.asList("an1", "an2"));

    SerializedClassifierInstance b = new SerializedClassifierInstance("id", classifier);
    b.setParentNodeID("parent");
    b.setPropertyValue(p, "V");
    b.addChildren(cont, Arrays.asList("c1", "c2"));
    b.addReferenceValue(
        ref, Collections.singletonList(new SerializedReferenceValue.Entry("RID", "RI")));
    b.setAnnotations(Arrays.asList("an1", "an2"));

    assertEquals(a, b);
    assertEquals(a.hashCode(), b.hashCode());

    b.addAnnotation("diff");
    assertNotEquals(a, b);
  }

  @Test
  public void toStringContainsKeyInfo() {
    SerializedClassifierInstance sci =
        new SerializedClassifierInstance("idX", simpleMetaPointer("C"));
    sci.setParentNodeID("pY");
    String s = sci.toString();
    assertNotNull(s);
    assertTrue(s.contains("id"));
    assertTrue(s.contains("classifier"));
    assertTrue(s.contains("parentNodeID"));
  }
}
