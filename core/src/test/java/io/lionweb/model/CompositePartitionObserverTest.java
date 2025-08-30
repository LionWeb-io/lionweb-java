package io.lionweb.model;

import static org.junit.Assert.*;

import io.lionweb.language.*;
import io.lionweb.model.impl.DynamicAnnotationInstance;
import io.lionweb.model.impl.DynamicNode;
import java.util.Arrays;
import org.junit.Test;

public class CompositePartitionObserverTest {

  @Test
  public void combine_twoSimpleObservers() {
    MockPartitionObserver obs1 = new MockPartitionObserver();
    MockPartitionObserver obs2 = new MockPartitionObserver();

    PartitionObserver combined = CompositePartitionObserver.combine(obs1, obs2);

    assertTrue(combined instanceof CompositePartitionObserver);
    CompositePartitionObserver composite = (CompositePartitionObserver) combined;
    assertEquals(2, composite.getElements().size());
    assertTrue(composite.getElements().contains(obs1));
    assertTrue(composite.getElements().contains(obs2));
  }

  @Test
  public void combine_returnsOriginalWhenCombiningWithSingleObserver() {
    MockPartitionObserver obs1 = new MockPartitionObserver();
    MockPartitionObserver obs2 = new MockPartitionObserver();
    MockPartitionObserver obs3 = new MockPartitionObserver();

    CompositePartitionObserver composite =
        (CompositePartitionObserver) CompositePartitionObserver.combine(obs1, obs2);
    PartitionObserver result = CompositePartitionObserver.combine(composite, obs3);

    assertTrue(result instanceof CompositePartitionObserver);
    assertEquals(3, ((CompositePartitionObserver) result).getElements().size());
  }

  @Test
  public void combine_flattensNestedComposites() {
    MockPartitionObserver obs1 = new MockPartitionObserver();
    MockPartitionObserver obs2 = new MockPartitionObserver();
    MockPartitionObserver obs3 = new MockPartitionObserver();
    MockPartitionObserver obs4 = new MockPartitionObserver();

    CompositePartitionObserver left =
        (CompositePartitionObserver) CompositePartitionObserver.combine(obs1, obs2);
    CompositePartitionObserver right =
        (CompositePartitionObserver) CompositePartitionObserver.combine(obs3, obs4);

    PartitionObserver result = CompositePartitionObserver.combine(left, right);

    assertEquals(4, ((CompositePartitionObserver) result).getElements().size());
  }

  @Test
  public void remove_returnsObserverWhenOnlyOneRemains() {
    MockPartitionObserver obs1 = new MockPartitionObserver();
    MockPartitionObserver obs2 = new MockPartitionObserver();

    CompositePartitionObserver composite =
        (CompositePartitionObserver) CompositePartitionObserver.combine(obs1, obs2);

    PartitionObserver result = composite.remove(obs1);

    assertEquals(obs2, result);
  }

  @Test
  public void remove_throwsWhenObserverNotFound() {
    MockPartitionObserver obs1 = new MockPartitionObserver();
    MockPartitionObserver obs2 = new MockPartitionObserver();
    MockPartitionObserver obs3 = new MockPartitionObserver();

    CompositePartitionObserver composite =
        (CompositePartitionObserver) CompositePartitionObserver.combine(obs1, obs2);

    assertThrows(IllegalArgumentException.class, () -> composite.remove(obs3));
  }

  @Test
  public void propertyChanged_notifiesAllObservers() {
    MockPartitionObserver obs1 = new MockPartitionObserver();
    MockPartitionObserver obs2 = new MockPartitionObserver();

    CompositePartitionObserver composite =
        (CompositePartitionObserver) CompositePartitionObserver.combine(obs1, obs2);

    Concept concept = new Concept();
    DynamicNode node = new DynamicNode("test", concept);
    Property property = new Property();

    composite.propertyChanged(node, property, "old", "new");

    assertEquals(
        Arrays.asList(
            new MockPartitionObserver.PropertyChangedRecord(node, property, "old", "new")),
        obs1.getRecords());
    assertEquals(
        Arrays.asList(
            new MockPartitionObserver.PropertyChangedRecord(node, property, "old", "new")),
        obs2.getRecords());
  }

  @Test
  public void childAdded_notifiesAllObservers() {
    MockPartitionObserver obs1 = new MockPartitionObserver();
    MockPartitionObserver obs2 = new MockPartitionObserver();

    CompositePartitionObserver composite =
        (CompositePartitionObserver) CompositePartitionObserver.combine(obs1, obs2);

    Concept concept = new Concept();
    DynamicNode parent = new DynamicNode("parent", concept);
    DynamicNode child = new DynamicNode("child", concept);
    Containment containment = new Containment();

    composite.childAdded(parent, containment, 0, child);

    assertEquals(
        Arrays.asList(new MockPartitionObserver.ChildAddedRecord(parent, containment, 0, child)),
        obs1.getRecords());
    assertEquals(
        Arrays.asList(new MockPartitionObserver.ChildAddedRecord(parent, containment, 0, child)),
        obs2.getRecords());
  }

  @Test
  public void referenceValueAdded_notifiesAllObservers() {
    MockPartitionObserver obs1 = new MockPartitionObserver();
    MockPartitionObserver obs2 = new MockPartitionObserver();

    CompositePartitionObserver composite =
        (CompositePartitionObserver) CompositePartitionObserver.combine(obs1, obs2);

    Concept concept = new Concept();
    DynamicNode node = new DynamicNode("test", concept);
    Reference reference = new Reference();
    ReferenceValue refValue = new ReferenceValue(node, "resolve");

    composite.referenceValueAdded(node, reference, refValue);

    assertEquals(
        Arrays.asList(new MockPartitionObserver.ReferenceAddedRecord(node, reference, refValue)),
        obs1.getRecords());
    assertEquals(
        Arrays.asList(new MockPartitionObserver.ReferenceAddedRecord(node, reference, refValue)),
        obs2.getRecords());
  }

  @Test
  public void combine_withSameObserverTwice_deduplicates() {
    MockPartitionObserver obs1 = new MockPartitionObserver();
    MockPartitionObserver obs2 = new MockPartitionObserver();

    // Combine obs1 and obs2
    CompositePartitionObserver composite1 =
        (CompositePartitionObserver) CompositePartitionObserver.combine(obs1, obs2);

    // Combine the composite with obs1 again - should not duplicate
    PartitionObserver result = CompositePartitionObserver.combine(composite1, obs1);

    assertEquals(2, ((CompositePartitionObserver) result).getElements().size());
    assertTrue(((CompositePartitionObserver) result).getElements().contains(obs1));
    assertTrue(((CompositePartitionObserver) result).getElements().contains(obs2));
  }

  @Test
  public void childRemoved_notifiesAllObservers() {
    MockPartitionObserver obs1 = new MockPartitionObserver();
    MockPartitionObserver obs2 = new MockPartitionObserver();

    CompositePartitionObserver composite =
        (CompositePartitionObserver) CompositePartitionObserver.combine(obs1, obs2);

    Concept concept = new Concept();
    DynamicNode parent = new DynamicNode("parent", concept);
    DynamicNode child = new DynamicNode("child", concept);
    Containment containment = new Containment();

    composite.childRemoved(parent, containment, 0, child);

    assertEquals(1, obs1.getRecords().size());
    assertEquals(1, obs2.getRecords().size());
    assertTrue(obs1.getRecords().get(0) instanceof MockPartitionObserver.ChildRemovedRecord);
    assertTrue(obs2.getRecords().get(0) instanceof MockPartitionObserver.ChildRemovedRecord);
  }

  @Test
  public void annotationAdded_notifiesAllObservers() {
    MockPartitionObserver obs1 = new MockPartitionObserver();
    MockPartitionObserver obs2 = new MockPartitionObserver();

    CompositePartitionObserver composite =
        (CompositePartitionObserver) CompositePartitionObserver.combine(obs1, obs2);

    Concept concept = new Concept();
    DynamicNode node = new DynamicNode("node", concept);
    DynamicAnnotationInstance annotation = new DynamicAnnotationInstance("ann", new Annotation());

    composite.annotationAdded(node, 0, annotation);

    assertEquals(1, obs1.getRecords().size());
    assertEquals(1, obs2.getRecords().size());
    assertTrue(obs1.getRecords().get(0) instanceof MockPartitionObserver.AnnotationAddedRecord);
    assertTrue(obs2.getRecords().get(0) instanceof MockPartitionObserver.AnnotationAddedRecord);
  }

  @Test
  public void annotationRemoved_notifiesAllObservers() {
    MockPartitionObserver obs1 = new MockPartitionObserver();
    MockPartitionObserver obs2 = new MockPartitionObserver();

    CompositePartitionObserver composite =
        (CompositePartitionObserver) CompositePartitionObserver.combine(obs1, obs2);

    Concept concept = new Concept();
    DynamicNode node = new DynamicNode("node", concept);
    DynamicAnnotationInstance annotation = new DynamicAnnotationInstance("ann", new Annotation());

    composite.annotationRemoved(node, 0, annotation);

    assertEquals(1, obs1.getRecords().size());
    assertEquals(1, obs2.getRecords().size());
    assertTrue(obs1.getRecords().get(0) instanceof MockPartitionObserver.AnnotationRemovedRecord);
    assertTrue(obs2.getRecords().get(0) instanceof MockPartitionObserver.AnnotationRemovedRecord);
  }

  @Test
  public void referenceValueChanged_notifiesAllObservers() {
    MockPartitionObserver obs1 = new MockPartitionObserver();
    MockPartitionObserver obs2 = new MockPartitionObserver();

    CompositePartitionObserver composite =
        (CompositePartitionObserver) CompositePartitionObserver.combine(obs1, obs2);

    Concept concept = new Concept();
    DynamicNode node = new DynamicNode("node", concept);
    Reference reference = new Reference();

    composite.referenceValueChanged(node, reference, 0, "oldRef", "oldInfo", "newRef", "newInfo");

    assertEquals(1, obs1.getRecords().size());
    assertEquals(1, obs2.getRecords().size());
    assertTrue(obs1.getRecords().get(0) instanceof MockPartitionObserver.ReferenceChangedRecord);
    assertTrue(obs2.getRecords().get(0) instanceof MockPartitionObserver.ReferenceChangedRecord);
  }

  @Test
  public void referenceValueRemoved_notifiesAllObservers() {
    MockPartitionObserver obs1 = new MockPartitionObserver();
    MockPartitionObserver obs2 = new MockPartitionObserver();

    CompositePartitionObserver composite =
        (CompositePartitionObserver) CompositePartitionObserver.combine(obs1, obs2);

    Concept concept = new Concept();
    DynamicNode node = new DynamicNode("node", concept);
    Reference reference = new Reference();
    ReferenceValue refValue = new ReferenceValue(node, "resolve");

    composite.referenceValueRemoved(node, reference, 0, refValue);

    assertEquals(1, obs1.getRecords().size());
    assertEquals(1, obs2.getRecords().size());
    assertTrue(obs1.getRecords().get(0) instanceof MockPartitionObserver.ReferenceRemovedRecord);
    assertTrue(obs2.getRecords().get(0) instanceof MockPartitionObserver.ReferenceRemovedRecord);
  }
}
