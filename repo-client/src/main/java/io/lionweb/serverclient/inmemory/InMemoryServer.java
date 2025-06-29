package io.lionweb.serverclient.inmemory;

import io.lionweb.model.ClassifierInstance;
import io.lionweb.model.Node;
import io.lionweb.serialization.SerializationProvider;
import io.lionweb.serialization.data.SerializedChunk;
import io.lionweb.serialization.data.SerializedClassifierInstance;
import io.lionweb.serverclient.api.HistorySupport;
import io.lionweb.serverclient.api.RepositoryConfiguration;
import io.lionweb.serverclient.api.RepositoryVersionToken;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class InMemoryServer {

    private Map<String, RepositoryData> repositories = new HashMap<>();

    private @NotNull RepositoryData getRepository(@NotNull String repositoryName) {
        Objects.requireNonNull(repositoryName);
        RepositoryData repositoryData = repositories.get(repositoryName);
        if (repositoryData == null) {
            throw new IllegalArgumentException();
        }
        return repositoryData;
    }

    public RepositoryConfiguration getRepositoryConfiguration(@NotNull String repositoryName) {
        return getRepository(repositoryName).configuration;
    }

    public @NotNull RepositoryVersionToken createPartitions(@NotNull String repositoryName, @NotNull List<Node> partitions) {
        Objects.requireNonNull(partitions);
        RepositoryData repositoryData = getRepository(repositoryName);
        SerializedChunk serializedChunk = SerializationProvider.getStandardJsonSerialization(repositoryData.configuration.getLionWebVersion()).serializeTreesToSerializationChunk(partitions);
        return createPartitions(repositoryName, serializedChunk);
    }

    public @NotNull RepositoryVersionToken createPartitions(@NotNull String repositoryName, @NotNull SerializedChunk partitions) {
        Objects.requireNonNull(partitions);
        RepositoryData repositoryData = getRepository(repositoryName);
        repositoryData.partitionIDs.addAll(partitions.getClassifierInstances().stream().filter(n -> n.getParentNodeID() == null).map(SerializedClassifierInstance::getID).filter(id ->
                !repositoryData.partitionIDs.contains(id)).collect(Collectors.toList()));
        repositoryData.store(partitions.getClassifierInstances());
        return repositoryData.bumpVersion();
    }

    public @NotNull RepositoryVersionToken deletePartitions(@NotNull String repositoryName, @NotNull List<String> partitionIds) {
        Objects.requireNonNull(partitionIds);
        RepositoryData repositoryData = getRepository(repositoryName);
        repositoryData.partitionIDs.removeIf(partitionIds::contains);
        // TODO remove descendants
        return repositoryData.bumpVersion();
    }

    public @NotNull List<Node> listPartitions(@NotNull String repositoryName) {
        RepositoryData repositoryData = getRepository(repositoryName);
        List<SerializedClassifierInstance> nodes = repositoryData.retrieveTrees(repositoryData.partitionIDs);
        SerializedChunk serializedChunk = new SerializedChunk();
        serializedChunk.setSerializationFormatVersion(repositoryData.configuration.getLionWebVersion().getVersionString());
        nodes.forEach(serializedChunk::addClassifierInstance);
        // TODO add languages

        return SerializationProvider
                .getStandardJsonSerialization(repositoryData.configuration.getLionWebVersion()).deserializeSerializationChunk(serializedChunk)
                .stream()
                .filter(c -> c instanceof Node)
                .map(c -> (Node)c)
                .filter(Node::isRoot)
                .collect(Collectors.toList());
    }

    public @NotNull List<String> ids(@NotNull String repositoryName, int count) {
        if (count < 0) {
            throw new IllegalArgumentException();
        }
        RepositoryData repositoryData = getRepository(repositoryName);
        return repositoryData.ids(count);
    }

    public @NotNull RepositoryVersionToken store(@NotNull String repositoryName, @NotNull List<Node> nodes) {
        Objects.requireNonNull(nodes);
        RepositoryData repositoryData = getRepository(repositoryName);
//        repositoryData.store(nodes);
//        return repositoryData.bumpVersion();
        throw new UnsupportedOperationException();
    }

    public @NotNull Set<RepositoryConfiguration> listRepositories() {
        return repositories.values().stream().map(r -> r.configuration).collect(Collectors.toSet());
    }

    public void createRepository(@NotNull RepositoryConfiguration repositoryConfiguration) {
        Objects.requireNonNull(repositoryConfiguration);
        if (repositoryConfiguration.getHistorySupport() == HistorySupport.ENABLED) {
            throw new IllegalArgumentException();
        }
        repositories.put(repositoryConfiguration.getName(), new RepositoryData(repositoryConfiguration));
    }

    public void deleteRepository(@NotNull String repositoryName) {
        Objects.requireNonNull(repositoryName);
        if (!repositories.containsKey(repositoryName)) {
            throw new IllegalArgumentException();
        }
        repositories.remove(repositoryName);
    }

}
