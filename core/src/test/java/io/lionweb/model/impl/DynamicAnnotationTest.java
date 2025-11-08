package io.lionweb.model.impl;

import static org.junit.Assert.*;

import io.lionweb.language.*;
import io.lionweb.language.assigners.CommonIDAssigners;
import io.lionweb.language.assigners.CommonKeyAssigners;
import io.lionweb.model.ClassifierInstanceUtils;
import io.lionweb.model.Node;
import io.lionweb.serialization.*;
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

  @Test
  public void annotationDescendants() {
    Language language = new Language("ALanguage");
    Concept c1 = new Concept(language, "C1", "my-id1");
    Concept c2 = new Concept(language, "C2", "my-id2");
    Concept c3 = new Concept(language, "C3", "my-id3");
    c2.addContainment("mySubC2", c2, Multiplicity.OPTIONAL);
    Annotation a1 =
        new Annotation(language, "A1", "my-id4")
            .setAnnotates(c1)
            .addContainment("myC2", c2, Multiplicity.ZERO_OR_MORE)
            .addContainment("myC3", c3, Multiplicity.OPTIONAL);
    CommonKeyAssigners.qualifiedKeyAssigner.assignKeys(language);
    CommonIDAssigners.qualifiedIDAssigner.assignIDs(language);

    DynamicNode n1 = new DynamicNode("n1", c1);
    DynamicAnnotationInstance a1_1 = new DynamicAnnotationInstance("a1_1", a1);
    a1_1.setAnnotated(n1);
    DynamicNode n2 = new DynamicNode("n2", c2);
    DynamicNode n3 = new DynamicNode("n3", c2);
    ClassifierInstanceUtils.addChild(a1_1, "myC2", n2);
    ClassifierInstanceUtils.addChild(a1_1, "myC2", n3);
    DynamicNode n4 = new DynamicNode("n4", c2);
    ClassifierInstanceUtils.addChild(n3, "mySubC2", n4);

    DynamicNode n5 = new DynamicNode("n5", c3);
    ClassifierInstanceUtils.addChild(a1_1, "myC3", n5);

    assertEquals(n3, n4.getParent());
    assertEquals(a1_1, n4.getParent().getParent());
    assertEquals(n1, n4.getParent().getParent().getParent());

    assertEquals(a1_1, n3.getParent());
    assertEquals(n1, n3.getParent().getParent());

    assertEquals(a1_1, n2.getParent());
    assertEquals(n1, n2.getParent().getParent());

    assertEquals(a1_1, n5.getParent());
    assertEquals(n1, n5.getParent().getParent());

    assertEquals(n1, a1_1.getParent());

    assertNull(n1.getParent());
  }
}
