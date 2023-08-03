package io.lionweb.lioncore.java.self;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import io.lionweb.lioncore.java.language.Concept;
import io.lionweb.lioncore.java.language.Property;
import io.lionweb.lioncore.java.utils.LanguageValidator;
import io.lionweb.lioncore.java.utils.ValidationResult;
import org.junit.Test;

public class LionCoreTest {

  @Test
  public void lionCoreIsValid() {
    ValidationResult vr = new LanguageValidator().validate(LionCore.getInstance());
    if (!vr.isSuccessful()) {
      throw new RuntimeException("LionCore Language is not valid: " + vr);
    }
  }

  @Test
  public void checkProperty() {
    Concept property = LionCore.getProperty();
    Property name = property.getPropertyByName("name");
    assertNotNull(name);
    assertEquals("LIonCore-builtins-INamed-name", name.getKey());
    assertEquals("LIonCore-builtins", name.getDeclaringLanguage().getKey());
    Property key = property.getPropertyByName("key");
    assertNotNull(key);
  }

  @Test
  public void checkLanguage() {
    Concept language = LionCore.getLanguage();
    Property name = language.getPropertyByName("name");
    assertNotNull(name);
    assertEquals("LIonCore-builtins-INamed-name", name.getKey());
    assertEquals("LIonCore-builtins", name.getDeclaringLanguage().getKey());
    Property version = language.getPropertyByName("version");
    assertNotNull(version);
  }
}
