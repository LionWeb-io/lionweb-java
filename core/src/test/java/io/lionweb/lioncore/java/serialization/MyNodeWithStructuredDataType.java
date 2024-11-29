package io.lionweb.lioncore.java.serialization;

import io.lionweb.lioncore.java.language.*;
import io.lionweb.lioncore.java.model.ClassifierInstanceUtils;
import io.lionweb.lioncore.java.model.StructuredDataTypeInstance;
import io.lionweb.lioncore.java.model.impl.DynamicNode;

public class MyNodeWithStructuredDataType extends DynamicNode {
  public static final Language LANGUAGE =
      new Language().setID("mm3").setKey("mylanguageWithSDT").setName("MMSDT").setVersion("1");
  public static final Concept CONCEPT =
      new Concept()
          .setID("concept-MyNodeWithStructuredDataType")
          .setKey("concept-MyNodeWithStructuredDataType")
          .setName("MyNodeWithStructuredDataType")
          .setParent(LANGUAGE);
  public static final StructuredDataType POINT =
      new StructuredDataType()
          .setID("point-id")
          .setKey("point-key")
          .setName("point")
          .setParent(LANGUAGE)
          .addField(new Field("x", LionCoreBuiltins.getInteger()))
          .addField(new Field("y", LionCoreBuiltins.getInteger()));
  public static final StructuredDataType ADDRESS =
      new StructuredDataType()
          .setID("point-id")
          .setKey("point-key")
          .setName("point")
          .setParent(LANGUAGE)
          .addField(new Field("street", LionCoreBuiltins.getString()))
          .addField(new Field("city", LionCoreBuiltins.getString()));;

  static {
    CONCEPT.addFeature(Property.createRequired("point", POINT));
    LANGUAGE.addElement(CONCEPT);
    LANGUAGE.addElement(POINT);
  }

  public MyNodeWithStructuredDataType(String id) {
    super(id, CONCEPT);
  }

  public StructuredDataTypeInstance getPoint() {
    return (StructuredDataTypeInstance)
        ClassifierInstanceUtils.getPropertyValueByName(this, "point");
  }

  public void setPoint(StructuredDataTypeInstance point) {
    ClassifierInstanceUtils.setPropertyValueByName(this, "point", point);
  }
}
