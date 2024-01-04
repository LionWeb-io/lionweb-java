package io.lionweb.api.bulk.test.retrieve;

import io.lionweb.lioncore.java.serialization.data.SerializedClassifierInstance;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class TestSingleUnknown extends ATestRetrieve {
  @Test
  public void infinite() {
    List<SerializedClassifierInstance> roots = getBulk().retrieve(Arrays.asList("unknown-id"), null).getClassifierInstances();
    assertTrue(roots.isEmpty());
  }
  @Test
  public void depth0() {
    List<SerializedClassifierInstance> roots = getBulk().retrieve(Arrays.asList("unknown-id"), 0).getClassifierInstances();
    assertTrue(roots.isEmpty());
  }
  @Test
  public void depth1() {
    List<SerializedClassifierInstance> roots = getBulk().retrieve(Arrays.asList("unknown-id"), 1).getClassifierInstances();
    assertTrue(roots.isEmpty());
  }
  @Test
  public void depth2() {
    List<SerializedClassifierInstance> roots = getBulk().retrieve(Arrays.asList("unknown-id"), 2).getClassifierInstances();
    assertTrue(roots.isEmpty());
  }
}
