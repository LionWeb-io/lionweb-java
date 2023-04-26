package org.lionweb.lioncore.java.metamodel;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ConceptTest {

    @Test
    public void aConceptIsNonAbstractByDefault() {
        Concept  c = new Concept();
        assertEquals(false, c.isAbstract());
    }
}
