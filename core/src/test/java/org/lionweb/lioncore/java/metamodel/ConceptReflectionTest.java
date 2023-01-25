package org.lionweb.lioncore.java.metamodel;

import org.junit.Test;
import org.lionweb.lioncore.java.self.LionCore;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class ConceptReflectionTest {

    @Test
    public void getPropertyValueSimpleName() {
        Metamodel metamodel = new Metamodel();
        Concept concept = new Concept(metamodel, "MyConcept");
        assertEquals("MyConcept", concept.getPropertyValue(LionCore.getConcept().getPropertyByName("simpleName")));
    }

    @Test
    public void setPropertyValueSimpleName() {
        Metamodel metamodel = new Metamodel();
        Concept concept = new Concept(metamodel, "MyConcept");
        concept.setPropertyValue(LionCore.getConcept().getPropertyByName("simpleName"), "MyAmazingConcept");
        assertEquals("MyAmazingConcept", concept.getSimpleName());
    }

    @Test
    public void getPropertyValueAbstract() {
        Metamodel metamodel = new Metamodel();
        Concept concept = new Concept(metamodel, "MyConcept");
        Property property = LionCore.getConcept().getPropertyByName("abstract");
        concept.setAbstract(true);
        assertEquals(true, concept.getPropertyValue(property));
        concept.setAbstract(false);
        assertEquals(false, concept.getPropertyValue(property));
    }

    @Test
    public void setPropertyValueAbstract() {
        Metamodel metamodel = new Metamodel();
        Concept concept = new Concept(metamodel, "MyConcept");
        Property property = LionCore.getConcept().getPropertyByName("abstract");
        concept.setPropertyValue(property, true);
        assertEquals(true, concept.isAbstract());
        concept.setPropertyValue(property, false);
        assertEquals(false, concept.isAbstract());
    }

    @Test
    public void getReferenceExtended() {
        Metamodel metamodel = new Metamodel();
        Concept concept = new Concept(metamodel, "MyConcept");
        Concept otherConcept = new Concept(metamodel, "OtherConcept");
        Reference reference = LionCore.getConcept().getReferenceByName("extended");
        concept.setExtendedConcept(null);
        assertEquals(Collections.emptyList(), concept.getReferredNodes(reference));
        concept.setExtendedConcept(otherConcept);
        assertEquals(Arrays.asList(otherConcept), concept.getReferredNodes(reference));
    }

    @Test
    public void setReferenceExtended() {
        Metamodel metamodel = new Metamodel();
        Concept concept = new Concept(metamodel, "MyConcept");
        Concept otherConcept = new Concept(metamodel, "OtherConcept");
        Reference reference = LionCore.getConcept().getReferenceByName("extended");
        concept.addReferredNode(reference, null);
        assertEquals(null, concept.getExtendedConcept());
        concept.addReferredNode(reference, otherConcept);
        assertEquals(otherConcept, concept.getExtendedConcept());
    }
}
