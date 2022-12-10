package org.lionweb.lioncore.java.self;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LionCoreTest {

    @Test
    public void lionCoreHasAllTheElements() {
        assertEquals(18, LionCore.getInstance().getElements().size());
    }
}
