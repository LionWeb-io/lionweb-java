package io.lionweb.serverclient.inmemory;

import io.lionweb.model.Node;
import io.lionweb.serialization.JsonSerialization;
import io.lionweb.serialization.LowLevelJsonSerialization;
import io.lionweb.serialization.SerializationProvider;
import io.lionweb.serialization.data.SerializedChunk;
import io.lionweb.serialization.data.SerializedClassifierInstance;
import io.lionweb.serverclient.api.HistorySupport;
import io.lionweb.serverclient.api.RepositoryConfiguration;
import io.lionweb.serverclient.api.RepositoryVersionToken;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InMemoryServer {

    private class RepositoryData {
        private @NotNull RepositoryConfiguration configuration;
        private List<String> partitions = new ArrayList<>();
        private Map<String, SerializedClassifierInstance> nodes = new HashMap<>();
        private int currentVersion = 0;
        private int nextId = 1;

        public RepositoryData(@NotNull RepositoryConfiguration configuration) {
            this.configuration = configuration;
        }

        RepositoryVersionToken bumpVersion() {
            return new RepositoryVersionToken("v-" + ++currentVersion);
        }

        public List<String> ids(int count) {
            List<String> res = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                res.add("id-" + (nextId++));
            }
            return res;
        }

        public void store(List<SerializedClassifierInstance> nodes) {
            Map<String, SerializedClassifierInstance> originalNodes = new HashMap<>();
            Map<String, SerializedClassifierInstance> nodesToAssign = new HashMap<>();
            nodes.forEach(n -> nodesToAssign.put(n.getID(), n));
            for (String partitionId : partitions) {
                SerializedClassifierInstance partition = this.nodes.get(partitionId);
                Stream<SerializedClassifierInstance> nodesInPartition = thisAndAllDescendants(partition);
                nodesInPartition.forEach(existingNode -> {
                    if (nodesToAssign.containsKey(existingNode.getID())) {
                        originalNodes.put(existingNode.getID(), existingNode);
                    }
                });
            }
            if (originalNodes.size() != nodesToAssign.size()) {
                throw new IllegalStateException();
            }
            nodesToAssign.forEach((key, value) -> {
                if (isRoot(value)) {
                    throw new UnsupportedOperationException();
                } else {
                    throw new UnsupportedOperationException();
                }
            });
        }

        private boolean isRoot(SerializedClassifierInstance node) {
            return node.getParentNodeID() == null;
        }

        private Stream<SerializedClassifierInstance> thisAndAllDescendants(SerializedClassifierInstance root) {
            throw new UnsupportedOperationException();
        }
    }

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
        repositoryData.partitions.addAll(partitions.getClassifierInstances().stream().filter(n -> n.getParentNodeID() == null).map(SerializedClassifierInstance::getID).filter(id ->
                !repositoryData.partitions.contains(id)).collect(Collectors.toList()));
        repositoryData.store(partitions.getClassifierInstances());
        return repositoryData.bumpVersion();
    }

    public @NotNull RepositoryVersionToken deletePartitions(@NotNull String repositoryName, @NotNull List<String> partitionIds) {
        Objects.requireNonNull(partitionIds);
        RepositoryData repositoryData = getRepository(repositoryName);
        repositoryData.partitions.removeIf(partitionIds::contains);
        // TODO remove descendants
        return repositoryData.bumpVersion();
    }

    public @NotNull List<Node> listPartitions(@NotNull String repositoryName) {
        RepositoryData repositoryData = getRepository(repositoryName);
        //return repositoryData.partitions;
        throw new UnsupportedOperationException();
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
