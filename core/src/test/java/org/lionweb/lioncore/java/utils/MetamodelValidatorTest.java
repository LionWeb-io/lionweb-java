package org.lionweb.lioncore.java.utils;

import org.junit.Test;
import org.lionweb.lioncore.java.metamodel.Annotation;
import org.lionweb.lioncore.java.metamodel.Metamodel;
import org.lionweb.lioncore.java.metamodel.PrimitiveType;

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
}
