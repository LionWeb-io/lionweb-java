package io.lionweb.client.inmemory;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.lionweb.LionWebVersion;
import io.lionweb.client.api.HistorySupport;
import io.lionweb.client.api.RepositoryConfiguration;
import io.lionweb.client.delta.DeltaInMemorySynchronizer;
import io.lionweb.client.delta.DeltaSynchronizer;
import io.lionweb.language.Language;
import io.lionweb.language.LionCoreBuiltins;
import io.lionweb.serialization.data.MetaPointer;
import java.util.Collections;
import org.junit.jupiter.api.Test;

public class LocalInMemoryDeltaChannelTest {

  @Test
  public void setProperty() {
    InMemoryServer server = new InMemoryServer();
    server.createRepository(
        new RepositoryConfiguration("repo1", LionWebVersion.v2024_1, HistorySupport.DISABLED));

    LocalInMemoryDeltaChannel channel = new LocalInMemoryDeltaChannel(server, "repo1");
    DeltaSynchronizer synchronizer =
        new DeltaInMemorySynchronizer(channel, server, "repo1", LionWebVersion.v2024_1);

    // TODO create a new node
    //      when I attach it to a synchronizer, if it is a partition
    //      and it does not exist, it is created

    assertEquals(Collections.emptyList(), server.listPartitionIDs("repo1"));
    Language l1 = new Language("MyLanguage");
    l1.setID("l1");
    synchronizer.attachTree(l1);
    assertEquals(Collections.singletonList("l1"), server.listPartitionIDs("repo1"));
    assertEquals(
        "MyLanguage",
        server
            .retrieve("repo1", Collections.singletonList("l1"), 0)
            .get(0)
            .getPropertyValue(
                MetaPointer.from(LionCoreBuiltins.getINamed().getPropertyByName("name"))));

    l1.setName("MyOtherLanguage");
    assertEquals(
        "MyOtherLanguage",
        server
            .retrieve("repo1", Collections.singletonList("l1"), 0)
            .get(0)
            .getPropertyValue(
                MetaPointer.from(LionCoreBuiltins.getINamed().getPropertyByName("name"))));
  }
}
