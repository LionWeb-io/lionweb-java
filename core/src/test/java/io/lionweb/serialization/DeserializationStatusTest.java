package io.lionweb.serialization;

import static org.junit.jupiter.api.Assertions.*;

import io.lionweb.api.ClassifierInstanceResolver;
import io.lionweb.serialization.data.MetaPointer;
import io.lionweb.serialization.data.SerializedClassifierInstance;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

public class DeserializationStatusTest {

  private static final MetaPointer MP = MetaPointer.get("l", "1", "C");

  private DeserializationStatus statusOf(SerializedClassifierInstance... nodes) {
    ClassifierInstanceResolver empty = id -> null;
    return new DeserializationStatus(
        Arrays.asList(nodes), empty, new DataTypesValuesSerialization());
  }

  @Test
  public void placeAtRemovesNodeFromNodesToSort() {
    SerializedClassifierInstance n1 = new SerializedClassifierInstance("n1", MP);
    SerializedClassifierInstance n2 = new SerializedClassifierInstance("n2", MP);
    SerializedClassifierInstance n3 = new SerializedClassifierInstance("n3", MP);
    DeserializationStatus status = statusOf(n1, n2, n3);

    assertEquals(3, status.howManyToSort());
    status.placeAt(0);
    assertEquals(2, status.howManyToSort());
  }

  @Test
  public void placeAtMovesNodeToSortedList() {
    SerializedClassifierInstance n1 = new SerializedClassifierInstance("n1", MP);
    SerializedClassifierInstance n2 = new SerializedClassifierInstance("n2", MP);
    DeserializationStatus status = statusOf(n1, n2);

    status.placeAt(0);

    assertEquals(1, status.howManySorted());
    assertTrue(status.isSortedID("n1"));
  }

  @Test
  public void placeAtLastElement() {
    SerializedClassifierInstance n1 = new SerializedClassifierInstance("n1", MP);
    SerializedClassifierInstance n2 = new SerializedClassifierInstance("n2", MP);
    DeserializationStatus status = statusOf(n1, n2);

    // Place last element (index == last, no swap needed)
    status.placeAt(1);

    assertTrue(status.isSortedID("n2"));
    assertEquals(1, status.howManyToSort());
    assertEquals("n1", status.getNodeToSort(0).getID());
  }

  @Test
  public void placeAtAllNodesProducesCorrectSortedList() {
    SerializedClassifierInstance n1 = new SerializedClassifierInstance("n1", MP);
    SerializedClassifierInstance n2 = new SerializedClassifierInstance("n2", MP);
    SerializedClassifierInstance n3 = new SerializedClassifierInstance("n3", MP);
    DeserializationStatus status = statusOf(n1, n2, n3);

    // Place index 1 (n2), then index 0 of remaining, then remaining
    status.placeAt(1); // places n2; n3 swaps to index 1
    status.placeAt(0); // places n1; n3 now at index 0
    status.placeAt(0); // places n3

    assertEquals(0, status.howManyToSort());
    assertEquals(3, status.howManySorted());

    List<SerializedClassifierInstance> sorted = status.getSortedList();
    assertTrue(sorted.stream().anyMatch(n -> "n1".equals(n.getID())));
    assertTrue(sorted.stream().anyMatch(n -> "n2".equals(n.getID())));
    assertTrue(sorted.stream().anyMatch(n -> "n3".equals(n.getID())));
  }

  @Test
  public void isSortedIDReturnsFalseBeforePlacement() {
    SerializedClassifierInstance n1 = new SerializedClassifierInstance("n1", MP);
    DeserializationStatus status = statusOf(n1);

    assertFalse(status.isSortedID("n1"));
    status.placeAt(0);
    assertTrue(status.isSortedID("n1"));
  }
}
