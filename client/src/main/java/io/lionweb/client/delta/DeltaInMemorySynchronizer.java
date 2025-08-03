package io.lionweb.client.delta;

import io.lionweb.LionWebVersion;
import io.lionweb.client.inmemory.InMemoryServer;
import io.lionweb.model.Node;
import io.lionweb.serialization.JsonSerialization;
import io.lionweb.serialization.LowLevelJsonSerialization;
import io.lionweb.serialization.SerializationProvider;
import io.lionweb.serialization.data.SerializedChunk;

public class DeltaInMemorySynchronizer extends DeltaSynchronizer {

    private InMemoryServer server;
    private String repositoryName;
    private JsonSerialization jsonSerialization;
    private LionWebVersion lionWebVersion;

    public DeltaInMemorySynchronizer(DeltaChannel channel, InMemoryServer server,
                                     String repositoryName, LionWebVersion lionWebVersion) {
        super(channel);
        this.server = server;
        this.repositoryName = repositoryName;
        jsonSerialization = SerializationProvider.getStandardJsonSerialization(lionWebVersion);
    }

    @Override
    protected void forceState(Node node) {
        if (node.getParent() == null) {
            if (!server.listPartitionIDs(repositoryName).contains(node.getID())) {
                SerializedChunk chunk = jsonSerialization.serializeTreeToSerializationChunk(node);
                server.createPartitionFromChunk(repositoryName, chunk.getClassifierInstances());
                return;
            }
        }
        SerializedChunk chunk = jsonSerialization.serializeTreeToSerializationChunk(node);
        server.store(repositoryName, chunk.getClassifierInstances());
    }
}
