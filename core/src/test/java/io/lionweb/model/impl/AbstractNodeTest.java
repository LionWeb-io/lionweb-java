package io.lionweb.model.impl;

import static org.junit.Assert.assertThrows;

import io.lionweb.language.*;
import io.lionweb.model.MockPartitionObserver;
import org.junit.Test;

public class AbstractNodeTest {

  @Test
  public void partitionObserversCanBeAssignedOnlyToRootNodes() {
    Language language = new Language();
    language.setID("l1");
    Concept concept = new Concept();
    concept.setID("c1");
    language.addElement(concept);

    assertThrows(
        UnsupportedOperationException.class,
        () -> concept.registerPartitionObserver(new MockPartitionObserver()));
    assertThrows(
        UnsupportedOperationException.class,
        () -> concept.unregisterPartitionObserver(new MockPartitionObserver()));
  }
}
