package org.lionweb.lioncore.java.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ModelValidatorTest {

    @Test
    public void positiveCase() {
        assertEquals(true, ModelValidator.isValidID("foo"));
    }

    @Test
    public void emptyIDIsInvalid() {
        assertEquals(false, ModelValidator.isValidID(""));
    }

    @Test
    public void idsWithUmlautsAreInvalid() {
        assertEquals(false, ModelValidator.isValidID("foö"));
    }

    @Test
    public void idsWithAccentsAreInvalid() {
        assertEquals(false, ModelValidator.isValidID("foò"));
        assertEquals(false, ModelValidator.isValidID("foó"));
    }
}
