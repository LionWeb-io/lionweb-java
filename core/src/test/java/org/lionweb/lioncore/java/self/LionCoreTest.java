package org.lionweb.lioncore.java.self;

import org.junit.Test;
import org.lionweb.lioncore.java.utils.MetamodelValidator;
import org.lionweb.lioncore.java.utils.ValidationResult;

public class LionCoreTest {

  @Test
  public void lionCoreIsValid() {
    ValidationResult vr = new MetamodelValidator().validate(LionCore.getInstance());
    if (!vr.isSuccessful()) {
      throw new RuntimeException("LionCore Metamodel is not valid: " + vr);
    }
  }
}
