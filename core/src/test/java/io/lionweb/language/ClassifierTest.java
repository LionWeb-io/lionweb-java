package io.lionweb.language;

import io.lionweb.LionWebVersion;
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

    @Test
    public void getPropertyByID() {
        assertEquals(LionCore.getLanguage(LionWebVersion.v2023_1).getPropertyByName("name"),
                LionCore.getLanguage(LionWebVersion.v2023_1).getPropertyByID("LionCore-builtins-INamed-name"));
        assertEquals(LionCore.getLanguage(LionWebVersion.v2024_1).getPropertyByName("name"),
                LionCore.getLanguage(LionWebVersion.v2024_1).getPropertyByID("LionCore-builtins-INamed-name-2024-1"));
    }

    @Test
    public void getContainmentByID() {
        assertEquals(LionCore.getLanguage(LionWebVersion.v2023_1).getContainmentByName("entities"),
                LionCore.getLanguage(LionWebVersion.v2023_1).getContainmentByID("-id-Language-entities"));
        assertEquals(LionCore.getLanguage(LionWebVersion.v2024_1).getContainmentByName("entities"),
                LionCore.getLanguage(LionWebVersion.v2024_1).getContainmentByID("-id-Language-entities-2024-1"));
    }

    @Test
    public void getReferenceByID() {
        assertEquals(LionCore.getLanguage(LionWebVersion.v2023_1).getReferenceByName("dependsOn"),
                LionCore.getLanguage(LionWebVersion.v2023_1).getReferenceByID("-id-Language-dependsOn"));
        assertEquals(LionCore.getLanguage(LionWebVersion.v2024_1).getReferenceByName("dependsOn"),
                LionCore.getLanguage(LionWebVersion.v2024_1).getReferenceByID("-id-Language-dependsOn-2024-1"));
    }
}
