package io.lionweb.lioncore.java.language;

import static org.junit.Assert.assertEquals;

import io.lionweb.lioncore.java.utils.LanguageValidator;
import io.lionweb.lioncore.java.utils.ValidationResult;
import org.junit.Test;

public class LionCoreBuiltinsTest {

  @Test
  public void stringPrimitiveType() {
    PrimitiveType string =
        (PrimitiveType) LionCoreBuiltins.getInstance().getElementByName("String");
    assertEquals("String", string.getName());
    assertEquals("LionCore.builtins.String", string.qualifiedName());
  }

  @Test
  public void primitiveTypesHaveAgreedIDs() {
    assertEquals("LionCore-builtins-String", LionCoreBuiltins.getString().getID());
    assertEquals("LionCore-builtins-Boolean", LionCoreBuiltins.getBoolean().getID());
    assertEquals("LionCore-builtins-Integer", LionCoreBuiltins.getInteger().getID());
    assertEquals("LionCore-builtins-JSON", LionCoreBuiltins.getJSON().getID());
  }

  @Test
  public void lionCoreBuiltinsIsValid() {
    ValidationResult vr = new LanguageValidator().validate(LionCoreBuiltins.getInstance());
    if (!vr.isSuccessful()) {
      throw new RuntimeException("LionCoreBuiltins Language is not valid: " + vr);
    }
  }
}
