package io.lionweb.lioncore.java.utils;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class IdUtilsTest {

  @Test
  public void testCleanString() {
    assertEquals("", IdUtils.cleanString(""));
    assertEquals("a", IdUtils.cleanString("a"));
    assertEquals("a-b", IdUtils.cleanString("a@b"));
    assertEquals("---FF-", IdUtils.cleanString("(@%FF?"));
    assertEquals("123_456", IdUtils.cleanString("123_456"));
    assertEquals("123-456", IdUtils.cleanString("123-456"));
  }

}
