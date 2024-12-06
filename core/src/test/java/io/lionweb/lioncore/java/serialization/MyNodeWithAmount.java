package io.lionweb.lioncore.java.serialization;

import io.lionweb.lioncore.java.language.*;
import io.lionweb.lioncore.java.model.ClassifierInstanceUtils;
import io.lionweb.lioncore.java.model.StructuredDataTypeInstance;
import io.lionweb.lioncore.java.model.impl.DynamicNode;

public class MyNodeWithAmount extends DynamicNode {
  public static final Language LANGUAGE =
      new Language()
          .setID("mm3withCurrency")
          .setKey("mylanguageWithCurrency")
          .setName("MMSDT")
          .setVersion("1");
  public static final Concept CONCEPT =
      new Concept()
          .setID("concept-MyNodeCurrency")
          .setKey("concept-MyNodeCurrency")
          .setName("MyNodeCurrency")
          .setParent(LANGUAGE);
  public static final StructuredDataType DECIMAL =
      new StructuredDataType()
          .setID("decimal-id")
          .setKey("decimal-key")
          .setName("decimal")
          .setParent(LANGUAGE)
          .addField(
              new Field("int", LionCoreBuiltins.getInteger()).setID("int-id").setKey("int-key"))
          .addField(
              new Field("frac", LionCoreBuiltins.getInteger()).setID("frac-id").setKey("frac-key"));
  public static final Enumeration CURRENCY =
      new Enumeration(LANGUAGE, "Currency")
          .setID("currency-id")
          .setKey("currency-key")
          .addLiteral(new EnumerationLiteral("EURO").setKey("euro"))
          .addLiteral(new EnumerationLiteral("YEN").setKey("yen"));
  public static final StructuredDataType AMOUNT =
      new StructuredDataType()
          .setID("amount-id")
          .setKey("amount-key")
          .setName("amount")
          .setParent(LANGUAGE)
          .addField(new Field("value", DECIMAL).setID("value-id").setKey("value-key"))
          .addField(new Field("currency", CURRENCY).setID("currency-id").setKey("currency-key"))
          .addField(
              new Field("digital", LionCoreBuiltins.getBoolean())
                  .setID("digital-id")
                  .setKey("digital-key"));

  static {
    CONCEPT.addFeature(
        Property.createRequired("amount", AMOUNT).setKey("my-amount").setID("my-amount"));
    LANGUAGE.addElement(CONCEPT);
    LANGUAGE.addElement(DECIMAL);
    LANGUAGE.addElement(CURRENCY);
    LANGUAGE.addElement(AMOUNT);
  }

  public MyNodeWithAmount(String id) {
    super(id, CONCEPT);
  }

  public StructuredDataTypeInstance getAmount() {
    return (StructuredDataTypeInstance)
        ClassifierInstanceUtils.getPropertyValueByName(this, "amount");
  }

  public void setAmount(StructuredDataTypeInstance amount) {
    ClassifierInstanceUtils.setPropertyValueByName(this, "amount", amount);
  }
}
