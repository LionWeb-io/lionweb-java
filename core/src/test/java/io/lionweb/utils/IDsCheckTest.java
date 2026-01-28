package io.lionweb.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class IDsCheckTest {

  @Test
  public void positiveCase() {
    assertTrue(CommonChecks.isValidID("foo"));
  }

  @Test
  public void emptyIDIsInvalid() {
    assertFalse(CommonChecks.isValidID(""));
  }

  @Test
  public void idsWithUmlautsAreInvalid() {
    assertFalse(CommonChecks.isValidID("foö"));
  }

  @Test
  public void idsWithAccentsAreInvalid() {
    assertFalse(CommonChecks.isValidID("foò"));
    assertFalse(CommonChecks.isValidID("foó"));
  }
}
