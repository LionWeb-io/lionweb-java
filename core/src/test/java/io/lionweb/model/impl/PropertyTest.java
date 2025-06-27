package io.lionweb.model.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import io.lionweb.LionWebVersion;
import io.lionweb.language.*;
import org.junit.Test;

public class PropertyTest {

  @Test
  public void equality() {
    Property p1 = new Property(LionWebVersion.v2023_1);
    Property p2 = new Property(LionWebVersion.v2023_1);
    assertEquals(p1, p2);

    p1.setID("id2");
    p2.setID("id1");
    assertNotEquals(p1, p2);

    p1.setID("id1");
    assertEquals(p1, p2);

    p1.setType(LionCoreBuiltins.getString(LionWebVersion.v2023_1));
    p2.setType(LionCoreBuiltins.getString(LionWebVersion.v2023_1));
    assertEquals(p1, p2);

    Language l1 = new Language(LionWebVersion.v2023_1, "lang");
    Language l2 = new Language(LionWebVersion.v2023_1, "lang");
    Concept c1 = new Concept(l1, "C", "c-id", "c-key");
    Concept c2 = new Concept(l2, "C", "c-id", "c-key");
    Concept c3 = new Concept(l2, "C", "c3-id", "c-key");
    p1.setParent(c1);
    p2.setParent(c3);
    assertNotEquals(p1, p2);

    p2.setParent(c2);
    assertEquals(p1, p2);
  }
}
