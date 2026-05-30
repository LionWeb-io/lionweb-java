package io.lionweb.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class IDsCheckTest {

  @Test
  public void positiveCase() {
    assertTrue(IdUtils.isValidID("foo"));
  }

  @Test
  public void emptyIDIsInvalid() {
    assertFalse(IdUtils.isValidID(""));
  }

  @Test
  public void idsWithUmlautsAreInvalid() {
    assertFalse(IdUtils.isValidID("foö"));
  }

  @Test
  public void idsWithAccentsAreInvalid() {
    assertFalse(IdUtils.isValidID("foò"));
    assertFalse(IdUtils.isValidID("foó"));
  }

  @Test
  public void nullIDIsInvalid() {
    assertFalse(IdUtils.isValidID(null));
  }

  @Test
  public void singleLetterIsValid() {
    assertTrue(IdUtils.isValidID("a"));
    assertTrue(IdUtils.isValidID("Z"));
  }

  @Test
  public void singleDigitIsValid() {
    assertTrue(IdUtils.isValidID("0"));
    assertTrue(IdUtils.isValidID("9"));
  }

  @Test
  public void dashIsValidInID() {
    assertTrue(IdUtils.isValidID("foo-bar"));
    assertTrue(IdUtils.isValidID("-"));
  }

  @Test
  public void underscoreIsValidInID() {
    assertTrue(IdUtils.isValidID("foo_bar"));
    assertTrue(IdUtils.isValidID("_"));
  }

  @Test
  public void mixedValidCharsAreValid() {
    assertTrue(IdUtils.isValidID("abc-123_XYZ"));
  }

  @Test
  public void spaceIsInvalid() {
    assertFalse(IdUtils.isValidID("foo bar"));
    assertFalse(IdUtils.isValidID(" "));
  }

  @Test
  public void dotIsInvalid() {
    assertFalse(IdUtils.isValidID("foo.bar"));
  }
}
