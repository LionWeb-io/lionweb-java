package io.lionweb.api.bulk.test;

import io.lionweb.lioncore.java.serialization.data.SerializedClassifierInstance;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertFalse;

public class TestPartitions extends ATestBulk {
  @Test
  public void listPartitions() {
    List<SerializedClassifierInstance> roots = getBulk().partitions().getClassifierInstances();
    assertFalse(roots.toString(), roots.isEmpty());
  }
}
