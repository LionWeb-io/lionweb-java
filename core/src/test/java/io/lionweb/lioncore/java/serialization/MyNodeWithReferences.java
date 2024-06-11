package io.lionweb.lioncore.java.serialization;

import io.lionweb.lioncore.java.language.*;
import io.lionweb.lioncore.java.model.ClassifierInstanceUtils;
import io.lionweb.lioncore.java.model.ReferenceValue;
import io.lionweb.lioncore.java.model.impl.DynamicNode;
import java.util.List;

public class MyNodeWithReferences extends DynamicNode {
  public static final Language LANGUAGE =
      new Language().setID("mm2").setKey("mylanguage2").setName("MM2").setVersion("1");
  public static final Concept CONCEPT =
      new Concept()
          .setID("concept-MyNodeWithReferences")
          .setKey("concept-MyNodeWithReferences")
          .setName("MyNodeWithReferences")
          .addFeature(
              Reference.createOptional("r1", MyNodeWithProperties.CONCEPT).setID("r1").setKey("r1"))
          .addFeature(
              Reference.createMultiple("r2", MyNodeWithProperties.CONCEPT).setID("r2").setKey("r2"))
          .setParent(LANGUAGE);

  static {
    LANGUAGE.addElement(CONCEPT);
  }

  public MyNodeWithReferences(String id) {
    super(id, CONCEPT);
  }

  public ReferenceValue getR1() {
    return ClassifierInstanceUtils.getOnlyReferenceValueByReferenceName(this, "r1");
  }

  public List<ReferenceValue> getR2() {
    return ClassifierInstanceUtils.getReferenceValueByName(this, "r2");
  }

  public void setP1(ReferenceValue value) {
    ClassifierInstanceUtils.setOnlyReferenceValueByName(this, "r1", value);
  }

  public void setR2(List<ReferenceValue> values) {
    ClassifierInstanceUtils.setReferenceValuesByName(this, "r1", values);
  }
}
