package io.lionweb.client.delta;

import static org.junit.Assert.assertEquals;

import io.lionweb.language.Language;
import org.junit.jupiter.api.Test;

public class DeltaClientAndServerTest {

  @Test
  public void simpleSynchronizationOfNodesInstances() {
    Language language1 = new Language("Language A", "lang-a", "lang-a-key");
    Language language2 = new Language("Language A", "lang-a", "lang-a-key");

    DeltaChannel channel = new InMemoryDeltaChannel();
    DeltaClient client = new DeltaClient(channel);
    DeltaServer server = new DeltaServer(channel);

    // TODO should we first create the nodes on the server and then check them out?

    client.synchronize(language1);
    client.synchronize(language2);

    assertEquals("Language A", language1.getName());
    assertEquals("Language A", language2.getName());

    language1.setName("Language B");
    assertEquals("Language B", language1.getName());
    assertEquals("Language B", language2.getName());

    language2.setName("Language C");
    assertEquals("Language C", language1.getName());
    assertEquals("Language C", language2.getName());
  }
}
