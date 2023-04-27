package org.lionweb.lioncore.java.utils;

import static org.junit.Assert.assertEquals;
import static org.lionweb.lioncore.java.utils.CommonChecks.isValidID;

import org.junit.Test;

public class IDsCheckTest {

  @Test
  public void positiveCase() {
    assertEquals(true, isValidID("foo"));
  }

  @Test
  public void emptyIDIsInvalid() {
    assertEquals(false, isValidID(""));
  }

  @Test
  public void idsWithUmlautsAreInvalid() {
    assertEquals(false, isValidID("foö"));
  }

  @Test
  public void idsWithAccentsAreInvalid() {
    assertEquals(false, isValidID("foò"));
    assertEquals(false, isValidID("foó"));
  }
}
