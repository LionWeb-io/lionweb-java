package io.lionweb.api.bulk.test.retrieve;

import io.lionweb.lioncore.java.serialization.data.SerializedClassifierInstance;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TestSingleMidNode extends ATestRetrieve {
  @Test
  public void infinite() {
    List<SerializedClassifierInstance> roots = getBulk().retrieve(Arrays.asList("midnode-id"), null).getClassifierInstances();
    assertEquals(5, roots.size());
  }
  @Test
  public void depth0() {
    List<SerializedClassifierInstance> roots = getBulk().retrieve(Arrays.asList("midnode-id"), 0).getClassifierInstances();
    assertEquals(1, roots.size());
  }
  @Test
  public void depth1() {
    List<SerializedClassifierInstance> roots = getBulk().retrieve(Arrays.asList("midnode-id"), 1).getClassifierInstances();
    assertEquals(3, roots.size());
  }
  @Test
  public void depth2() {
    List<SerializedClassifierInstance> roots = getBulk().retrieve(Arrays.asList("midnode-id"), 2).getClassifierInstances();
    assertEquals(4, roots.size());
  }
}
