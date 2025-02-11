package io.lionweb.lioncore.java.language;

import static org.junit.Assert.*;

import io.lionweb.lioncore.java.LionWebVersion;
import io.lionweb.lioncore.java.utils.LanguageValidator;
import io.lionweb.lioncore.java.utils.ValidationResult;
import org.junit.Test;

public class LionCoreBuiltinsTest {

  @Test
  public void stringPrimitiveType() {
    PrimitiveType string =
        (PrimitiveType) LionCoreBuiltins.getInstance().getElementByName("String");
    assertEquals("String", string.getName());
    assertEquals("LionCore_builtins.String", string.qualifiedName());
  }

  @Test
  public void primitiveTypesHaveAgreedIDsv2023() {
    assertEquals(
        "LionCore-builtins-String", LionCoreBuiltins.getString(LionWebVersion.v2023_1).getID());
    assertEquals(
        "LionCore-builtins-Boolean", LionCoreBuiltins.getBoolean(LionWebVersion.v2023_1).getID());
    assertEquals(
        "LionCore-builtins-Integer", LionCoreBuiltins.getInteger(LionWebVersion.v2023_1).getID());
    assertEquals(
        "LionCore-builtins-JSON", LionCoreBuiltins.getJSON(LionWebVersion.v2023_1).getID());
  }

  @Test
  public void primitiveTypesHaveAgreedIDsv2024() {
    assertEquals(
        "LionCore-builtins-String-2024-1",
        LionCoreBuiltins.getString(LionWebVersion.v2024_1).getID());
    assertEquals(
        "LionCore-builtins-Boolean-2024-1",
        LionCoreBuiltins.getBoolean(LionWebVersion.v2024_1).getID());
    assertEquals(
        "LionCore-builtins-Integer-2024-1",
        LionCoreBuiltins.getInteger(LionWebVersion.v2024_1).getID());
    assertThrows(
        IllegalArgumentException.class, () -> LionCoreBuiltins.getJSON(LionWebVersion.v2024_1));
  }

  @Test
  public void lionCoreBuiltinsIsValid() {
    ValidationResult vr = new LanguageValidator().validate(LionCoreBuiltins.getInstance());
    if (!vr.isSuccessful()) {
      throw new RuntimeException("LionCoreBuiltins Language is not valid: " + vr);
    }
  }
}
