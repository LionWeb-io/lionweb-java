package io.lionweb.client.delta;

import io.lionweb.client.delta.messages.commands.ChangeProperty;
import io.lionweb.language.Language;
import io.lionweb.language.LionCoreBuiltins;
import io.lionweb.serialization.data.MetaPointer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SynchronizationTest {

  @Test
  public void setPropertyCommand() {
    MockDeltaChannel deltaClient = new MockDeltaChannel();
    DeltaSynchronizer synchronizer = new DeltaSynchronizer(deltaClient);

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
