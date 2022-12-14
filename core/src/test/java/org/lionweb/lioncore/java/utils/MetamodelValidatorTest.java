package org.lionweb.lioncore.java.utils;

import org.checkerframework.checker.units.qual.C;
import org.junit.Test;
import org.lionweb.lioncore.java.metamodel.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class MetamodelValidatorTest {

    @Test
    public void anEmptyAnnotationIsInvalid() {
        Metamodel metamodel = new Metamodel();
        Annotation annotation = new Annotation();
        metamodel.addElement(annotation);

        assertFalse(new MetamodelValidator().validateMetamodel(metamodel).isSuccessful());
        assertFalse(new MetamodelValidator().isMetamodelValid(metamodel));
        assertEquals(3, new MetamodelValidator().validateMetamodel(metamodel).getIssues().size());
        assertTrue(new MetamodelValidator().validateMetamodel(metamodel).getIssues().stream().allMatch(issue -> issue.isError()));
        assertEquals(new HashSet<>(Arrays.asList("Metamodel not set", "Simple name not set", "Qualified name not set")),
                new MetamodelValidator().validateMetamodel(metamodel).getIssues().stream().map(issue -> issue.getMessage()).collect(Collectors.toSet()));
    }

    @Test
    public void anAnnotationCanBeValid() {
        Metamodel metamodel = new Metamodel("MyMetamodel");
        Annotation annotation = new Annotation(metamodel, "MyAnnotation");
        metamodel.addElement(annotation);

        assertTrue(new MetamodelValidator().validateMetamodel(metamodel).isSuccessful());
        assertTrue(new MetamodelValidator().isMetamodelValid(metamodel));
        assertEquals(0, new MetamodelValidator().validateMetamodel(metamodel).getIssues().size());
    }

    @Test
    public void anEmptyPrimitiveTypeIsInvalid() {
        Metamodel metamodel = new Metamodel("MyMetamodel");
        PrimitiveType primitiveType = new PrimitiveType();
        metamodel.addElement(primitiveType);

        assertFalse(new MetamodelValidator().validateMetamodel(metamodel).isSuccessful());
        assertFalse(new MetamodelValidator().isMetamodelValid(metamodel));
        assertEquals(2, new MetamodelValidator().validateMetamodel(metamodel).getIssues().size());
        assertTrue(new MetamodelValidator().validateMetamodel(metamodel).getIssues().stream().allMatch(issue -> issue.isError()));
        assertEquals(new HashSet<>(Arrays.asList("Metamodel not set", "Simple name not set")),
                new MetamodelValidator().validateMetamodel(metamodel).getIssues().stream().map(issue -> issue.getMessage()).collect(Collectors.toSet()));
    }

    @Test
    public void aPrimitiveTypeCanBeValid() {
        Metamodel metamodel = new Metamodel("MyMetamodel");
        PrimitiveType primitiveType = new PrimitiveType(metamodel, "PrimitiveType");
        metamodel.addElement(primitiveType);

        assertTrue(new MetamodelValidator().validateMetamodel(metamodel).isSuccessful());
        assertTrue(new MetamodelValidator().isMetamodelValid(metamodel));
        assertEquals(0, new MetamodelValidator().validateMetamodel(metamodel).getIssues().size());
    }

    @Test
    public void simpleSelfInheritanceIsCaught() {
        Metamodel metamodel = new Metamodel("MyMetamodel");
        Concept a = new Concept(metamodel, "a");
        a.setExtendedConcept(a);
        metamodel.addElement(a);

        assertEquals(new HashSet<>(Arrays.asList(new Issue(IssueSeverity.Error, "Cyclic hierarchy found", a))),
                new MetamodelValidator().validateMetamodel(metamodel).getIssues());
    }

    @Test
    public void indirectSelfInheritanceOfConceptsIsCaught() {
        Metamodel metamodel = new Metamodel("MyMetamodel");
        Concept a = new Concept(metamodel, "a");
        Concept b = new Concept(metamodel, "b");
        a.setExtendedConcept(b);
        b.setExtendedConcept(a);
        metamodel.addElement(a);
        metamodel.addElement(b);

        assertEquals(new HashSet<>(Arrays.asList(
                new Issue(IssueSeverity.Error, "Cyclic hierarchy found", a),
                        new Issue(IssueSeverity.Error, "Cyclic hierarchy found", b))),
                new MetamodelValidator().validateMetamodel(metamodel).getIssues());
    }

    @Test
    public void indirectSelfInheritanceOfConceptInterfacesIsCaught() {
        Metamodel metamodel = new Metamodel("MyMetamodel");
        ConceptInterface a = new ConceptInterface(metamodel, "a");
        ConceptInterface b = new ConceptInterface(metamodel, "b");
        a.addExtendedInterface(b);
        b.addExtendedInterface(a);
        metamodel.addElement(a);
        metamodel.addElement(b);

        assertEquals(new HashSet<>(Arrays.asList(
                        new Issue(IssueSeverity.Error, "Cyclic hierarchy found", a),
                        new Issue(IssueSeverity.Error, "Cyclic hierarchy found", b))),
                new MetamodelValidator().validateMetamodel(metamodel).getIssues());
    }

    @Test
    public void multipleDirectImplementationsOfTheSameInterfaceAreNotAllowed() {
        Metamodel metamodel = new Metamodel("MyMetamodel");
        Concept a = new Concept(metamodel, "A");
        ConceptInterface i = new ConceptInterface(metamodel, "I");

        a.addImplementedInterface(i);
        a.addImplementedInterface(i);

        metamodel.addElement(a);
        metamodel.addElement(i);

        assertEquals(new HashSet<>(Arrays.asList(new Issue(IssueSeverity.Error, "The same interface has been implemented multiple times", a))),
                new MetamodelValidator().validateMetamodel(metamodel).getIssues());
    }
    @Test
    public void multipleIndirectImplementationsOfTheSameInterfaceAreAllowed() {
        Metamodel metamodel = new Metamodel("MyMetamodel");
        Concept a = new Concept(metamodel, "A");
        Concept b = new Concept(metamodel, "B");
        ConceptInterface i = new ConceptInterface(metamodel, "I");

        a.setExtendedConcept(b);
        a.addImplementedInterface(i);
        b.addImplementedInterface(i);

        metamodel.addElement(a);
        metamodel.addElement(b);
        metamodel.addElement(i);

        assertEquals(new HashSet<>(Arrays.asList()),
                new MetamodelValidator().validateMetamodel(metamodel).getIssues());
    }
}
