package io.lionweb.api.bulk.test.retrieve.lowlevel;

import io.lionweb.api.bulk.lowlevel.IRetrieveResponse;
import io.lionweb.api.bulk.test.retrieve.ATestRetrieve;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertFalse;

public class TestNoNodeIds extends ATestRetrieve {
  @Test
  public void infinite() {
    IRetrieveResponse response = getBulkLowlevel().retrieve(Collections.emptyList(), null);
    assertFalse(response.isOk());
    assertFalse(response.isValidNodeIds());
  }
  @Test
  public void depth0() {
    IRetrieveResponse response = getBulkLowlevel().retrieve(Collections.emptyList(), "0");
    assertFalse(response.isOk());
    assertFalse(response.isValidNodeIds());
  }
  @Test
  public void depth1() {
    IRetrieveResponse response = getBulkLowlevel().retrieve(Collections.emptyList(), "1");
    assertFalse(response.isOk());
    assertFalse(response.isValidNodeIds());
  }
  @Test
  public void depth2() {
    IRetrieveResponse response = getBulkLowlevel().retrieve(Collections.emptyList(), "2");
    assertFalse(response.isOk());
    assertFalse(response.isValidNodeIds());
  }
}
