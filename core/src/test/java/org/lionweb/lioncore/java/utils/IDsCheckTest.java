package org.lionweb.lioncore.java.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class IDsCheckTest {

  @Test
  public void positiveCase() {
    assertEquals(true, MetamodelValidator.isValidID("foo"));
  }

  @Test
  public void emptyIDIsInvalid() {
    assertEquals(false, MetamodelValidator.isValidID(""));
  }

  @Test
  public void idsWithUmlautsAreInvalid() {
    assertEquals(false, MetamodelValidator.isValidID("foö"));
  }

  @Test
  public void idsWithAccentsAreInvalid() {
    assertEquals(false, MetamodelValidator.isValidID("foò"));
    assertEquals(false, MetamodelValidator.isValidID("foó"));
  }
}
