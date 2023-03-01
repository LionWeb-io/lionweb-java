package org.lionweb.lioncore.java.metamodel;

import org.junit.Test;
import org.lionweb.lioncore.java.metamodel.LionCoreBuiltins;
import org.lionweb.lioncore.java.metamodel.PrimitiveType;

import static org.junit.Assert.assertEquals;

public class LionCoreBuiltinsTest {

    @Test
    public void stringPrimitiveType() {
        PrimitiveType string = (PrimitiveType) LionCoreBuiltins.getInstance().getElementByName("String");
        assertEquals("String", string.getSimpleName());
        assertEquals("org.lionweb.Builtins.String", string.qualifiedName());
    }

    @Test
    public void primitiveTypesHaveAgreedIDs() {
        assertEquals("LIonCore_M3_String", LionCoreBuiltins.getString().getID());
        assertEquals("LIonCore_M3_Boolean", LionCoreBuiltins.getBoolean().getID());
        assertEquals("LIonCore_M3_Integer", LionCoreBuiltins.getInteger().getID());
        assertEquals("LIonCore_M3_JSON", LionCoreBuiltins.getJSON().getID());
    }
}
