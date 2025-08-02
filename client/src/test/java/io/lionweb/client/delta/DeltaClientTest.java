package io.lionweb.client.delta;

import io.lionweb.client.delta.messages.commands.ChangeProperty;
import io.lionweb.language.Language;
import io.lionweb.language.LionCoreBuiltins;
import io.lionweb.serialization.data.MetaPointer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DeltaClientTest {

    @Test
    public void setPropertyCommand() {
        MockDeltaChannel deltaClient = new MockDeltaChannel();
        DeltaSynchronizer synchronizer = new DeltaSynchronizer(deltaClient);

        Language l1 = new Language();
        l1.setID("l1");

        synchronizer.attachTree(l1);
        l1.setName("MyName");

        assertEquals(1, deltaClient.commands.size());
        assertEquals(new ChangeProperty(
                "cmd-1",
                "l1",
                MetaPointer.from(LionCoreBuiltins.getINamed().getPropertyByName("name")),
                "MyName"
        ), deltaClient.commands.get(0));
    }
}
