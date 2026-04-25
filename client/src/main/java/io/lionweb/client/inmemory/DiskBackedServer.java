package io.lionweb.client.inmemory;

import io.lionweb.LionWebVersion;
import io.lionweb.client.api.*;
import io.lionweb.client.delta.CommandSource;
import io.lionweb.client.delta.DeltaChannel;
import io.lionweb.client.delta.DeltaCommandReceiver;
import io.lionweb.client.delta.DeltaQueryReceiver;
import io.lionweb.client.delta.messages.DeltaCommand;
import io.lionweb.client.delta.messages.DeltaQuery;
import io.lionweb.client.delta.messages.DeltaQueryResponse;
import io.lionweb.client.delta.messages.commands.children.AddChild;
import io.lionweb.client.delta.messages.commands.children.DeleteChild;
import io.lionweb.client.delta.messages.commands.properties.ChangeProperty;
import io.lionweb.client.delta.messages.commands.references.AddReference;
import io.lionweb.client.delta.messages.events.ErrorEvent;
import io.lionweb.client.delta.messages.events.StandardErrorCode;
import io.lionweb.client.delta.messages.events.children.ChildAdded;
import io.lionweb.client.delta.messages.events.children.ChildDeleted;
import io.lionweb.client.delta.messages.events.properties.PropertyChanged;
import io.lionweb.client.delta.messages.events.references.ReferenceAdded;
import io.lionweb.client.delta.messages.queries.partitcipations.SignOnRequest;
import io.lionweb.client.delta.messages.queries.partitcipations.SignOnResponse;
import io.lionweb.model.Node;
import io.lionweb.serialization.AbstractSerialization;
import io.lionweb.serialization.data.SerializationChunk;
import io.lionweb.serialization.data.SerializedClassifierInstance;
import io.lionweb.serialization.data.SerializedReferenceValue;
import io.lionweb.utils.ValidationResult;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A disk-backed server that keeps frequently accessed partitions in memory and evicts
 * least-recently-used partitions to disk as JSON files.
 *
 * <p>This has the same public API as {@link InMemoryServer} and is a drop-in replacement when
 * memory usage is a concern. The hot-partition tier provides the same access speed as {@link
 * InMemoryServer}; accessing a cold partition incurs a deserialization cost on first access.
 *
 * <p>The {@code maxHotPartitionsPerRepository} parameter controls how many partitions are kept in
 * memory per repository. Tune this based on available heap and the size of your partitions.
 *
 * <p>Temporary files are written under {@code tempDir}. They are not cleaned up automatically; call
 * {@link #deleteTempFiles()} when the server is no longer needed.
 */
public class DiskBackedServer {

  private static final int DEFAULT_MAX_HOT_PARTITIONS = 10;

  private final Map<String, DiskBackedRepositoryData> repositories = new ConcurrentHashMap<>();
  private final Path tempDir;
  private final int maxHotPartitionsPerRepository;
  private int nextParticipationId = 1;

  public DiskBackedServer(@NotNull Path tempDir, int maxHotPartitionsPerRepository) {
    this.tempDir = tempDir;
    this.maxHotPartitionsPerRepository = maxHotPartitionsPerRepository;
    try {
      Files.createDirectories(tempDir);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public DiskBackedServer(@NotNull Path tempDir) {
    this(tempDir, DEFAULT_MAX_HOT_PARTITIONS);
  }

  public DiskBackedServer() {
    this(createDefaultTempDir());
  }

  // --- Repository management ---

  public @NotNull RepositoryConfiguration getRepositoryConfiguration(
      @NotNull String repositoryName) {
    return getRepository(repositoryName).configuration;
  }

  public @NotNull List<String> ids(@NotNull String repositoryName, int count) {
    if (count < 0) {
      throw new IllegalArgumentException("One can ask for zero or more ids");
    }
    return getRepository(repositoryName).ids(count);
  }

  public @NotNull Set<RepositoryConfiguration> listRepositories() {
    return repositories.values().stream().map(r -> r.configuration).collect(Collectors.toSet());
  }

  public void createRepository(@NotNull RepositoryConfiguration repositoryConfiguration) {
    Objects.requireNonNull(repositoryConfiguration);
    if (repositoryConfiguration.getHistorySupport() == HistorySupport.ENABLED) {
      throw new IllegalArgumentException(
          "The DiskBackedServer does not support History for the time being");
    }
    Path repoDir = tempDir.resolve(sanitize(repositoryConfiguration.getName()));
    repositories.put(
        repositoryConfiguration.getName(),
        new DiskBackedRepositoryData(
            repositoryConfiguration, repoDir, maxHotPartitionsPerRepository));
  }

  public void deleteRepository(@NotNull String repositoryName) {
    Objects.requireNonNull(repositoryName);
    if (!repositories.containsKey(repositoryName)) {
      throw new IllegalArgumentException("Repository not found: " + repositoryName);
    }
    repositories.remove(repositoryName);
  }

  // --- Partition operations ---

  public @NotNull List<String> listPartitionIDs(@NotNull String repositoryName) {
    Objects.requireNonNull(repositoryName);
    return getRepository(repositoryName).partitionIDs;
  }

  public @NotNull RepositoryVersionToken createPartitionFromChunk(
      @NotNull String repositoryName, @NotNull List<SerializedClassifierInstance> partitions) {
    Objects.requireNonNull(partitions);
    DiskBackedRepositoryData repoData = getRepository(repositoryName);
    partitions.stream()
        .filter(n -> n.getParentNodeID() == null)
        .filter(n -> !repoData.partitionIDs.contains(n.getID()))
        .forEach(n -> repoData.addPartition(n.getID(), Collections.emptyList()));
    repoData.store(partitions);
    return repoData.bumpVersion();
  }

  public @NotNull RepositoryVersionToken createPartition(
      @NotNull String repositoryName,
      @NotNull Node partition,
      @NotNull AbstractSerialization serialization) {
    Objects.requireNonNull(repositoryName);
    Objects.requireNonNull(partition);
    Objects.requireNonNull(serialization);
    if (partition.getParent() != null) {
      throw new IllegalArgumentException("Partition should not have a parent");
    }
    SerializationChunk chunk = serialization.serializeNodesToSerializationChunk(partition);
    return createPartitionFromChunk(repositoryName, chunk.getClassifierInstances());
  }

  public @NotNull RepositoryVersionToken deletePartitions(
      @NotNull String repositoryName, @NotNull List<String> partitionIds) {
    Objects.requireNonNull(partitionIds);
    DiskBackedRepositoryData repoData = getRepository(repositoryName);
    partitionIds.forEach(repoData::deletePartition);
    return repoData.bumpVersion();
  }

  // --- Node retrieval and storage ---

  public List<SerializedClassifierInstance> retrieve(
      @NotNull String repositoryName, List<String> nodeIds, int limit) {
    Objects.requireNonNull(repositoryName);
    DiskBackedRepositoryData repoData = getRepository(repositoryName);
    List<SerializedClassifierInstance> retrieved = new ArrayList<>();
    nodeIds.forEach(id -> repoData.retrieve(id, limit, retrieved));
    return retrieved;
  }

  public @Nullable io.lionweb.model.ClassifierInstance<?> retrieveAsClassifierInstance(
      @NotNull String repositoryName,
      @NotNull String nodeId,
      @NotNull AbstractSerialization serialization) {
    Objects.requireNonNull(repositoryName);
    Objects.requireNonNull(nodeId);
    Objects.requireNonNull(serialization);
    List<SerializedClassifierInstance> serializedNodes =
        retrieve(repositoryName, Arrays.asList(nodeId), 1);
    if (serializedNodes.isEmpty()) {
      return null;
    }
    LionWebVersion lionWebVersion = getRepository(repositoryName).configuration.getLionWebVersion();
    List<io.lionweb.model.ClassifierInstance<?>> nodes =
        serialization.deserializeSerializationChunk(
            SerializationChunk.fromNodes(lionWebVersion, serializedNodes));
    return nodes.stream().filter(n -> Objects.equals(n.getID(), nodeId)).findFirst().orElse(null);
  }

  public RepositoryVersionToken store(
      @NotNull String repositoryName, @NotNull List<SerializedClassifierInstance> nodes) {
    Objects.requireNonNull(repositoryName);
    DiskBackedRepositoryData repoData = getRepository(repositoryName);
    repoData.store(nodes);
    return repoData.bumpVersion();
  }

  // --- Inspection ---

  public Map<ClassifierKey, ClassifierResult> nodesByClassifier(@NotNull String repositoryName) {
    return nodesByClassifier(repositoryName, Integer.MAX_VALUE);
  }

  public Map<ClassifierKey, ClassifierResult> nodesByClassifier(
      @NotNull String repositoryName, @Nullable Integer limit) {
    return getRepository(repositoryName).nodesByClassifier(limit);
  }

  public Map<String, ClassifierResult> nodesByLanguage(@NotNull String repositoryName) {
    return nodesByLanguage(repositoryName, Integer.MAX_VALUE);
  }

  public Map<String, ClassifierResult> nodesByLanguage(
      @NotNull String repositoryName, @Nullable Integer limit) {
    return getRepository(repositoryName).nodesByLanguage(limit);
  }

  public @NotNull ValidationResult checkConsistency() {
    ValidationResult result = new ValidationResult();
    for (DiskBackedRepositoryData repoData : repositories.values()) {
      result.getIssues().addAll(repoData.checkConsistency().getIssues());
    }
    return result;
  }

  // --- Delta channel ---

  public void monitorDeltaChannel(String repositoryName, @NotNull DeltaChannel channel) {
    Objects.requireNonNull(channel);
    channel.registerCommandReceiver(new DeltaCommandReceiverImpl(repositoryName, channel));
    channel.registerQueryReceiver(new DeltaQueryReceiverImpl(repositoryName, channel));
  }

  // --- Cleanup ---

  /** Deletes all temporary partition files written to disk. */
  public void deleteTempFiles() {
    try {
      if (Files.exists(tempDir)) {
        try (Stream<Path> walk = Files.walk(tempDir)) {
          walk.sorted(Comparator.reverseOrder())
              .forEach(
                  p -> {
                    try {
                      Files.deleteIfExists(p);
                    } catch (IOException e) {
                      // best-effort cleanup
                    }
                  });
        }
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  // --- Private ---

  private @NotNull DiskBackedRepositoryData getRepository(@NotNull String repositoryName) {
    Objects.requireNonNull(repositoryName);
    DiskBackedRepositoryData repoData = repositories.get(repositoryName);
    if (repoData == null) {
      throw new IllegalArgumentException("Cannot find repository named " + repositoryName);
    }
    return repoData;
  }

  private static Path createDefaultTempDir() {
    try {
      return Files.createTempDirectory("lionweb-diskbacked-");
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private static String sanitize(String name) {
    return name.replaceAll("[^a-zA-Z0-9._-]", "_");
  }

  // --- Delta inner classes ---

  private class DeltaQueryReceiverImpl implements DeltaQueryReceiver {
    private final String repositoryName;
    private final DeltaChannel channel;

    DeltaQueryReceiverImpl(String repositoryName, DeltaChannel channel) {
      this.repositoryName = repositoryName;
      this.channel = channel;
    }

    @Override
    public DeltaQueryResponse receiveQuery(DeltaQuery query) {
      if (query instanceof SignOnRequest) {
        SignOnRequest signOnRequest = (SignOnRequest) query;
        return new SignOnResponse(signOnRequest.queryId, "participation-" + nextParticipationId++);
      }
      throw new UnsupportedOperationException("Not supported yet.");
    }
  }

  private class DeltaCommandReceiverImpl implements DeltaCommandReceiver {
    private final String repositoryName;
    private final DeltaChannel channel;

    DeltaCommandReceiverImpl(String repositoryName, DeltaChannel channel) {
      this.repositoryName = repositoryName;
      this.channel = channel;
    }

    @Override
    public void receiveCommand(String participationId, DeltaCommand command) {
      CommandSource source = new CommandSource(participationId, command.commandId);
      if (command instanceof ChangeProperty) {
        ChangeProperty changeProperty = (ChangeProperty) command;
        DiskBackedRepositoryData repoData = getRepository(repositoryName);
        List<SerializedClassifierInstance> retrieved = new ArrayList<>();
        try {
          repoData.retrieve(changeProperty.node, 0, retrieved);
        } catch (IllegalArgumentException e) {
          channel.sendEvent(
              seq ->
                  new ErrorEvent(
                      seq,
                      StandardErrorCode.UNKNOWN_NODE,
                      "Node with id " + changeProperty.node + " not found"));
          return;
        }
        SerializedClassifierInstance node = retrieved.get(0);
        String oldValue = node.getPropertyValue(changeProperty.property);
        node.setPropertyValue(changeProperty.property, changeProperty.newValue);
        String newValue = node.getPropertyValue(changeProperty.property);
        // Ensure the partition is marked dirty after in-place mutation
        repoData.hotRepositoryDataForNode(changeProperty.node);
        channel.sendEvent(
            seq ->
                new PropertyChanged(seq, node.getID(), changeProperty.property, newValue, oldValue)
                    .addSource(source));
        return;
      } else if (command instanceof AddChild) {
        AddChild addChild = (AddChild) command;
        DiskBackedRepositoryData repoData = getRepository(repositoryName);
        List<SerializedClassifierInstance> retrieved = new ArrayList<>();
        try {
          repoData.retrieve(addChild.parent, 0, retrieved);
        } catch (IllegalArgumentException e) {
          channel.sendEvent(
              seq ->
                  new ErrorEvent(
                      seq,
                      StandardErrorCode.UNKNOWN_NODE,
                      "Node with id " + addChild.parent + " not found"));
          return;
        }
        SerializedClassifierInstance node = retrieved.get(0);
        repoData.store(addChild.newChild.getClassifierInstances());
        String childId =
            addChild.newChild.getClassifierInstances().stream()
                .filter(n -> addChild.parent.equals(n.getParentNodeID()))
                .findFirst()
                .get()
                .getID();
        node.addChild(addChild.containment, childId, addChild.index);
        repoData.hotRepositoryDataForNode(addChild.parent);
        channel.sendEvent(
            seq ->
                new ChildAdded(
                        seq,
                        addChild.parent,
                        addChild.newChild,
                        addChild.containment,
                        addChild.index)
                    .addSource(source));
        return;
      } else if (command instanceof DeleteChild) {
        DeleteChild deleteChild = (DeleteChild) command;
        DiskBackedRepositoryData repoData = getRepository(repositoryName);
        List<SerializedClassifierInstance> retrieved = new ArrayList<>();
        try {
          repoData.retrieve(deleteChild.parent, 0, retrieved);
        } catch (IllegalArgumentException e) {
          channel.sendEvent(
              seq ->
                  new ErrorEvent(
                      seq,
                      StandardErrorCode.UNKNOWN_NODE,
                      "Node with id " + deleteChild.parent + " not found"));
          return;
        }
        channel.sendEvent(
            seq ->
                new ChildDeleted(
                        seq,
                        deleteChild.parent,
                        deleteChild.containment,
                        deleteChild.index,
                        deleteChild.deletedChild)
                    .addSource(source));
        return;
      } else if (command instanceof AddReference) {
        AddReference addReference = (AddReference) command;
        DiskBackedRepositoryData repoData = getRepository(repositoryName);
        List<SerializedClassifierInstance> retrieved = new ArrayList<>();
        try {
          repoData.retrieve(addReference.parent, 0, retrieved);
        } catch (IllegalArgumentException e) {
          channel.sendEvent(
              seq ->
                  new ErrorEvent(
                      seq,
                      StandardErrorCode.UNKNOWN_NODE,
                      "Node with id " + addReference.parent + " not found"));
          return;
        }
        SerializedClassifierInstance node = retrieved.get(0);
        node.addReferenceValue(
            addReference.reference,
            addReference.index,
            new SerializedReferenceValue.Entry(
                addReference.newTarget, addReference.newResolveInfo));
        repoData.hotRepositoryDataForNode(addReference.parent);
        channel.sendEvent(
            seq ->
                new ReferenceAdded(
                        seq,
                        addReference.parent,
                        addReference.reference,
                        addReference.index,
                        addReference.newTarget,
                        addReference.newResolveInfo)
                    .addSource(source));
        return;
      }
      throw new UnsupportedOperationException(
          "Unsupported command type: " + command.getClass().getName());
    }
  }
}
