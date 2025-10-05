package io.lionweb.client.inmemory;

import io.lionweb.LionWebVersion;
import io.lionweb.client.api.*;
import io.lionweb.model.ClassifierInstance;
import io.lionweb.model.Node;
import io.lionweb.serialization.AbstractSerialization;
import io.lionweb.serialization.SerializationProvider;
import io.lionweb.serialization.UnavailableNodePolicy;
import io.lionweb.serialization.data.SerializationChunk;
import io.lionweb.serialization.data.SerializedClassifierInstance;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NodesLevelInMemoryServerClient
    implements BulkAPIClient, DBAdminAPIClient, InspectionAPIClient {
  private final @NotNull ChunkLevelInMemoryServerClient chunkLevel;
  private @Nullable AbstractSerialization serialization;

  public NodesLevelInMemoryServerClient(@NotNull InMemoryServer inMemoryServer) {
    this.chunkLevel = new ChunkLevelInMemoryServerClient(inMemoryServer);
  }

  /**
   * We do not make the repositoryName settable, as otherwise we may also need to change the
   * serialization, as it depends on the LionWebVersion.
   */
  public NodesLevelInMemoryServerClient(
      @NotNull InMemoryServer inMemoryServer, @NotNull String repositoryName) {
    Objects.requireNonNull(repositoryName);
    this.chunkLevel = new ChunkLevelInMemoryServerClient(inMemoryServer, repositoryName);
  }

  @Nullable
  public String getRepositoryName() {
    return chunkLevel.getRepositoryName();
  }

  /** This does not do anything. */
  @Override
  public void createDatabase() {
    // No op
  }

  @Override
  public List<String> ids(int count) {
    return chunkLevel.ids(count);
  }

  @Override
  public void createRepository(@NotNull RepositoryConfiguration repositoryConfiguration) {
    chunkLevel.createRepository(repositoryConfiguration);
  }

  @Override
  public void deleteRepository(@NotNull String repositoryName) {
    chunkLevel.deleteRepository(repositoryName);
  }

  @NotNull
  @Override
  public Set<RepositoryConfiguration> listRepositories() {
    return chunkLevel.listRepositories();
  }

  @NotNull
  @Override
  public LionWebVersion getLionWebVersion() {
    return chunkLevel.getLionWebVersion();
  }

  @Override
  public List<Node> listPartitions() {
    try {
      List<SerializedClassifierInstance> serializedNodes =
          chunkLevel.retrieveAsChunk(chunkLevel.listPartitionsIDs());
      return deserializeNodes(serializedNodes).stream()
          .filter(n -> n.getParent() == null)
          .collect(Collectors.toList());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Nullable
  @Override
  public RepositoryVersionToken createPartitions(List<Node> partitions) {
    AbstractSerialization serialization = getSerialization();
    return chunkLevel.createPartitionsFromChunk(
        serialization.serializeTreesToSerializationChunk(partitions).getClassifierInstances());
  }

  @Nullable
  @Override
  public RepositoryVersionToken deletePartitions(List<String> ids) {
    return chunkLevel.deletePartitions(ids);
  }

  @Override
  public List<String> listPartitionsIDs() {
    return chunkLevel.listPartitionsIDs();
  }

  @Override
  public List<Node> retrieve(List<String> nodeIds) {
    List<SerializedClassifierInstance> serializedClassifierInstances = null;
    try {
      serializedClassifierInstances = chunkLevel.retrieveAsChunk(nodeIds);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return deserializeNodes(serializedClassifierInstances);
  }

  private List<? extends ClassifierInstance<?>> deserialize(
      List<SerializedClassifierInstance> serializedNodes) {
    SerializationChunk chunk = new SerializationChunk();
    chunk.setSerializationFormatVersion(getLionWebVersion().getVersionString());
    serializedNodes.forEach(chunk::addClassifierInstance);
    chunk.populateUsedLanguages();
    return getSerialization().deserializeSerializationChunk(chunk);
  }

  @Nullable
  @Override
  public RepositoryVersionToken store(List<Node> nodes) {
    return chunkLevel.storeChunk(
        getSerialization().serializeTreesToSerializationChunk(nodes).getClassifierInstances());
  }

  @Override
  public List<Node> retrieve(List<String> nodeIds, int limit) {
    List<SerializedClassifierInstance> serializedClassifierInstances =
        chunkLevel.retrieveAsChunk(nodeIds, limit);
    return deserializeNodes(serializedClassifierInstances).stream()
        .filter(node -> nodeIds.contains(node.getID()))
        .collect(Collectors.toList());
  }

  //
  // InspectionAPIClient methods
  //

  @Override
  public Map<ClassifierKey, ClassifierResult> nodesByClassifier() {
    return chunkLevel.nodesByClassifier();
  }

  @Override
  public Map<ClassifierKey, ClassifierResult> nodesByClassifier(@Nullable Integer limit) {
    return chunkLevel.nodesByClassifier(limit);
  }

  @Override
  public Map<String, ClassifierResult> nodesByLanguage() {
    return chunkLevel.nodesByLanguage();
  }

  @Override
  public Map<String, ClassifierResult> nodesByLanguage(@Nullable Integer limit) {
    return chunkLevel.nodesByLanguage(limit);
  }

  //
  // Private methods
  //

  private AbstractSerialization getSerialization() {
    if (serialization == null) {
      if (getRepositoryName() == null) {
        throw new IllegalStateException();
      }
      serialization =
          SerializationProvider.getStandardJsonSerialization(chunkLevel.getLionWebVersion());
      serialization.enableDynamicNodes();
      serialization.setAllUnavailabilityPolicies(UnavailableNodePolicy.PROXY_NODES);
    }
    return serialization;
  }

  private List<Node> deserializeNodes(List<SerializedClassifierInstance> serializedNodes) {
    return deserialize(serializedNodes).stream()
        .filter(n -> n instanceof Node)
        .map(n -> (Node) n)
        .collect(Collectors.toList());
  }
}
