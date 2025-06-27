package io.lionweb.lioncore;

import static org.junit.Assert.*;

import io.lionweb.language.Concept;
import io.lionweb.language.Containment;
import io.lionweb.language.Property;
import io.lionweb.utils.LanguageValidator;
import io.lionweb.utils.ValidationResult;
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
    assertEquals("LionCore-builtins-INamed-name", name.getKey());
    assertEquals("LionCore-builtins", name.getDeclaringLanguage().getKey());
    Property key = property.getPropertyByName("key");
    assertNotNull(key);
  }

  @Test
  public void checkLanguage() {
    Concept language = LionCore.getLanguage();
    Property name = language.getPropertyByName("name");
    assertNotNull(name);
    assertEquals("LionCore-builtins-INamed-name", name.getKey());
    assertEquals("LionCore-builtins", name.getDeclaringLanguage().getKey());
    Property version = language.getPropertyByName("version");
    assertNotNull(version);
  }

  @Test
  public void checkSDTFieldsAreRequired() {
    Concept sdt = LionCore.getStructuredDataType();
    Containment fields = sdt.getContainmentByName("fields");
    assertTrue(fields.isRequired());
    assertFalse(fields.isOptional());
  }
}
