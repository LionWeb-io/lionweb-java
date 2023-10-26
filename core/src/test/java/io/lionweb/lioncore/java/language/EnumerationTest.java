package io.lionweb.lioncore.java.language;

import org.junit.Test;

import static org.junit.Assert.assertSame;

public class EnumerationTest {
    @Test
    public void literalParentIsEnum_Constructor() {
        Enumeration enm = new Enumeration();
        enm.setName("MyEnum");

        EnumerationLiteral lit = new EnumerationLiteral(enm, "Lit1");

        assertSame(enm, lit.getParent());
        assertSame(enm, lit.getEnumeration());
    }

    @Test
    public void literalParentIsEnum_setParent() {
        Enumeration enm = new Enumeration();
        enm.setName("MyEnum");

        EnumerationLiteral lit = new EnumerationLiteral();
        lit.setParent(enm);

        assertSame(enm, lit.getParent());
        assertSame(enm, lit.getEnumeration());
    }

    @Test
    public void literalParentIsEnum_setEnumeration() {
        Enumeration enm = new Enumeration();
        enm.setName("MyEnum");

        EnumerationLiteral lit = new EnumerationLiteral();
        lit.setEnumeration(enm);

        assertSame(enm, lit.getParent());
        assertSame(enm, lit.getEnumeration());
    }
}