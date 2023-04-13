package org.lionweb.lioncore.java.metamodel;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class LionCoreBuiltinsTest {

  @Test
  public void stringPrimitiveType() {
    PrimitiveType string =
        (PrimitiveType) LionCoreBuiltins.getInstance().getElementByName("String");
    assertEquals("String", string.getName());
    assertEquals("org.lionweb.Builtins.String", string.qualifiedName());
  }

  @Test
  public void primitiveTypesHaveAgreedIDs() {
    assertEquals("LIonCore_M3_String", LionCoreBuiltins.getString().getID());
    assertEquals("LIonCore_M3_Boolean", LionCoreBuiltins.getBoolean().getID());
    assertEquals("LIonCore_M3_Integer", LionCoreBuiltins.getInteger().getID());
    assertEquals("LIonCore_M3_JSON", LionCoreBuiltins.getJSON().getID());
  }
}
