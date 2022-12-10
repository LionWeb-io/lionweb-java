package org.lionweb.lioncore.java.metamodel;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PrimitiveTypeTest {

    @Test
    public void anEmptyPrimitiveTypeIsInvalid() {
        PrimitiveType primitiveType = new PrimitiveType();
        assertFalse(primitiveType.validate().isSuccessful());
        assertFalse(primitiveType.isValid());
        assertEquals(2, primitiveType.validate().getIssues().size());
        assertTrue(primitiveType.validate().getIssues().stream().allMatch(issue -> issue.isError()));
        assertEquals(new HashSet<>(Arrays.asList("Metamodel not set", "Simple name not set")), primitiveType.validate().getIssues().stream().map(issue -> issue.getMessage()).collect(Collectors.toSet()));
    }

    @Test
    public void aPrimitiveTypeCanBeValid() {
        Metamodel metamodel = new Metamodel();
        PrimitiveType primitiveType = new PrimitiveType(metamodel, "PrimitiveType");
        assertTrue(primitiveType.validate().isSuccessful());
        assertTrue(primitiveType.isValid());
        assertEquals(0, primitiveType.validate().getIssues().size());
    }
}
