package org.lionweb.lioncore.java.metamodel;

import org.junit.Test;
import org.lionweb.lioncore.java.utils.IssueSeverity;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AnnotationTest {

    @Test
    public void anEmptyAnnotationIsInvalid() {
        Annotation annotation = new Annotation();
        assertFalse(annotation.validate().isSuccessful());
        assertFalse(annotation.isValid());
        assertEquals(2, annotation.validate().getIssues().size());
        assertTrue(annotation.validate().getIssues().stream().allMatch(issue -> issue.isError()));
        assertEquals(new HashSet<>(Arrays.asList("Metamodel not set", "Simple name not set")), annotation.validate().getIssues().stream().map(issue -> issue.getMessage()).collect(Collectors.toSet()));
    }

    @Test
    public void anAnnotationCanBeValid() {
        Metamodel metamodel = new Metamodel();
        Annotation annotation = new Annotation(metamodel, "MyAnnotation");
        assertTrue(annotation.validate().isSuccessful());
        assertTrue(annotation.isValid());
        assertEquals(0, annotation.validate().getIssues().size());
    }
}
