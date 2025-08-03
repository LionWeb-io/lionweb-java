package io.lionweb.client.delta;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.lionweb.language.Language;
import org.junit.jupiter.api.Test;

public class SynchronizationTest {

  @Test
  public void setPropertyCommand() {
    MockDeltaChannel deltaClient = new MockDeltaChannel();
    DeltaSynchronizer synchronizer = new MockDeltaSynchronizer(deltaClient);

    Language l1a = new Language();
    l1a.setID("l1");
    synchronizer.attachTree(l1a);

    Language l1b = new Language();
    l1b.setID("l1");
    synchronizer.attachTree(l1b);

    l1a.setName("MyName");
    assertEquals("MyName", l1a.getName());
    assertEquals("MyName", l1b.getName());
  }
}
