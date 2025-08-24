package io.lionweb.model.impl;

import io.lionweb.language.*;
import io.lionweb.lioncore.LionCore;
import io.lionweb.model.AnnotationInstance;
import io.lionweb.model.MockPartitionObserver;
import io.lionweb.model.ReferenceValue;
import io.lionweb.serialization.Book;
import io.lionweb.serialization.Library;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class AbstractNodeTest {

  @Test
  public void partitionObserversCanBeAssignedOnlyToRootNodes() {
      Language language = new Language();
      language.setID("l1");
      Concept concept = new Concept();
      concept.setID("c1");
      language.addElement(concept);

      assertThrows(UnsupportedOperationException.class, () -> concept.registerPartitionObserver(new MockPartitionObserver()));
      assertThrows(UnsupportedOperationException.class, () -> concept.unregisterPartitionObserver(new MockPartitionObserver()));
  }

}
