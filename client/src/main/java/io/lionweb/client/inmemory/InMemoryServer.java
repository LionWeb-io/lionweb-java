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
import io.lionweb.client.delta.messages.commands.ChangeClassifier;
import io.lionweb.client.delta.messages.commands.annotations.AddAnnotation;
import io.lionweb.client.delta.messages.commands.annotations.DeleteAnnotation;
import io.lionweb.client.delta.messages.commands.annotations.MoveAnnotationFromOtherParent;
import io.lionweb.client.delta.messages.commands.annotations.MoveAnnotationInSameParent;
import io.lionweb.client.delta.messages.commands.annotations.ReplaceAnnotation;
import io.lionweb.client.delta.messages.commands.children.AddChild;
import io.lionweb.client.delta.messages.commands.children.DeleteChild;
import io.lionweb.client.delta.messages.commands.children.MoveChildFromOtherContainment;
import io.lionweb.client.delta.messages.commands.children.MoveChildFromOtherContainmentInSameParent;
import io.lionweb.client.delta.messages.commands.children.MoveChildInSameContainment;
import io.lionweb.client.delta.messages.commands.children.ReplaceChild;
import io.lionweb.client.delta.messages.commands.partitions.AddPartition;
import io.lionweb.client.delta.messages.commands.partitions.DeletePartition;
import io.lionweb.client.delta.messages.commands.properties.AddProperty;
import io.lionweb.client.delta.messages.commands.properties.ChangeProperty;
import io.lionweb.client.delta.messages.commands.properties.DeleteProperty;
import io.lionweb.client.delta.messages.commands.references.AddReference;
import io.lionweb.client.delta.messages.commands.references.ChangeReference;
import io.lionweb.client.delta.messages.commands.references.DeleteReference;
import io.lionweb.client.delta.messages.events.ClassifierChanged;
import io.lionweb.client.delta.messages.events.ErrorEvent;
import io.lionweb.client.delta.messages.events.StandardErrorCode;
import io.lionweb.client.delta.messages.events.annotations.AnnotationAdded;
import io.lionweb.client.delta.messages.events.annotations.AnnotationDeleted;
import io.lionweb.client.delta.messages.events.annotations.AnnotationMovedFromOtherParent;
import io.lionweb.client.delta.messages.events.annotations.AnnotationMovedInSameParent;
import io.lionweb.client.delta.messages.events.annotations.AnnotationReplaced;
import io.lionweb.client.delta.messages.events.children.ChildAdded;
import io.lionweb.client.delta.messages.events.children.ChildDeleted;
import io.lionweb.client.delta.messages.events.children.ChildMovedFromOtherContainment;
import io.lionweb.client.delta.messages.events.children.ChildMovedFromOtherContainmentInSameParent;
import io.lionweb.client.delta.messages.events.children.ChildMovedInSameContainment;
import io.lionweb.client.delta.messages.events.children.ChildReplaced;
import io.lionweb.client.delta.messages.events.partitions.PartitionAdded;
import io.lionweb.client.delta.messages.events.partitions.PartitionDeleted;
import io.lionweb.client.delta.messages.events.properties.PropertyAdded;
import io.lionweb.client.delta.messages.events.properties.PropertyChanged;
import io.lionweb.client.delta.messages.events.properties.PropertyDeleted;
import io.lionweb.client.delta.messages.events.references.ReferenceAdded;
import io.lionweb.client.delta.messages.events.references.ReferenceChanged;
import io.lionweb.client.delta.messages.events.references.ReferenceDeleted;
import io.lionweb.client.delta.messages.queries.ErrorResponse;
import io.lionweb.client.delta.messages.queries.ListAndSubscribePartitionsRequest;
import io.lionweb.client.delta.messages.queries.ListAndSubscribePartitionsResponse;
import io.lionweb.client.delta.messages.queries.ListPartitionsRequest;
import io.lionweb.client.delta.messages.queries.ListPartitionsResponse;
import io.lionweb.client.delta.messages.queries.partitcipations.ReconnectRequest;
import io.lionweb.client.delta.messages.queries.partitcipations.ReconnectResponse;
import io.lionweb.client.delta.messages.queries.partitcipations.SignOffRequest;
import io.lionweb.client.delta.messages.queries.partitcipations.SignOffResponse;
import io.lionweb.client.delta.messages.queries.partitcipations.SignOnRequest;
import io.lionweb.client.delta.messages.queries.partitcipations.SignOnResponse;
import io.lionweb.client.delta.messages.queries.subscriptions.SubscribeToPartitionContentsRequest;
import io.lionweb.client.delta.messages.queries.subscriptions.SubscribeToPartitionContentsResponse;
import io.lionweb.client.delta.messages.queries.subscriptions.UnsubscribeFromPartitionContentsRequest;
import io.lionweb.client.delta.messages.queries.subscriptions.UnsubscribeFromPartitionContentsResponse;
import io.lionweb.model.ClassifierInstance;
import io.lionweb.model.Node;
import io.lionweb.serialization.AbstractSerialization;
import io.lionweb.serialization.data.SerializationChunk;
import io.lionweb.serialization.data.SerializedClassifierInstance;
import io.lionweb.serialization.data.SerializedReferenceValue;
import io.lionweb.utils.ValidationResult;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An InMemoryServer is useful for testing and as a replacement for a proper server, when performing
 * processing operations.
 *
 * <p>We store data using SerializedClassifierInstance so that: - We do not need to know the
 * languages - We can inspect the nodes while we could not if we stored the data serialized in JSON
 * or binary formats.
 *
 * <p>Different clients can then still work with nodes or JSON or binary formats.
 *
 * <p>Also look at {@link NodesLevelInMemoryServerClient} for easier handling of node storage and
 * retrieval.
 */
public class InMemoryServer {

  /** Internally we store the data separately for each repository. */
  private final Map<String, RepositoryData> repositories = new ConcurrentHashMap<>();

  private final boolean materializeClassifierIndex;

  public InMemoryServer() {
    this(true);
  }

  public InMemoryServer(boolean materializeClassifierIndex) {
    this.materializeClassifierIndex = materializeClassifierIndex;
  }

  private int nextParticipationId = 1;

  /** Participations that are currently active (signed on, not yet signed off). */
  private final Set<String> activeParticipations = Collections.synchronizedSet(new HashSet<>());

  public @NotNull RepositoryConfiguration getRepositoryConfiguration(
      @NotNull String repositoryName) {
    return getRepository(repositoryName).configuration;
  }

  public @NotNull List<String> ids(@NotNull String repositoryName, int count) {
    if (count < 0) {
      throw new IllegalArgumentException("One can ask for zero or more ids");
    }
    RepositoryData repositoryData = getRepository(repositoryName);
    return repositoryData.ids(count);
  }

  public @NotNull Set<RepositoryConfiguration> listRepositories() {
    return repositories.values().stream().map(r -> r.configuration).collect(Collectors.toSet());
  }

  public void createRepository(@NotNull RepositoryConfiguration repositoryConfiguration) {
    Objects.requireNonNull(repositoryConfiguration);
    if (repositoryConfiguration.getHistorySupport() == HistorySupport.ENABLED) {
      throw new IllegalArgumentException(
          "The InMemoryServer does not support History for the time being");
    }
    repositories.put(
        repositoryConfiguration.getName(),
        new RepositoryData(repositoryConfiguration, materializeClassifierIndex));
  }

  public void deleteRepository(@NotNull String repositoryName) {
    Objects.requireNonNull(repositoryName);
    if (!repositories.containsKey(repositoryName)) {
      throw new IllegalArgumentException();
    }
    repositories.remove(repositoryName);
  }

  public @NotNull List<String> listPartitionIDs(@NotNull String repositoryName) {
    Objects.requireNonNull(repositoryName, "RepositoryName should not be null");
    RepositoryData repositoryData = repositories.get(repositoryName);
    return repositoryData.partitionIDs;
  }

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

  public @NotNull RepositoryVersionToken createPartition(
      @NotNull String repositoryName,
      @NotNull Node partition,
      @NotNull AbstractSerialization serialization) {
    Objects.requireNonNull(repositoryName, "RepositoryName should not be null");
    Objects.requireNonNull(partition, "Partition should not be null");
    Objects.requireNonNull(serialization, "Serialization should not be null");
    if (partition.getParent() != null) {
      throw new IllegalArgumentException("Partition should not have a parent");
    }

    SerializationChunk serializationChunk =
        serialization.serializeNodesToSerializationChunk(partition);
    return createPartitionFromChunk(repositoryName, serializationChunk.getClassifierInstances());
  }

  public @NotNull RepositoryVersionToken deletePartitions(
      @NotNull String repositoryName, @NotNull List<String> partitionIds) {
    Objects.requireNonNull(partitionIds);
    RepositoryData repositoryData = getRepository(repositoryName);
    repositoryData.partitionIDs.removeIf(partitionIds::contains);
    partitionIds.forEach(repositoryData::deleteNodeAndDescendant);
    return repositoryData.bumpVersion();
  }

  public List<SerializedClassifierInstance> retrieve(
      @NotNull String repositoryName, List<String> nodeIds, int limit) {
    Objects.requireNonNull(repositoryName, "RepositoryName should not be null");
    RepositoryData repositoryData = repositories.get(repositoryName);
    List<SerializedClassifierInstance> retrieved = new ArrayList<>();
    nodeIds.forEach(n -> repositoryData.retrieve(n, limit, retrieved));
    return retrieved;
  }

  public @Nullable ClassifierInstance<?> retrieveAsClassifierInstance(
      @NotNull String repositoryName,
      @NotNull String nodeId,
      @NotNull AbstractSerialization serialization) {
    Objects.requireNonNull(repositoryName, "RepositoryName should not be null");
    Objects.requireNonNull(nodeId, "NodeId should not be null");
    Objects.requireNonNull(serialization, "Serialization should not be null");
    List<SerializedClassifierInstance> serializedNodes =
        retrieve(repositoryName, List.of(nodeId), 1);
    if (serializedNodes.isEmpty()) {
      return null;
    }
    LionWebVersion lionWebVersion =
        repositories.get(repositoryName).configuration.getLionWebVersion();
    List<ClassifierInstance<?>> nodes =
        serialization.deserializeSerializationChunk(
            SerializationChunk.fromNodes(lionWebVersion, serializedNodes));
    return nodes.stream().filter(n -> Objects.equals(n.getID(), nodeId)).findFirst().orElse(null);
  }

  /**
   * @param nodes {@link io.lionweb.serialization.LowLevelJsonSerialization} can produce {@link
   *     SerializedClassifierInstance} nodes, if we need to store data from JSON files.
   */
  public RepositoryVersionToken store(
      @NotNull String repositoryName, @NotNull List<SerializedClassifierInstance> nodes) {
    Objects.requireNonNull(repositoryName, "RepositoryName should not be null");
    RepositoryData repositoryData = repositories.get(repositoryName);
    repositoryData.store(nodes);
    return repositoryData.bumpVersion();
  }

  //
  // Inspection
  //

  public Map<ClassifierKey, ClassifierResult> nodesByClassifier(@NotNull String repositoryName) {
    return nodesByClassifier(repositoryName, Integer.MAX_VALUE);
  }

  public ClassifierResult nodesByClassifier(@NotNull String repositoryName, ClassifierKey key) {
    return nodesByClassifier(repositoryName, Integer.MAX_VALUE, key);
  }

  public Map<ClassifierKey, ClassifierResult> nodesByClassifier(
      @NotNull String repositoryName, @Nullable Integer limit) {
    RepositoryData repositoryData = getRepository(repositoryName);
    return repositoryData.nodesByClassifier(limit);
  }

  public ClassifierResult nodesByClassifier(
      @NotNull String repositoryName, @Nullable Integer limit, ClassifierKey key) {
    RepositoryData repositoryData = getRepository(repositoryName);
    return repositoryData.nodesByClassifier(limit, key);
  }

  public Map<String, ClassifierResult> nodesByLanguage(@NotNull String repositoryName) {
    return nodesByLanguage(repositoryName, Integer.MAX_VALUE);
  }

  public Map<String, ClassifierResult> nodesByLanguage(
      @NotNull String repositoryName, @Nullable Integer limit) {
    RepositoryData repositoryData = getRepository(repositoryName);
    Map<String, List<SerializedClassifierInstance>> byMetapointer =
        repositoryData.nodesByID.values().stream()
            .collect(Collectors.groupingBy(n -> n.getClassifier().getLanguage()));
    Map<String, ClassifierResult> res = new HashMap<>();
    for (Map.Entry<String, List<SerializedClassifierInstance>> entry : byMetapointer.entrySet()) {
      ClassifierResult cr =
          new ClassifierResult(
              entry.getValue().stream()
                  .limit(limit)
                  .map(n -> n.getID())
                  .collect(Collectors.toSet()),
              entry.getValue().size());
      res.put(entry.getKey(), cr);
    }
    return res;
  }

  /**
   * Checks the consistency of all repositories stored in the system and aggregates any validation
   * issues found into a single {@link ValidationResult}.
   *
   * <p>The method iterates through all repository data, invokes their individual consistency
   * checks, and collects any issues reported into the resulting validation result object.
   *
   * <p>This is intended for debugging purposes.
   *
   * @return a {@link ValidationResult} containing all identified issues, or an empty result if no
   *     issues were found.
   */
  public @NotNull ValidationResult checkConsistency() {
    ValidationResult result = new ValidationResult();
    for (RepositoryData repositoryData : repositories.values()) {
      ValidationResult partial = repositoryData.checkConsistency();
      result.getIssues().addAll(partial.getIssues());
    }
    return result;
  }

  //
  // Delta methods
  //

  public void monitorDeltaChannel(String repositoryName, @NotNull DeltaChannel channel) {
    Objects.requireNonNull(channel, "Channel should not be null");
    channel.registerCommandReceiver(new DeltaCommandReceiverImpl(repositoryName, channel));
    channel.registerQueryReceiver(new DeltaQueryReceiverImpl(repositoryName, channel));
  }

  //
  // Private methods
  //

  private @NotNull RepositoryData getRepository(@NotNull String repositoryName) {
    Objects.requireNonNull(repositoryName, "RepositoryName should not be null");
    RepositoryData repositoryData = repositories.get(repositoryName);
    if (repositoryData == null) {
      throw new IllegalArgumentException("Cannot find repository named " + repositoryName);
    }
    return repositoryData;
  }

  //
  // Private classes
  //

    private class DeltaQueryReceiverImpl implements DeltaQueryReceiver {

        private final String repositoryName;
        private final DeltaChannel channel;

        /** The participationId currently bound to this channel session; null before sign-on. */
        private String currentParticipationId;

        private DeltaQueryReceiverImpl(String repositoryName, DeltaChannel channel) {
            this.repositoryName = repositoryName;
            this.channel = channel;
        }

        @Override
        public DeltaQueryResponse receiveQuery(DeltaQuery query) {
            if (query instanceof SignOnRequest) {
                SignOnRequest signOnRequest = (SignOnRequest) query;
                String participationId = "participation-" + nextParticipationId++;
                activeParticipations.add(participationId);
                currentParticipationId = participationId;
                return new SignOnResponse(signOnRequest.queryId, participationId);
            } else if (query instanceof SignOffRequest) {
                SignOffRequest signOffRequest = (SignOffRequest) query;
                activeParticipations.remove(currentParticipationId);
                currentParticipationId = null;
                return new SignOffResponse(signOffRequest.queryId);
            } else if (query instanceof ReconnectRequest) {
                ReconnectRequest reconnectRequest = (ReconnectRequest) query;
                if (!activeParticipations.contains(reconnectRequest.participationId)) {
                    ErrorResponse error = new ErrorResponse(reconnectRequest.queryId);
                    error.errorCode = StandardErrorCode.INVALID_PARTICIPATION.code;
                    error.message = "Unknown participation: " + reconnectRequest.participationId;
                    return error;
                }
                currentParticipationId = reconnectRequest.participationId;
                return new ReconnectResponse(reconnectRequest.queryId, 0);
            } else if (query instanceof ListPartitionsRequest) {
                ListPartitionsRequest req = (ListPartitionsRequest) query;
                RepositoryData repositoryData = getRepository(repositoryName);
                SerializationChunk chunk = buildPartitionRootsChunk(repositoryData);
                return new ListPartitionsResponse(req.queryId, chunk);
            } else if (query instanceof ListAndSubscribePartitionsRequest) {
                ListAndSubscribePartitionsRequest req = (ListAndSubscribePartitionsRequest) query;
                RepositoryData repositoryData = getRepository(repositoryName);
                SerializationChunk chunk = buildPartitionRootsChunk(repositoryData);
                return new ListAndSubscribePartitionsResponse(req.queryId, chunk, false);
            } else if (query instanceof SubscribeToPartitionContentsRequest) {
                SubscribeToPartitionContentsRequest req = (SubscribeToPartitionContentsRequest) query;
                RepositoryData repositoryData = getRepository(repositoryName);
                List<SerializedClassifierInstance> nodes = new ArrayList<>();
                repositoryData.retrieve(req.partition, Integer.MAX_VALUE, nodes);
                LionWebVersion version = repositoryData.configuration.getLionWebVersion();
                SerializationChunk chunk = SerializationChunk.fromNodes(version, nodes);
                return new SubscribeToPartitionContentsResponse(req.queryId, chunk);
            } else if (query instanceof UnsubscribeFromPartitionContentsRequest) {
                UnsubscribeFromPartitionContentsRequest req =
                    (UnsubscribeFromPartitionContentsRequest) query;
                return new UnsubscribeFromPartitionContentsResponse(req.queryId);
            }
            throw new UnsupportedOperationException("Not supported yet.");
        }

        private @NotNull SerializationChunk buildPartitionRootsChunk(
            @NotNull RepositoryData repositoryData) {
            List<SerializedClassifierInstance> roots =
                repositoryData.partitionIDs.stream()
                    .map(id -> repositoryData.nodesByID.get(id))
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toList());
            if (roots.isEmpty()) {
                SerializationChunk empty = new SerializationChunk();
                empty.setSerializationFormatVersion(
                    repositoryData.configuration.getLionWebVersion().getVersionString());
                return empty;
            }
            return SerializationChunk.fromNodes(repositoryData.configuration.getLionWebVersion(), roots);
        }
    }

    private class DeltaCommandReceiverImpl implements DeltaCommandReceiver {
        private final String repositoryName;
        private final DeltaChannel channel;

        private DeltaCommandReceiverImpl(String repositoryName, DeltaChannel channel) {
            this.repositoryName = repositoryName;
            this.channel = channel;
        }

        @Override
        public void receiveCommand(String participationId, DeltaCommand command) {
            if (!activeParticipations.contains(participationId)) {
                channel.sendEvent(
                    sequenceNumber ->
                        new ErrorEvent(
                            sequenceNumber,
                            StandardErrorCode.INVALID_PARTICIPATION,
                            "Invalid participation: " + participationId));
                return;
            }
            CommandSource source = new CommandSource(participationId, command.commandId);
            RepositoryData data = getRepository(repositoryName);
            try {
                if (command instanceof ChangeProperty)
                    handleChangeProperty((ChangeProperty) command, data, source);
                else if (command instanceof AddChild) handleAddChild((AddChild) command, data, source);
                else if (command instanceof DeleteChild)
                    handleDeleteChild((DeleteChild) command, data, source);
                else if (command instanceof AddReference)
                    handleAddReference((AddReference) command, data, source);
                else if (command instanceof AddAnnotation)
                    handleAddAnnotation((AddAnnotation) command, data, source);
                else if (command instanceof DeleteAnnotation)
                    handleDeleteAnnotation((DeleteAnnotation) command, data, source);
                else if (command instanceof MoveAnnotationInSameParent)
                    handleMoveAnnotationInSameParent((MoveAnnotationInSameParent) command, data, source);
                else if (command instanceof MoveAnnotationFromOtherParent)
                    handleMoveAnnotationFromOtherParent(
                        (MoveAnnotationFromOtherParent) command, data, source);
                else if (command instanceof ReplaceAnnotation)
                    handleReplaceAnnotation((ReplaceAnnotation) command, data, source);
                else if (command instanceof MoveChildInSameContainment)
                    handleMoveChildInSameContainment((MoveChildInSameContainment) command, data, source);
                else if (command instanceof MoveChildFromOtherContainmentInSameParent)
                    handleMoveChildFromOtherContainmentInSameParent(
                        (MoveChildFromOtherContainmentInSameParent) command, data, source);
                else if (command instanceof MoveChildFromOtherContainment)
                    handleMoveChildFromOtherContainment(
                        (MoveChildFromOtherContainment) command, data, source);
                else if (command instanceof ReplaceChild)
                    handleReplaceChild((ReplaceChild) command, data, source);
                else if (command instanceof AddProperty)
                    handleAddProperty((AddProperty) command, data, source);
                else if (command instanceof DeleteProperty)
                    handleDeleteProperty((DeleteProperty) command, data, source);
                else if (command instanceof ChangeReference)
                    handleChangeReference((ChangeReference) command, data, source);
                else if (command instanceof DeleteReference)
                    handleDeleteReference((DeleteReference) command, data, source);
                else if (command instanceof ChangeClassifier)
                    handleChangeClassifier((ChangeClassifier) command, data, source);
                else if (command instanceof AddPartition)
                    handleAddPartition((AddPartition) command, source);
                else if (command instanceof DeletePartition)
                    handleDeletePartition((DeletePartition) command, data, source);
                else
                    throw new UnsupportedOperationException(
                        "Unsupported command type: " + command.getClass().getName());
            } catch (NodeNotFoundException e) {
                String msg = e.getMessage();
                channel.sendEvent(seqNum -> new ErrorEvent(seqNum, StandardErrorCode.UNKNOWN_NODE, msg));
            }
        }

        private void handleChangeProperty(
            ChangeProperty cmd, RepositoryData data, CommandSource source) {
            SerializedClassifierInstance node = requireNode(data, cmd.node);
            String oldValue = node.getPropertyValue(cmd.property);
            node.setPropertyValue(cmd.property, cmd.newValue);
            String newValue = node.getPropertyValue(cmd.property);
            channel.sendEvent(
                seqNum ->
                    new PropertyChanged(seqNum, node.getID(), cmd.property, newValue, oldValue)
                        .addSource(source));
        }

        private void handleAddChild(AddChild cmd, RepositoryData data, CommandSource source) {
            SerializedClassifierInstance parent = requireNode(data, cmd.parent);
            data.store(cmd.newChild.getClassifierInstances());
            String childId =
                cmd.newChild.getClassifierInstances().stream()
                    .filter(n -> cmd.parent.equals(n.getParentNodeID()))
                    .findFirst()
                    .orElseThrow()
                    .getID();
            parent.addChild(cmd.containment, childId, cmd.index);
            channel.sendEvent(
                seqNum ->
                    new ChildAdded(seqNum, cmd.parent, cmd.newChild, cmd.containment, cmd.index)
                        .addSource(source));
        }

        private void handleDeleteChild(DeleteChild cmd, RepositoryData data, CommandSource source) {
            requireNode(data, cmd.parent);
            channel.sendEvent(
                seqNum ->
                    new ChildDeleted(
                        seqNum,
                        cmd.parent,
                        cmd.deletedChild,
                        Collections.emptyList(),
                        cmd.index,
                        cmd.containment)
                        .addSource(source));
        }

        private void handleAddReference(AddReference cmd, RepositoryData data, CommandSource source) {
            SerializedClassifierInstance node = requireNode(data, cmd.parent);
            node.addReferenceValue(
                cmd.reference,
                cmd.index,
                new SerializedReferenceValue.Entry(cmd.newReference, cmd.newResolveInfo));
            channel.sendEvent(
                seqNum ->
                    new ReferenceAdded(
                        seqNum,
                        cmd.parent,
                        cmd.reference,
                        cmd.index,
                        cmd.newReference,
                        cmd.newResolveInfo)
                        .addSource(source));
        }

        private void handleAddAnnotation(
            @NotNull AddAnnotation cmd, @NotNull RepositoryData data, @NotNull CommandSource source) {
            SerializedClassifierInstance parent = requireNode(data, cmd.parent);
            Set<String> chunkIds =
                cmd.newAnnotation.getClassifierInstances().stream()
                    .map(SerializedClassifierInstance::getID)
                    .collect(Collectors.toSet());
            SerializedClassifierInstance annotationRoot =
                cmd.newAnnotation.getClassifierInstances().stream()
                    .filter(n -> n.getParentNodeID() == null || !chunkIds.contains(n.getParentNodeID()))
                    .findFirst()
                    .orElseThrow(() -> new NodeNotFoundException("annotation root not found in chunk"));
            annotationRoot.setParentNodeID(cmd.parent);
            data.store(cmd.newAnnotation.getClassifierInstances());
            List<String> annotations = new ArrayList<>(parent.getAnnotations());
            annotations.add(cmd.index, annotationRoot.getID());
            parent.setAnnotations(annotations);
            channel.sendEvent(
                seqNum ->
                    new AnnotationAdded(seqNum, cmd.parent, cmd.newAnnotation, cmd.index)
                        .addSource(source));
        }

        private void handleDeleteAnnotation(
            @NotNull DeleteAnnotation cmd,
            @NotNull RepositoryData data,
            @NotNull CommandSource source) {
            SerializedClassifierInstance parent = requireNode(data, cmd.parent);
            List<String> descendants = new ArrayList<>();
            collectDescendants(data, cmd.deletedAnnotation, descendants);
            List<String> annotations = new ArrayList<>(parent.getAnnotations());
            annotations.remove(cmd.index);
            parent.setAnnotations(annotations);
            data.deleteNodeAndDescendant(cmd.deletedAnnotation);
            channel.sendEvent(
                seqNum ->
                    new AnnotationDeleted(
                        seqNum,
                        cmd.deletedAnnotation,
                        descendants.toArray(new String[0]),
                        cmd.parent,
                        cmd.index)
                        .addSource(source));
        }

        private void handleMoveAnnotationInSameParent(
            @NotNull MoveAnnotationInSameParent cmd,
            @NotNull RepositoryData data,
            @NotNull CommandSource source) {
            SerializedClassifierInstance parent = requireNode(data, cmd.parent);
            List<String> annotations = new ArrayList<>(parent.getAnnotations());
            String id = annotations.remove(cmd.oldIndex);
            annotations.add(cmd.newIndex, id);
            parent.setAnnotations(annotations);
            channel.sendEvent(
                seqNum ->
                    new AnnotationMovedInSameParent(
                        seqNum, cmd.newIndex, cmd.movedAnnotation, cmd.parent, cmd.oldIndex)
                        .addSource(source));
        }

        private void handleMoveAnnotationFromOtherParent(
            @NotNull MoveAnnotationFromOtherParent cmd,
            @NotNull RepositoryData data,
            @NotNull CommandSource source) {
            SerializedClassifierInstance oldParent = requireNode(data, cmd.oldParent);
            SerializedClassifierInstance newParent = requireNode(data, cmd.newParent);
            SerializedClassifierInstance annotation = requireNode(data, cmd.movedAnnotation);
            List<String> oldAnnotations = new ArrayList<>(oldParent.getAnnotations());
            oldAnnotations.remove(cmd.oldIndex);
            oldParent.setAnnotations(oldAnnotations);
            annotation.setParentNodeID(cmd.newParent);
            List<String> newAnnotations = new ArrayList<>(newParent.getAnnotations());
            newAnnotations.add(cmd.newIndex, cmd.movedAnnotation);
            newParent.setAnnotations(newAnnotations);
            channel.sendEvent(
                seqNum ->
                    new AnnotationMovedFromOtherParent(
                        seqNum,
                        cmd.newParent,
                        cmd.newIndex,
                        cmd.movedAnnotation,
                        cmd.oldParent,
                        cmd.oldIndex)
                        .addSource(source));
        }

        private void handleReplaceAnnotation(
            @NotNull ReplaceAnnotation cmd,
            @NotNull RepositoryData data,
            @NotNull CommandSource source) {
            SerializedClassifierInstance parent = requireNode(data, cmd.parent);
            List<String> descendants = new ArrayList<>();
            collectDescendants(data, cmd.replacedAnnotation, descendants);
            Set<String> chunkIds =
                cmd.newAnnotation.getClassifierInstances().stream()
                    .map(SerializedClassifierInstance::getID)
                    .collect(Collectors.toSet());
            SerializedClassifierInstance newRoot =
                cmd.newAnnotation.getClassifierInstances().stream()
                    .filter(n -> n.getParentNodeID() == null || !chunkIds.contains(n.getParentNodeID()))
                    .findFirst()
                    .orElseThrow(() -> new NodeNotFoundException("new annotation root not found"));
            newRoot.setParentNodeID(cmd.parent);
            List<String> annotations = new ArrayList<>(parent.getAnnotations());
            annotations.remove(cmd.index);
            parent.setAnnotations(annotations);
            data.deleteNodeAndDescendant(cmd.replacedAnnotation);
            data.store(cmd.newAnnotation.getClassifierInstances());
            annotations.add(cmd.index, newRoot.getID());
            parent.setAnnotations(annotations);
            channel.sendEvent(
                seqNum ->
                    new AnnotationReplaced(
                        seqNum,
                        cmd.newAnnotation,
                        cmd.replacedAnnotation,
                        descendants.toArray(new String[0]),
                        cmd.parent,
                        cmd.index)
                        .addSource(source));
        }

        private void handleMoveChildInSameContainment(
            @NotNull MoveChildInSameContainment cmd,
            @NotNull RepositoryData data,
            @NotNull CommandSource source) {
            SerializedClassifierInstance parent = requireNode(data, cmd.parent);
            parent.removeChild(cmd.movedChild);
            parent.addChild(cmd.containment, cmd.movedChild, cmd.newIndex);
            channel.sendEvent(
                seqNum ->
                    new ChildMovedInSameContainment(
                        seqNum,
                        cmd.newIndex,
                        cmd.movedChild,
                        cmd.parent,
                        cmd.containment,
                        cmd.oldIndex)
                        .addSource(source));
        }

        private void handleMoveChildFromOtherContainmentInSameParent(
            @NotNull MoveChildFromOtherContainmentInSameParent cmd,
            @NotNull RepositoryData data,
            @NotNull CommandSource source) {
            SerializedClassifierInstance parent = requireNode(data, cmd.parent);
            parent.removeChild(cmd.movedChild);
            parent.addChild(cmd.newContainment, cmd.movedChild, cmd.newIndex);
            channel.sendEvent(
                seqNum -> {
                    ChildMovedFromOtherContainmentInSameParent event =
                        new ChildMovedFromOtherContainmentInSameParent(
                            seqNum, cmd.newContainment, cmd.newIndex, cmd.movedChild);
                    event.parent = cmd.parent;
                    event.oldContainment = cmd.oldContainment;
                    event.oldIndex = cmd.oldIndex;
                    return event.addSource(source);
                });
        }

        private void handleMoveChildFromOtherContainment(
            @NotNull MoveChildFromOtherContainment cmd,
            @NotNull RepositoryData data,
            @NotNull CommandSource source) {
            SerializedClassifierInstance oldParent = requireNode(data, cmd.oldParent);
            SerializedClassifierInstance newParent = requireNode(data, cmd.newParent);
            SerializedClassifierInstance child = requireNode(data, cmd.movedChild);
            oldParent.removeChild(cmd.movedChild);
            child.setParentNodeID(cmd.newParent);
            newParent.addChild(cmd.newContainment, cmd.movedChild, cmd.newIndex);
            channel.sendEvent(
                seqNum -> {
                    ChildMovedFromOtherContainment event =
                        new ChildMovedFromOtherContainment(
                            seqNum, cmd.newParent, cmd.newContainment, cmd.newIndex, cmd.movedChild);
                    event.oldParent = cmd.oldParent;
                    event.oldContainment = cmd.oldContainment;
                    event.oldIndex = cmd.oldIndex;
                    return event.addSource(source);
                });
        }

        private void handleReplaceChild(
            @NotNull ReplaceChild cmd, @NotNull RepositoryData data, @NotNull CommandSource source) {
            SerializedClassifierInstance parent = requireNode(data, cmd.parent);
            List<String> replacedDescendants = new ArrayList<>();
            collectDescendants(data, cmd.replacedChild, replacedDescendants);
            parent.removeChild(cmd.replacedChild);
            data.deleteNodeAndDescendant(cmd.replacedChild);
            // Find the root of the new child subtree: the node whose parent is NOT inside this chunk.
            Set<String> chunkIds =
                cmd.newChild.getClassifierInstances().stream()
                    .map(SerializedClassifierInstance::getID)
                    .collect(Collectors.toSet());
            SerializedClassifierInstance newChildRoot =
                cmd.newChild.getClassifierInstances().stream()
                    .filter(n -> n.getParentNodeID() == null || !chunkIds.contains(n.getParentNodeID()))
                    .findFirst()
                    .orElseThrow(() -> new NodeNotFoundException("root of replacement chunk not found"));
            newChildRoot.setParentNodeID(cmd.parent);
            data.store(cmd.newChild.getClassifierInstances());
            parent.addChild(cmd.containment, newChildRoot.getID(), cmd.index);
            channel.sendEvent(
                seqNum ->
                    new ChildReplaced(
                        seqNum,
                        cmd.parent,
                        cmd.newChild,
                        cmd.replacedChild,
                        replacedDescendants,
                        cmd.containment,
                        cmd.index)
                        .addSource(source));
        }

        private void handleAddProperty(
            @NotNull AddProperty cmd, @NotNull RepositoryData data, @NotNull CommandSource source) {
            SerializedClassifierInstance node = requireNode(data, cmd.node);
            node.setPropertyValue(cmd.property, cmd.newValue);
            channel.sendEvent(
                seqNum ->
                    new PropertyAdded(seqNum, cmd.node, cmd.property, cmd.newValue).addSource(source));
        }

        private void handleDeleteProperty(
            @NotNull DeleteProperty cmd, @NotNull RepositoryData data, @NotNull CommandSource source) {
            SerializedClassifierInstance node = requireNode(data, cmd.node);
            String oldValue = node.getPropertyValue(cmd.property);
            node.setPropertyValue(cmd.property, null);
            channel.sendEvent(
                seqNum ->
                    new PropertyDeleted(seqNum, cmd.node, cmd.property, oldValue).addSource(source));
        }

        private void handleChangeReference(
            @NotNull ChangeReference cmd, @NotNull RepositoryData data, @NotNull CommandSource source) {
            SerializedClassifierInstance node = requireNode(data, cmd.parent);
            List<SerializedReferenceValue.Entry> entries =
                new ArrayList<>(node.getReferenceValues(cmd.reference));
            entries.set(
                cmd.index, new SerializedReferenceValue.Entry(cmd.newReference, cmd.newResolveInfo));
            node.setReferenceValue(cmd.reference, entries);
            channel.sendEvent(
                seqNum ->
                    new ReferenceChanged(
                        seqNum,
                        cmd.parent,
                        cmd.reference,
                        cmd.index,
                        cmd.newReference,
                        cmd.newResolveInfo,
                        cmd.oldReference,
                        cmd.oldResolveInfo)
                        .addSource(source));
        }

        private void handleDeleteReference(
            @NotNull DeleteReference cmd, @NotNull RepositoryData data, @NotNull CommandSource source) {
            SerializedClassifierInstance node = requireNode(data, cmd.parent);
            List<SerializedReferenceValue.Entry> entries =
                new ArrayList<>(node.getReferenceValues(cmd.reference));
            entries.remove(cmd.index);
            node.setReferenceValue(cmd.reference, entries);
            channel.sendEvent(
                seqNum ->
                    new ReferenceDeleted(
                        seqNum,
                        cmd.parent,
                        cmd.reference,
                        cmd.index,
                        cmd.deletedReference,
                        cmd.deletedResolveInfo)
                        .addSource(source));
        }

        private void handleChangeClassifier(
            @NotNull ChangeClassifier cmd,
            @NotNull RepositoryData data,
            @NotNull CommandSource source) {
            SerializedClassifierInstance node = requireNode(data, cmd.node);
            io.lionweb.serialization.data.MetaPointer oldClassifier = node.getClassifier();
            node.setClassifier(cmd.newClassifier);
            channel.sendEvent(
                seqNum ->
                    new ClassifierChanged(seqNum, cmd.node, cmd.newClassifier, oldClassifier)
                        .addSource(source));
        }

        private void handleAddPartition(@NotNull AddPartition cmd, @NotNull CommandSource source) {
            createPartitionFromChunk(repositoryName, cmd.newPartition.getClassifierInstances());
            channel.sendEvent(seqNum -> new PartitionAdded(seqNum, cmd.newPartition).addSource(source));
        }

        private void handleDeletePartition(
            @NotNull DeletePartition cmd, @NotNull RepositoryData data, @NotNull CommandSource source) {
            List<String> descendants = new ArrayList<>();
            collectDescendants(data, cmd.deletedPartition, descendants);
            deletePartitions(repositoryName, Collections.singletonList(cmd.deletedPartition));
            channel.sendEvent(
                seqNum ->
                    new PartitionDeleted(seqNum, cmd.deletedPartition, descendants).addSource(source));
        }

        /** Returns the node or throws {@link NodeNotFoundException} if it does not exist. */
        private SerializedClassifierInstance requireNode(
            @NotNull RepositoryData data, @NotNull String nodeId) {
            List<SerializedClassifierInstance> retrieved = new ArrayList<>();
            try {
                data.retrieve(nodeId, 0, retrieved);
            } catch (IllegalArgumentException e) {
                throw new NodeNotFoundException(nodeId);
            }
            return retrieved.get(0);
        }

        private void collectDescendants(
            @NotNull RepositoryData data, @NotNull String nodeId, @NotNull List<String> descendants) {
            SerializedClassifierInstance node = data.nodesByID.get(nodeId);
            if (node == null) return;
            node.getChildren()
                .forEach(
                    childId -> {
                        descendants.add(childId);
                        collectDescendants(data, childId, descendants);
                    });
        }

        private class NodeNotFoundException extends RuntimeException {
            NodeNotFoundException(String nodeId) {
                super("Node with id " + nodeId + " not found");
            }
        }
    }

}
