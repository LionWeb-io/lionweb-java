package io.lionweb.language;

import io.lionweb.lioncore.LionCore;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;

public class ClassifierTest {

    @Test
    public void allLinks() {
        assertEquals(new HashSet(Collections.singleton(
                LionCore.getLanguage().getReferenceByName("dependsOn")
        )), new HashSet(LionCore.getLanguage().allReferences()));
        assertEquals(new HashSet(Collections.singleton(
                LionCore.getLanguage().getContainmentByName("entities")
        )), new HashSet(LionCore.getLanguage().allContainments()));
        assertEquals(new HashSet(Arrays.asList(
                LionCore.getLanguage().getReferenceByName("dependsOn"),
                LionCore.getLanguage().getContainmentByName("entities")
        )), new HashSet(LionCore.getLanguage().allLinks()));
    }
}
