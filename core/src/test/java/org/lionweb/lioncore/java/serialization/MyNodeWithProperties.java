package org.lionweb.lioncore.java.serialization;

import com.google.gson.JsonElement;
import org.lionweb.lioncore.java.metamodel.Concept;
import org.lionweb.lioncore.java.metamodel.LionCoreBuiltins;
import org.lionweb.lioncore.java.metamodel.Metamodel;
import org.lionweb.lioncore.java.metamodel.Property;
import org.lionweb.lioncore.java.model.impl.DynamicNode;

public class MyNodeWithProperties extends DynamicNode  {
    public static Metamodel METAMODEL = new Metamodel().setID("mm1").setKey("mymetamodel").setName("MM1").setVersion("1");
    public static Concept CONCEPT = new Concept()
            .setID("concept-MyNodeWithProperties")
            .setKey("concept-MyNodeWithProperties")
            .setSimpleName("MyNodeWithProperties")
            .addFeature(Property.createOptional("p1", LionCoreBuiltins.getBoolean()).setID("p1").setKey("p1"))
            .addFeature(Property.createOptional("p2", LionCoreBuiltins.getInteger()).setID("p2").setKey("p2"))
            .addFeature(Property.createOptional("p3", LionCoreBuiltins.getString()).setID("p3").setKey("p3"))
            .addFeature(Property.createOptional("p4", LionCoreBuiltins.getJSON()).setID("p4").setKey("p4"))
            .setParent(METAMODEL);

    static {
        METAMODEL.addElement(CONCEPT);
    }
    public MyNodeWithProperties(String id) {
        super(id, CONCEPT);
    }

    public boolean getP1() {
        return (boolean) this.getPropertyValueByName("p1");
    }

    public int getP2() {
        return (int) this.getPropertyValueByName("p2");
    }

    public String getP3() {
        return (String) this.getPropertyValueByName("p3");
    }

    public JsonElement getP4() {
        return (JsonElement) this.getPropertyValueByName("p4");
    }

    public void setP1(boolean value) {
        this.setPropertyValueByName("p1", value);
    }

    public void setP2(int value) {
        this.setPropertyValueByName("p2", value);
    }

    public void setP3(String value) {
        this.setPropertyValueByName("p3", value);
    }

    public void setP4(JsonElement value) {
        this.setPropertyValueByName("p4", value);
    }


}
