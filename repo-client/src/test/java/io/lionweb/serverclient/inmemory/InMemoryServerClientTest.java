package io.lionweb.serverclient.inmemory;

import io.lionweb.LionWebVersion;
import io.lionweb.language.Concept;
import io.lionweb.language.Language;
import io.lionweb.serverclient.api.HistorySupport;
import io.lionweb.serverclient.api.RepositoryConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InMemoryServerClientTest {

    @Test
    public void testRepositoriesCRUD() {
        InMemoryServer server = new InMemoryServer();
        InMemoryServerClient client = new InMemoryServerClient(server);
        assertEquals(Collections.emptySet(), client.listRepositories());

        client.createRepository(new RepositoryConfiguration("MyRepo", LionWebVersion.v2024_1, HistorySupport.DISABLED));
        assertEquals(Collections.singleton(new RepositoryConfiguration("MyRepo", LionWebVersion.v2024_1, HistorySupport.DISABLED)), client.listRepositories());

        client.deleteRepository("MyRepo");
        assertEquals(Collections.emptySet(), client.listRepositories());
    }

    @Test
    public void testPartitionsCRUD() {
        InMemoryServer server = new InMemoryServer();
        InMemoryServerClient client = new InMemoryServerClient(server);
        client.createRepository(new RepositoryConfiguration("MyRepo", LionWebVersion.v2024_1, HistorySupport.DISABLED));
        client.setRepositoryName("MyRepo");

        assertEquals(Collections.emptyList(), client.listPartitions());

        Language l1 = new Language(LionWebVersion.v2024_1, "MyLanguage")
                .setID("l-id")
                .setKey("l-key")
                .setVersion("1.0");
        Concept c1 = new Concept(l1, "MyConcept")
                .setID("c1-id")
                .setKey("c1-key");

        client.createPartitions(Collections.singletonList(l1));
        assertEquals(Collections.singletonList(l1), client.listPartitions());

        client.deletePartitions(Collections.singletonList("l-id"));
        assertEquals(Collections.emptyList(), client.listPartitions());
    }
}
