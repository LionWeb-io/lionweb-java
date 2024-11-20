package io.lionweb.lioncore.java.serialization;

import com.google.gson.JsonElement;
import io.lionweb.lioncore.java.versions.LionWebVersion;
import io.lionweb.lioncore.java.language.Concept;
import io.lionweb.lioncore.java.language.Language;
import io.lionweb.lioncore.java.language.LionCoreBuiltins;
import io.lionweb.lioncore.java.language.Property;
import io.lionweb.lioncore.java.model.ClassifierInstanceUtils;
import io.lionweb.lioncore.java.model.impl.DynamicNode;

public class MyNodeWithProperties2023 extends DynamicNode {
  public static final Language LANGUAGE =
      new Language(LionWebVersion.v2023_1)
          .setID("mm1")
          .setKey("mylanguage")
          .setName("MM1")
          .setVersion("1");
  public static final Concept CONCEPT =
      new Concept(LionWebVersion.v2023_1)
          .setID("concept-MyNodeWithProperties")
          .setKey("concept-MyNodeWithProperties")
          .setName("MyNodeWithProperties")
          .addFeature(
              Property.createOptional(
                      LionWebVersion.v2023_1,
                      "p1",
                      LionCoreBuiltins.getBoolean(LionWebVersion.v2023_1))
                  .setID("p1")
                  .setKey("p1"))
          .addFeature(
              Property.createOptional(
                      LionWebVersion.v2023_1,
                      "p2",
                      LionCoreBuiltins.getInteger(LionWebVersion.v2023_1))
                  .setID("p2")
                  .setKey("p2"))
          .addFeature(
              Property.createOptional(
                      LionWebVersion.v2023_1,
                      "p3",
                      LionCoreBuiltins.getString(LionWebVersion.v2023_1))
                  .setID("p3")
                  .setKey("p3"))
          .addFeature(
              Property.createOptional(
                      LionWebVersion.v2023_1,
                      "p4",
                      LionCoreBuiltins.getJSON(LionWebVersion.v2023_1))
                  .setID("p4")
                  .setKey("p4"))
          .setParent(LANGUAGE);

  static {
    LANGUAGE.addElement(CONCEPT);
  }

  public MyNodeWithProperties2023(String id) {
    super(id, CONCEPT);
  }

  public Boolean getP1() {
    return (Boolean) ClassifierInstanceUtils.getPropertyValueByName(this, "p1");
  }

  public int getP2() {
    return (int) ClassifierInstanceUtils.getPropertyValueByName(this, "p2");
  }

  public String getP3() {
    return (String) ClassifierInstanceUtils.getPropertyValueByName(this, "p3");
  }

  public JsonElement getP4() {
    return (JsonElement) ClassifierInstanceUtils.getPropertyValueByName(this, "p4");
  }

  public void setP1(boolean value) {
    ClassifierInstanceUtils.setPropertyValueByName(this, "p1", value);
  }

  public void setP2(int value) {
    ClassifierInstanceUtils.setPropertyValueByName(this, "p2", value);
  }

  public void setP3(String value) {
    ClassifierInstanceUtils.setPropertyValueByName(this, "p3", value);
  }

  public void setP4(JsonElement value) {
    ClassifierInstanceUtils.setPropertyValueByName(this, "p4", value);
  }
}
