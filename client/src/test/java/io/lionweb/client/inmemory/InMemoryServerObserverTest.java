package io.lionweb.client.inmemory;

import io.lionweb.LionWebVersion;
import io.lionweb.client.api.HistorySupport;
import io.lionweb.client.api.RepositoryConfiguration;
import io.lionweb.language.Language;
import io.lionweb.serialization.JsonSerialization;
import io.lionweb.serialization.SerializationProvider;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InMemoryServerObserverTest {

    class MockInMemoryServerObserver implements InMemoryServerObserver {

        List<String> partitionsAdded = new ArrayList<>();
        List<String> partitionsRemoved = new ArrayList<>();

        @Override
        public void partitionAdded(String partitionId) {
            partitionsAdded.add(partitionId);
        }

        @Override
        public void partitionRemoved(String partitionId) {
            partitionsRemoved.add(partitionId);
        }

        @Override
        public void nodeDeleted(String node) {

        }
    }

    @Test
    public void addingPartitions() {
        InMemoryServer server = new InMemoryServer();
        server.createRepository(new RepositoryConfiguration("repo1", LionWebVersion.v2024_1, HistorySupport.DISABLED));

        Language l1 = new Language();
        l1.setID("l1");

        JsonSerialization jsonSerialization = SerializationProvider.getStandardJsonSerialization(LionWebVersion.v2024_1);

        MockInMemoryServerObserver observer = new MockInMemoryServerObserver();
        server.registerObserver(observer);

        server.createPartitionFromChunk("repo1", jsonSerialization.serializeTreeToSerializationChunk(l1).getClassifierInstances());
        assertEquals(Collections.singletonList("l1"), observer.partitionsAdded);
    }

    @Test
    public void removingPartitions() {
        InMemoryServer server = new InMemoryServer();
        server.createRepository(new RepositoryConfiguration("repo1", LionWebVersion.v2024_1, HistorySupport.DISABLED));

        Language l1 = new Language();
        l1.setID("l1");

        JsonSerialization jsonSerialization = SerializationProvider.getStandardJsonSerialization(LionWebVersion.v2024_1);
        server.createPartitionFromChunk("repo1", jsonSerialization.serializeTreeToSerializationChunk(l1).getClassifierInstances());

        MockInMemoryServerObserver observer = new MockInMemoryServerObserver();
        server.registerObserver(observer);

        server.deletePartitions("repo1", Collections.singletonList("l1"));

        assertEquals(Collections.singletonList("l1"), observer.partitionsRemoved);
    }
}
