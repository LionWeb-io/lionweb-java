package io.lionweb.model.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import io.lionweb.language.StructuredDataType;
import io.lionweb.model.StructuredDataTypeInstanceUtils;
import io.lionweb.serialization.MyNodeWithStructuredDataType;
import org.junit.jupiter.api.Test;

public class DynamicStructuredDataTypeInstanceTest {

  @Test
  public void checkEquality() {
    StructuredDataType point = MyNodeWithStructuredDataType.POINT;
    StructuredDataType address = MyNodeWithStructuredDataType.ADDRESS;
    DynamicStructuredDataTypeInstance sdt1 = new DynamicStructuredDataTypeInstance(point);
    DynamicStructuredDataTypeInstance sdt2 = new DynamicStructuredDataTypeInstance(point);
    DynamicStructuredDataTypeInstance sdt3 = new DynamicStructuredDataTypeInstance(address);

    // The structuredDataType should matter
    assertEquals(sdt1, sdt2);
    assertNotEquals(sdt1, sdt3);
    assertNotEquals(sdt2, sdt3);

    StructuredDataTypeInstanceUtils.setFieldValueByName(sdt1, "x", 10);
    assertNotEquals(sdt1, sdt2);

    StructuredDataTypeInstanceUtils.setFieldValueByName(sdt2, "x", 10);
    assertEquals(sdt1, sdt2);
  }
}
