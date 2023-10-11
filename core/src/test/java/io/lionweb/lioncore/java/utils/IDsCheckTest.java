package io.lionweb.lioncore.java.utils;

import org.junit.Assert;
import org.junit.Test;

public class IDsCheckTest {

  @Test
  public void positiveCase() {
    Assert.assertTrue(CommonChecks.isValidID("foo"));
  }

  @Test
  public void emptyIDIsInvalid() {
    Assert.assertFalse(CommonChecks.isValidID(""));
  }

  @Test
  public void idsWithUmlautsAreInvalid() {
    Assert.assertFalse(CommonChecks.isValidID("foö"));
  }

  @Test
  public void idsWithAccentsAreInvalid() {
    Assert.assertFalse(CommonChecks.isValidID("foò"));
    Assert.assertFalse(CommonChecks.isValidID("foó"));
  }
}
