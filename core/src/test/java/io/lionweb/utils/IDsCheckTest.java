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

  @Test
  public void nullIDIsInvalid() {
    assertFalse(CommonChecks.isValidID(null));
  }

  @Test
  public void singleLetterIsValid() {
    assertTrue(CommonChecks.isValidID("a"));
    assertTrue(CommonChecks.isValidID("Z"));
  }

  @Test
  public void singleDigitIsValid() {
    assertTrue(CommonChecks.isValidID("0"));
    assertTrue(CommonChecks.isValidID("9"));
  }

  @Test
  public void dashIsValidInID() {
    assertTrue(CommonChecks.isValidID("foo-bar"));
    assertTrue(CommonChecks.isValidID("-"));
  }

  @Test
  public void underscoreIsValidInID() {
    assertTrue(CommonChecks.isValidID("foo_bar"));
    assertTrue(CommonChecks.isValidID("_"));
  }

  @Test
  public void mixedValidCharsAreValid() {
    assertTrue(CommonChecks.isValidID("abc-123_XYZ"));
  }

  @Test
  public void spaceIsInvalid() {
    assertFalse(CommonChecks.isValidID("foo bar"));
    assertFalse(CommonChecks.isValidID(" "));
  }

  @Test
  public void dotIsInvalid() {
    assertFalse(CommonChecks.isValidID("foo.bar"));
  }
}
