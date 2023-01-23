package org.lionweb.lioncore.java.metamodel;

import org.junit.Test;
import org.lionweb.lioncore.java.self.LionCore;

import static org.junit.Assert.assertEquals;

public class BuiltinIDsTest {

    @Test
    public void M3ElementsHasExpectedIDs() {
        assertEquals("LIonCore_M3_Metamodel", LionCore.getMetamodel().getID());
        assertEquals("LIonCore_M3_Concept", LionCore.getConcept().getID());
        assertEquals("LIonCore_M3_ConceptInterface", LionCore.getConceptInterface().getID());
        assertEquals("LIonCore_M3_Property", LionCore.getProperty().getID());
    }
}
