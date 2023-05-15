package io.lionweb.lioncore.java.self;

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
}
