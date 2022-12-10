package org.lionweb.lioncore.java.metamodel;

import org.junit.Test;
import org.lionweb.lioncore.java.metamodel.Concept;
import org.lionweb.lioncore.java.metamodel.ConceptInterface;
import org.lionweb.lioncore.java.metamodel.Metamodel;

import static org.junit.Assert.*;

public class SimpleLanguageMetamodelTest {

    @Test public void emptyMetamodelDefinition() {
        Metamodel metamodel = new Metamodel("SimpleLanguage");
        assertEquals("SimpleLanguage", metamodel.getQualifiedName());
        assertEquals("SimpleLanguage", metamodel.namespaceQualifier());
        assertEquals(0, metamodel.dependsOn().size());
        assertEquals(0, metamodel.getElements().size());
    }

    @Test public void emptyConceptDefinition() {
        Metamodel metamodel = new Metamodel("SimpleLanguage");
        Concept expression = new Concept(metamodel, "Expression");
        assertEquals("Expression", expression.getSimpleName());
        assertSame(metamodel, expression.getContainer());
        assertSame(metamodel, expression.getMetamodel());
        assertEquals("SimpleLanguage.Expression", expression.qualifiedName());
        assertEquals("SimpleLanguage.Expression", expression.namespaceQualifier());
        assertNull(expression.getExtendedConcept());
        assertEquals(0, expression.getImplemented().size());
        assertEquals(0, expression.getFeatures().size());
        assertFalse(expression.isAbstract());
    }

    @Test public void emptyConceptInterfaceDefinition() {
        Metamodel metamodel = new Metamodel("SimpleLanguage");
        ConceptInterface deprecated = new ConceptInterface(metamodel, "Deprecated");
        assertEquals("Deprecated", deprecated.getSimpleName());
        assertSame(metamodel, deprecated.getContainer());
        assertSame(metamodel, deprecated.getMetamodel());
        assertEquals("SimpleLanguage.Deprecated", deprecated.qualifiedName());
        assertEquals("SimpleLanguage.Deprecated", deprecated.namespaceQualifier());
        assertEquals(0, deprecated.getExtendedInterfaces().size());
        assertEquals(0, deprecated.getFeatures().size());
    }
}
