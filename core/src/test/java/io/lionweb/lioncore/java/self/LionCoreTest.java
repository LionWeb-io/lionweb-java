package io.lionweb.lioncore.java.self;

import io.lionweb.lioncore.java.utils.MetamodelValidator;
import io.lionweb.lioncore.java.utils.ValidationResult;
import org.junit.Test;

public class LionCoreTest {

  @Test
  public void lionCoreIsValid() {
    ValidationResult vr = new MetamodelValidator().validate(LionCore.getInstance());
    if (!vr.isSuccessful()) {
      throw new RuntimeException("LionCore Metamodel is not valid: " + vr);
    }
  }
}
