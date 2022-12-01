package org.lionweb.lioncore.java.utils;

import org.junit.Test;
import org.lionweb.lioncore.java.utils.InvalidName;
import org.lionweb.lioncore.java.utils.Naming;

import static org.junit.Assert.assertEquals;

public class NamingTest {

    @Test
    public void validSimpleName() {
        Naming.validateSimpleName("myID123");
    }

    @Test(expected = InvalidName.class)
    public void invalidSimpleNameStartingWithDigits() {
        Naming.validateSimpleName("1myID");
    }

    @Test
    public void validQualifiedName() {
        Naming.validateQualifiedName("myID123.a.b");
    }
}
