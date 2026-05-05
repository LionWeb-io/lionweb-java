package io.lionweb.client.partitioned;

import io.lionweb.LionWebVersion;
import io.lionweb.client.api.*;
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

/**
 * A client for {@link PartitionedServer} that operates on {@link Node} objects.
 *
 * <p>This is the partition-oriented equivalent of {@code NodesLevelInMemoryServerClient}. It wraps
 * a {@link ChunkLevelPartitionedServerClient} and uses {@link AbstractSerialization} for converting
 * between nodes and serialised chunks.
 */
public class NodesLevelPartitionedServerClient
    implements BulkAPIClient, DBAdminAPIClient, InspectionAPIClient {

  private final @NotNull ChunkLevelPartitionedServerClient chunkLevel;
  private @Nullable AbstractSerialization serialization;

  public NodesLevelPartitionedServerClient(@NotNull PartitionedServer partitionedServer) {
    this.chunkLevel = new ChunkLevelPartitionedServerClient(partitionedServer);
  }

  public NodesLevelPartitionedServerClient(
      @NotNull PartitionedServer partitionedServer, @NotNull String repositoryName) {
    Objects.requireNonNull(repositoryName);
    this.chunkLevel = new ChunkLevelPartitionedServerClient(partitionedServer, repositoryName);
  }

  @Nullable
  public String getRepositoryName() {
    return chunkLevel.getRepositoryName();
  }

  // -------------------------------------------------------------------------
  // DBAdminAPIClient
  // -------------------------------------------------------------------------

  @Override
  public void createDatabase() {
    // No-op
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

  // -------------------------------------------------------------------------
  // BulkAPIClient
  // -------------------------------------------------------------------------

  @NotNull
  @Override
  public LionWebVersion getLionWebVersion() {
    return chunkLevel.getLionWebVersion();
  }

  @Override
  public List<String> ids(int count) {
    return chunkLevel.ids(count);
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

  @Override
  public List<String> listPartitionsIDs() {
    return chunkLevel.listPartitionsIDs();
  }

  @Nullable
  @Override
  public RepositoryVersionToken createPartitions(List<Node> partitions) throws IOException {
    AbstractSerialization ser = getSerialization();
    return chunkLevel.createPartitionsFromChunk(
        ser.serializeTreesToSerializationChunk(partitions).getClassifierInstances());
  }

  @Nullable
  @Override
  public RepositoryVersionToken deletePartitions(List<String> ids) throws IOException {
    return chunkLevel.deletePartitions(ids);
  }

  @Nullable
  @Override
  public RepositoryVersionToken store(List<Node> nodes) throws IOException {
    return chunkLevel.storeChunk(
        getSerialization().serializeTreesToSerializationChunk(nodes).getClassifierInstances());
  }

  @Override
  public List<Node> retrieve(List<String> nodeIds, int limit) throws IOException {
    List<SerializedClassifierInstance> serialized = chunkLevel.retrieveAsChunk(nodeIds, limit);
    return deserializeNodes(serialized).stream()
        .filter(n -> nodeIds.contains(n.getID()))
        .collect(Collectors.toList());
  }

  @Override
  public List<Node> retrieve(List<String> nodeIds) throws IOException {
    List<SerializedClassifierInstance> serialized = chunkLevel.retrieveAsChunk(nodeIds);
    return deserializeNodes(serialized);
  }

  // -------------------------------------------------------------------------
  // InspectionAPIClient
  // -------------------------------------------------------------------------

  @Override
  public Map<ClassifierKey, ClassifierResult> nodesByClassifier(@Nullable Integer limit) {
    return chunkLevel.nodesByClassifier(limit);
  }

  @Override
  public Map<String, ClassifierResult> nodesByLanguage(@Nullable Integer limit) {
    return chunkLevel.nodesByLanguage(limit);
  }

  // -------------------------------------------------------------------------
  // Private helpers
  // -------------------------------------------------------------------------

  private AbstractSerialization getSerialization() {
    if (serialization == null) {
      if (getRepositoryName() == null) throw new IllegalStateException("No repository set");
      serialization =
          SerializationProvider.getStandardJsonSerialization(chunkLevel.getLionWebVersion());
      serialization.enableDynamicNodes();
      serialization.setAllUnavailabilityPolicies(UnavailableNodePolicy.PROXY_NODES);
    }
    return serialization;
  }

  private List<Node> deserializeNodes(List<SerializedClassifierInstance> serialized) {
    SerializationChunk chunk = new SerializationChunk();
    chunk.setSerializationFormatVersion(getLionWebVersion().getVersionString());
    serialized.forEach(chunk::addClassifierInstance);
    chunk.populateUsedLanguages();
    return getSerialization().deserializeSerializationChunk(chunk).stream()
        .filter(n -> n instanceof Node)
        .map(n -> (Node) n)
        .collect(Collectors.toList());
  }
}
