package io.lionweb.lioncore.java.utils;

import org.junit.Assert;
import org.junit.Test;

public class IDsCheckTest {

  @Test
  public void positiveCase() {
    Assert.assertEquals(true, CommonChecks.isValidID("foo"));
  }

  @Test
  public void emptyIDIsInvalid() {
    Assert.assertEquals(false, CommonChecks.isValidID(""));
  }

  @Test
  public void idsWithUmlautsAreInvalid() {
    Assert.assertEquals(false, CommonChecks.isValidID("foö"));
  }

  @Test
  public void idsWithAccentsAreInvalid() {
    Assert.assertEquals(false, CommonChecks.isValidID("foò"));
    Assert.assertEquals(false, CommonChecks.isValidID("foó"));
  }
}
