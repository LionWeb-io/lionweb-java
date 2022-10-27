package org.lionweb.lioncore.java;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LionCoreBuiltinsTest {

    @Test
    public void stringPrimitiveType() {
        PrimitiveType string = (PrimitiveType)LionCoreBuiltins.getInstance().getElementByName("String");
        assertEquals("String", string.getSimpleName());
        assertEquals("org.lionweb.Builtins.String", string.qualifiedName());
    }
}
