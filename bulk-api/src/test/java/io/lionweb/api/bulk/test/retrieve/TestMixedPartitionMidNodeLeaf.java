package io.lionweb.api.bulk.test.retrieve;

import io.lionweb.lioncore.java.serialization.data.SerializedClassifierInstance;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TestMixedPartitionMidNodeLeaf extends ATestRetrieve {
  @Test
  public void infinite() {
    List<SerializedClassifierInstance> roots = getBulk().retrieve(Arrays.asList("partition-id", "midnode-id", "leaf-id"), null).getClassifierInstances();
    assertEquals(10, roots.size());
  }
  @Test
  public void depth0() {
    List<SerializedClassifierInstance> roots = getBulk().retrieve(Arrays.asList("partition-id", "midnode-id", "leaf-id"), 0).getClassifierInstances();
    assertEquals(3, roots.size());
  }
  @Test
  public void depth1() {
    List<SerializedClassifierInstance> roots = getBulk().retrieve(Arrays.asList("partition-id", "midnode-id", "leaf-id"), 1).getClassifierInstances();
    assertEquals(6, roots.size());
  }
  @Test
  public void depth2() {
    List<SerializedClassifierInstance> roots = getBulk().retrieve(Arrays.asList("partition-id", "midnode-id", "leaf-id"), 3).getClassifierInstances();
    assertEquals(10, roots.size());
  }
}
