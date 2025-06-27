package io.lionweb.serialization;

import io.lionweb.language.*;
import io.lionweb.model.ClassifierInstanceUtils;
import io.lionweb.model.impl.DynamicNode;

public class MyNodeWithSelfContainment extends DynamicNode {
  public static final Language LANGUAGE =
      new Language().setID("mm3").setKey("mylanguage3").setName("MM3").setVersion("1");
  public static final Concept CONCEPT =
      new Concept()
          .setID("concept-MyNodeWithSelfContainment")
          .setKey("concept-MyNodeWithSelfContainment")
          .setName("MyNodeWithSelfContainment")
          .setParent(LANGUAGE);

  static {
    CONCEPT.addFeature(
        Containment.createOptional("another", CONCEPT).setID("another-id").setKey("another-key"));
    LANGUAGE.addElement(CONCEPT);
  }

  public MyNodeWithSelfContainment(String id) {
    super(id, CONCEPT);
  }

  public MyNodeWithSelfContainment getAnother() {
    return (MyNodeWithSelfContainment)
        ClassifierInstanceUtils.getOnlyChildByContainmentName(this, "another");
  }

  public void setAnother(MyNodeWithSelfContainment another) {
    ClassifierInstanceUtils.setOnlyChildByContainmentName(this, "another", another);
  }
}
