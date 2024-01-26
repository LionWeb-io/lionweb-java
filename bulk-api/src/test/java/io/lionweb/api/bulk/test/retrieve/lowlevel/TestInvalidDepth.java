package io.lionweb.api.bulk.test.retrieve.lowlevel;

import io.lionweb.api.bulk.lowlevel.IRetrieveResponse;
import io.lionweb.api.bulk.test.retrieve.ATestRetrieve;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertFalse;

public class TestInvalidDepth extends ATestRetrieve {
  @Test
  public void nothing() {
    IRetrieveResponse response = getBulkLowlevel().retrieve(Arrays.asList("leaf-id"), "");
    assertFalse(response.isOk());
    assertFalse(response.isValidDepthLimit());
  }
  @Test
  public void negative() {
    IRetrieveResponse response = getBulkLowlevel().retrieve(Arrays.asList("leaf-id"), "-1");
    assertFalse(response.isOk());
    assertFalse(response.isValidDepthLimit());
  }
  @Test
  public void character() {
    IRetrieveResponse response = getBulkLowlevel().retrieve(Arrays.asList("leaf-id"), "a");
    assertFalse(response.isOk());
    assertFalse(response.isValidDepthLimit());
  }
  @Test
  public void string() {
    IRetrieveResponse response = getBulkLowlevel().retrieve(Arrays.asList("leaf-id"), "asdf");
    assertFalse(response.isOk());
    assertFalse(response.isValidDepthLimit());
  }
}
