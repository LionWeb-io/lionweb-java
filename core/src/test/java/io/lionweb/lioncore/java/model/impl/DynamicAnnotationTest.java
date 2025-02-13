package io.lionweb.lioncore.java.model.impl;

import static org.junit.Assert.*;

import io.lionweb.lioncore.java.model.ClassifierInstanceUtils;
import io.lionweb.lioncore.java.model.Node;
import io.lionweb.lioncore.java.serialization.*;
import org.junit.Test;

public class DynamicAnnotationTest {

  @Test
  public void annotationWithChildren() {
    MyAnnotation myAnnotation = new MyAnnotation("ann1");
    DynamicNode value1 = new DynamicNode("value1", MyAnnotation.VALUE);
    ClassifierInstanceUtils.setPropertyValueByName(value1, "amount", 123);
    ClassifierInstanceUtils.addChild(myAnnotation, "values", value1);
    assertEquals(
        1, ClassifierInstanceUtils.getChildrenByContainmentName(myAnnotation, "values").size());
    Node retrievedValue1 =
        ClassifierInstanceUtils.getChildrenByContainmentName(myAnnotation, "values").get(0);
    assertEquals(123, ClassifierInstanceUtils.getPropertyValueByName(retrievedValue1, "amount"));
  }
}
