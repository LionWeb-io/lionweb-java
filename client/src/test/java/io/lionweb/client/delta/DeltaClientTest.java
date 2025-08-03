package io.lionweb.client.delta;

import io.lionweb.client.delta.messages.DeltaQueryResponse;
import io.lionweb.client.delta.messages.commands.ChangeProperty;
import io.lionweb.client.delta.messages.queries.GetAvailableIds;
import io.lionweb.client.delta.messages.queries.SignOff;
import io.lionweb.client.delta.messages.queries.SignOn;
import io.lionweb.language.Language;
import io.lionweb.language.LionCoreBuiltins;
import io.lionweb.serialization.data.MetaPointer;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DeltaClientTest {

  @Test
  public void connectionAndDisconnection() {
    MockDeltaChannel deltaChannel = new MockDeltaChannel();
    DeltaQueryResponse response = deltaChannel.sendQuery(new SignOn("q1", DeltaProtocolVersion.v2025_1, "TestClient"));
    assertEquals("q1", response.queryId);
    String participationId = (String)response.values.get("participationId");
    assertNotNull(participationId);
    assertFalse(participationId.isEmpty());

    deltaChannel.sendQuery(new SignOff("q2"));
  }

  @Test
  public void gettingIds() {
    MockDeltaChannel deltaChannel = new MockDeltaChannel();
    DeltaQueryResponse response = deltaChannel.sendQuery(new GetAvailableIds("q1",5));
    assertEquals("q1", response.queryId);
    List<String> ids = (List<String>)response.values.get("ids");
    assertEquals(5, ids.size());
  }

  @Test
  public void setPropertyCommand() {
    MockDeltaChannel deltaChannel = new MockDeltaChannel();
    DeltaSynchronizer synchronizer = new MockDeltaSynchronizer(deltaChannel);

    Language l1 = new Language();
    l1.setID("l1");

    synchronizer.attachTree(l1);
    l1.setName("MyName");

    assertEquals(1, deltaChannel.commands.size());
    assertEquals(
        new ChangeProperty(
            "cmd-1",
            "l1",
            MetaPointer.from(LionCoreBuiltins.getINamed().getPropertyByName("name")),
            "MyName"),
        deltaChannel.commands.get(0));
  }
}
