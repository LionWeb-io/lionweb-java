package io.lionweb.serialization;

import io.lionweb.language.*;
import io.lionweb.model.ClassifierInstanceUtils;
import io.lionweb.model.StructuredDataTypeInstance;
import io.lionweb.model.impl.DynamicNode;

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
          .addField(new Field("x", LionCoreBuiltins.getInteger()).setID("x-id").setKey("x-key"))
          .addField(new Field("y", LionCoreBuiltins.getInteger()).setID("y-id").setKey("y-key"));
  public static final StructuredDataType ADDRESS =
      new StructuredDataType()
          .setID("address-id")
          .setKey("address-key")
          .setName("address")
          .setParent(LANGUAGE)
          .addField(
              new Field("street", LionCoreBuiltins.getString())
                  .setID("street-id")
                  .setKey("street-key"))
          .addField(
              new Field("city", LionCoreBuiltins.getString()).setID("city-id").setKey("city-key"));

  static {
    CONCEPT.addFeature(
        Property.createRequired("point", POINT).setKey("my-point").setID("my-point-id"));
    CONCEPT.addFeature(
        Property.createOptional("address", ADDRESS).setKey("my-address").setID("my-address-id"));
    LANGUAGE.addElement(CONCEPT);
    LANGUAGE.addElement(POINT);
    LANGUAGE.addElement(ADDRESS);
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

  public StructuredDataTypeInstance getAddress() {
    return (StructuredDataTypeInstance)
        ClassifierInstanceUtils.getPropertyValueByName(this, "address");
  }

  public void setAddress(StructuredDataTypeInstance address) {
    ClassifierInstanceUtils.setPropertyValueByName(this, "address", address);
  }
}
