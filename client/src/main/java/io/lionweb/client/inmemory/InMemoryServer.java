package io.lionweb.client.inmemory;

import io.lionweb.LionWebVersion;
import io.lionweb.client.api.HistorySupport;
import io.lionweb.client.api.RepositoryConfiguration;
import io.lionweb.client.api.RepositoryVersionToken;
import io.lionweb.model.Node;
import io.lionweb.serialization.AbstractSerialization;
import io.lionweb.serialization.JsonSerialization;
import io.lionweb.serialization.SerializationProvider;
import io.lionweb.serialization.data.SerializedChunk;
import io.lionweb.serialization.data.SerializedClassifierInstance;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

/**
 * An InMemoryServer is useful for testing and as a replacement for a proper server, when performing processing
 * operations.
 */
public class InMemoryServer {

  private final Map<String, RepositoryData> repositories = new HashMap<>();
  private final Map<LionWebVersion, AbstractSerialization> supportSerialization = new HashMap<>();

  public @NotNull RepositoryConfiguration getRepositoryConfiguration(@NotNull String repositoryName) {
    return getRepository(repositoryName).configuration;
  }

//  public @NotNull RepositoryVersionToken createPartitions(
//      @NotNull String repositoryName, @NotNull List<Node> partitions) {
//    Objects.requireNonNull(partitions);
//    RepositoryData repositoryData = getRepository(repositoryName);
//    SerializedChunk serializedChunk = getSupportSerialization(repositoryData).serializeTreesToSerializationChunk(partitions);
//    return createPartitionFromChunk(repositoryName, serializedChunk.getClassifierInstances());
//  }

  public @NotNull RepositoryVersionToken createPartitionFromChunk(
      @NotNull String repositoryName, @NotNull List<SerializedClassifierInstance> partitions) {
    Objects.requireNonNull(partitions);
    RepositoryData repositoryData = getRepository(repositoryName);
    // We get all roots (i.e. -> partitions) which do not yet exist
    // and add them to the list of partition IDs
    repositoryData.partitionIDs.addAll(
        partitions.stream()
            .filter(n -> n.getParentNodeID() == null)
            .map(SerializedClassifierInstance::getID)
            .filter(id -> !repositoryData.partitionIDs.contains(id))
            .collect(Collectors.toList()));
    repositoryData.store(partitions);
    return repositoryData.bumpVersion();
  }

  public @NotNull RepositoryVersionToken deletePartitions(
      @NotNull String repositoryName, @NotNull List<String> partitionIds) {
    Objects.requireNonNull(partitionIds);
    RepositoryData repositoryData = getRepository(repositoryName);
    repositoryData.partitionIDs.removeIf(partitionIds::contains);
    partitionIds.forEach(repositoryData::deleteNodeAndDescendant);
    return repositoryData.bumpVersion();
  }

//  public @NotNull List<Node> listPartitions(@NotNull String repositoryName) {
//    RepositoryData repositoryData = getRepository(repositoryName);
//    List<SerializedClassifierInstance> nodes =
//        repositoryData.retrieveTrees(repositoryData.partitionIDs);
//    SerializedChunk serializedChunk = new SerializedChunk();
//    serializedChunk.setSerializationFormatVersion(
//        repositoryData.configuration.getLionWebVersion().getVersionString());
//    nodes.forEach(serializedChunk::addClassifierInstance);
//    // TODO add languages
//
//    return SerializationProvider.getStandardJsonSerialization(
//            repositoryData.configuration.getLionWebVersion())
//        .deserializeSerializationChunk(serializedChunk).stream()
//        .filter(c -> c instanceof Node)
//        .map(c -> (Node) c)
//        .filter(Node::isRoot)
//        .collect(Collectors.toList());
//  }

  public @NotNull List<String> ids(@NotNull String repositoryName, int count) {
    if (count < 0) {
      throw new IllegalArgumentException();
    }
    RepositoryData repositoryData = getRepository(repositoryName);
    return repositoryData.ids(count);
  }

//  public @NotNull RepositoryVersionToken store(
//      @NotNull String repositoryName, @NotNull List<Node> nodes) {
//    Objects.requireNonNull(nodes);
//    RepositoryData repositoryData = getRepository(repositoryName);
//    //        repositoryData.store(nodes);
//    //        return repositoryData.bumpVersion();
//    throw new UnsupportedOperationException();
//  }

  public @NotNull Set<RepositoryConfiguration> listRepositories() {
    return repositories.values().stream().map(r -> r.configuration).collect(Collectors.toSet());
  }

  public void createRepository(@NotNull RepositoryConfiguration repositoryConfiguration) {
    Objects.requireNonNull(repositoryConfiguration);
    if (repositoryConfiguration.getHistorySupport() == HistorySupport.ENABLED) {
      throw new IllegalArgumentException();
    }
    repositories.put(
        repositoryConfiguration.getName(), new RepositoryData(repositoryConfiguration));
  }

  public void deleteRepository(@NotNull String repositoryName) {
    Objects.requireNonNull(repositoryName);
    if (!repositories.containsKey(repositoryName)) {
      throw new IllegalArgumentException();
    }
    repositories.remove(repositoryName);
  }

  private @NotNull RepositoryData getRepository(@NotNull String repositoryName) {
    Objects.requireNonNull(repositoryName, "RepositoryName should not be null");
    RepositoryData repositoryData = repositories.get(repositoryName);
    if (repositoryData == null) {
      throw new IllegalArgumentException("Cannot find repository named " + repositoryName);
    }
    return repositoryData;
  }

  private @NotNull AbstractSerialization getSupportSerialization(@NotNull RepositoryData repositoryData) {
    return supportSerialization.computeIfAbsent(repositoryData.configuration.getLionWebVersion(), SerializationProvider::getStandardJsonSerialization);
  }


  public List<SerializedClassifierInstance> retrieve(@NotNull String repositoryName, List<String> nodeIds, int limit) {
    Objects.requireNonNull(repositoryName, "RepositoryName should not be null");
    RepositoryData repositoryData = repositories.get(repositoryName);
    List<SerializedClassifierInstance> retrieved = new ArrayList<>();
    nodeIds.forEach(n -> repositoryData.retrieve(n, limit, retrieved));
    return retrieved;
  }

  public @NotNull List<String> listPartitionIDs(@NotNull String repositoryName) {
    Objects.requireNonNull(repositoryName, "RepositoryName should not be null");
    RepositoryData repositoryData = repositories.get(repositoryName);
    return repositoryData.partitionIDs;
  }


}
