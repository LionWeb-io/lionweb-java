package io.lionweb.client.delta;

import io.lionweb.LionWebVersion;
import io.lionweb.client.delta.messages.BaseDeltaEvent;
import io.lionweb.client.delta.messages.DeltaEvent;
import io.lionweb.client.delta.messages.DeltaQueryResponse;
import io.lionweb.client.delta.messages.commands.children.AddChild;
import io.lionweb.client.delta.messages.commands.children.DeleteChild;
import io.lionweb.client.delta.messages.commands.partitions.AddPartition;
import io.lionweb.client.delta.messages.commands.partitions.DeletePartition;
import io.lionweb.client.delta.messages.commands.properties.ChangeProperty;
import io.lionweb.client.delta.messages.commands.references.AddReference;
import io.lionweb.client.delta.messages.events.ErrorEvent;
import io.lionweb.client.delta.messages.events.children.ChildAdded;
import io.lionweb.client.delta.messages.events.children.ChildDeleted;
import io.lionweb.client.delta.messages.events.partitions.PartitionAdded;
import io.lionweb.client.delta.messages.events.partitions.PartitionDeleted;
import io.lionweb.client.delta.messages.events.properties.PropertyChanged;
import io.lionweb.client.delta.messages.events.references.ReferenceAdded;
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
import io.lionweb.language.Containment;
import io.lionweb.language.Property;
import io.lionweb.language.Reference;
import io.lionweb.model.*;
import io.lionweb.model.impl.ProxyNode;
import io.lionweb.serialization.*;
import io.lionweb.serialization.data.MetaPointer;
import io.lionweb.serialization.data.SerializationChunk;
import java.lang.ref.WeakReference;
import java.util.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DeltaClient implements DeltaEventReceiver, DeltaQueryResponseReceiver {
  private LionWebVersion lionWebVersion;
  private DeltaChannel channel;
  private MonitoringObserver observer = new MonitoringObserver();
  private String participationId;
  private HashMap<String, Set<WeakReference<ClassifierInstance<?>>>> nodes = new HashMap<>();
  private DataTypesValuesSerialization dataTypesValuesSerialization =
      new DataTypesValuesSerialization();
  private AbstractSerialization serialization;
  private Set<String> queriesSent = new HashSet<>();
  private String clientId;
  private String pendingReconnectParticipationId;

  private static enum ParticipationState {
    NOT_CONNECTED,
    CONNECTED,
    SIGNED_OFF,
  }

  private ParticipationState state = ParticipationState.NOT_CONNECTED;

  public DeltaClient(DeltaChannel channel, String clientId) {
    this(LionWebVersion.currentVersion, channel, clientId);
  }

  public DeltaClient(LionWebVersion lionWebVersion, DeltaChannel channel, String clientId) {
    this.clientId = clientId;
    this.lionWebVersion = lionWebVersion;
    this.channel = channel;
    this.channel.registerEventReceiver(this);
    this.channel.registerQueryResponseReceiver(this);
    this.dataTypesValuesSerialization.registerLionBuiltinsPrimitiveSerializersAndDeserializers(
        lionWebVersion);
    this.serialization = SerializationProvider.getStandardJsonSerialization(lionWebVersion);
    this.serialization.setUnavailableParentPolicy(UnavailableNodePolicy.PROXY_NODES);
    this.serialization.setUnavailableReferenceTargetPolicy(UnavailableNodePolicy.PROXY_NODES);
  }

  /**
   * It is responsibility of the caller to ensure that the partition is initially in sync with the
   * server.
   */
  public void monitor(@NotNull Node partition) {
    Objects.requireNonNull(partition, "partition should not be null");
    synchronized (partition) {
      partition
          .thisAndAllDescendants()
          .forEach(
              n ->
                  nodes
                      .computeIfAbsent(n.getID(), id -> new HashSet<>())
                      .add(new WeakReference<>(n)));
      partition.registerPartitionObserver(observer);
    }
  }

  protected void monitorNode(Node node) {
    nodes.computeIfAbsent(node.getID(), id -> new HashSet<>()).add(new WeakReference<>(node));
  }

  @Override
  public void receiveEvent(DeltaEvent event) {
    if (isFromOwnParticipation(event)) return;
    observer.paused = true;
    try {
      if (event instanceof PropertyChanged) onPropertyChanged((PropertyChanged) event);
      else if (event instanceof ChildAdded) onChildAdded((ChildAdded) event);
      else if (event instanceof ChildDeleted) onChildDeleted((ChildDeleted) event);
      else if (event instanceof ReferenceAdded) onReferenceAdded((ReferenceAdded) event);
      else if (event instanceof ErrorEvent) onErrorEvent((ErrorEvent) event);
      else if (event instanceof PartitionAdded || event instanceof PartitionDeleted) {
        /* no-op */
      } else
        throw new UnsupportedOperationException(
            "Unsupported event type: " + event.getClass().getName());
    } finally {
      observer.paused = false;
    }
  }

  private boolean isFromOwnParticipation(DeltaEvent event) {
    if (!(event instanceof BaseDeltaEvent)) return false;
    return ((BaseDeltaEvent<?>) event)
        .originCommands.stream().anyMatch(src -> src.participationId.equals(this.participationId));
  }

  private void onPropertyChanged(PropertyChanged event) {
    Set<WeakReference<ClassifierInstance<?>>> matchingNodes = this.nodes.get(event.node);
    if (matchingNodes == null) return;
    for (WeakReference<ClassifierInstance<?>> ref : matchingNodes) {
      ClassifierInstance<?> instance = ref.get();
      if (instance != null) {
        ClassifierInstanceUtils.setPropertyValueByMetaPointer(
            instance, event.property, event.newValue);
      }
    }
  }

  private void onChildAdded(ChildAdded event) {
    for (WeakReference<ClassifierInstance<?>> ref : nodes.get(event.parent)) {
      ClassifierInstance<?> instance = ref.get();
      if (instance == null) continue;
      Node child = (Node) serialization.deserializeSerializationChunk(event.newChild).get(0);
      monitorNode(child);
      Containment containment =
          instance.getClassifier().getContainmentByMetaPointer(event.containment);
      if (containment == null) {
        throw new IllegalStateException(
            "Containment not found for " + instance + " using metapointer " + event.containment);
      }
      instance.addChild(containment, child, event.index);
    }
  }

  private void onChildDeleted(ChildDeleted event) {
    for (WeakReference<ClassifierInstance<?>> ref : nodes.get(event.parent)) {
      ClassifierInstance<?> instance = ref.get();
      if (instance == null) continue;
      Containment containment =
          instance.getClassifier().getContainmentByMetaPointer(event.containment);
      if (containment == null) {
        throw new IllegalStateException(
            "Containment not found for " + instance + " using metapointer " + event.containment);
      }
      instance.removeChild(containment, event.index);
    }
  }

  private void onReferenceAdded(ReferenceAdded event) {
    for (WeakReference<ClassifierInstance<?>> ref : nodes.get(event.parent)) {
      ClassifierInstance<?> instance = ref.get();
      if (instance == null) continue;
      Reference reference = instance.getClassifier().getReferenceByMetaPointer(event.reference);
      if (reference == null) {
        throw new IllegalStateException(
            "Reference not found for " + instance + " using metapointer " + event.reference);
      }
      instance.addReferenceValue(
          reference,
          event.index,
          new ReferenceValue(new ProxyNode(event.newReference), event.newResolveInfo));
    }
  }

  private void onErrorEvent(ErrorEvent event) {
    // observer.paused is reset by the finally block in receiveEvent
    throw new ErrorEventReceivedException(event.errorCode, event.message);
  }

  private class MonitoringObserver implements PartitionObserver {

    public boolean paused = false;

    @Override
    public void propertyChanged(
        ClassifierInstance<?> classifierInstance,
        Property property,
        Object oldValue,
        Object newValue) {
      if (paused) return;
      channel.sendCommand(
          participationId,
          commandId ->
              new ChangeProperty(
                  commandId,
                  classifierInstance.getID(),
                  MetaPointer.from(property),
                  dataTypesValuesSerialization.serialize(property.getType().getID(), newValue)));
    }

    @Override
    public void childAdded(
        ClassifierInstance<?> classifierInstance,
        Containment containment,
        int index,
        Node newChild) {
      if (paused) return;
      SerializationChunk chunk = serialization.serializeNodesToSerializationChunk(newChild);
      if (newChild.getID() == null) {
        throw new IllegalStateException("Child id must not be null");
      }
      channel.sendCommand(
          participationId,
          commandId ->
              new AddChild(
                  commandId,
                  classifierInstance.getID(),
                  chunk,
                  MetaPointer.from(containment),
                  index));
    }

    @Override
    public void childRemoved(
        ClassifierInstance<?> classifierInstance,
        Containment containment,
        int index,
        @NotNull Node removedChild) {
      if (paused) return;
      Objects.requireNonNull(removedChild, "removedChild must not be null");
      String removedChildId = removedChild.getID();
      Objects.requireNonNull(removedChildId, "removedChildId must not be null");
      channel.sendCommand(
          participationId,
          commandId ->
              new DeleteChild(
                  commandId,
                  classifierInstance.getID(),
                  MetaPointer.from(containment),
                  index,
                  removedChildId));
    }

    @Override
    public void annotationAdded(
        ClassifierInstance<?> node, int index, AnnotationInstance newAnnotation) {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void annotationRemoved(
        ClassifierInstance<?> node, int index, AnnotationInstance removedAnnotation) {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void referenceValueAdded(
        ClassifierInstance<?> classifierInstance,
        Reference reference,
        int index,
        ReferenceValue referenceValue) {
      if (paused) return;
      channel.sendCommand(
          participationId,
          commandId ->
              new AddReference(
                  commandId,
                  classifierInstance.getID(),
                  MetaPointer.from(reference),
                  index,
                  referenceValue.getReferredID(),
                  referenceValue.getResolveInfo()));
    }

    @Override
    public void referenceValueChanged(
        ClassifierInstance<?> classifierInstance,
        Reference reference,
        int index,
        String oldReferred,
        String oldResolveInfo,
        String newReferred,
        String newResolveInfo) {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void referenceValueRemoved(
        ClassifierInstance<?> classifierInstance,
        Reference reference,
        int index,
        ReferenceValue referenceValue) {
      throw new UnsupportedOperationException("Not supported yet.");
    }
  }

  /**
   * Retrieves the participation ID associated with the current session.
   *
   * @return the participation ID if available, or {@code null} if no participation ID has been set
   *     or the client is not currently participating.
   */
  public @Nullable String getParticipationId() {
    return participationId;
  }

  @Override
  public void receiveQueryResponse(DeltaQueryResponse queryResponse) {
    if (!queriesSent.contains(queryResponse.queryId)) return;
    if (queryResponse instanceof SignOnResponse) {
      SignOnResponse signOnResponse = (SignOnResponse) queryResponse;
      this.participationId = signOnResponse.participationId;
      this.state = ParticipationState.CONNECTED;
      return;
    } else if (queryResponse instanceof SignOffResponse) {
      this.state = ParticipationState.SIGNED_OFF;
      return;
    } else if (queryResponse instanceof ReconnectResponse) {
      this.participationId = pendingReconnectParticipationId;
      this.pendingReconnectParticipationId = null;
      this.state = ParticipationState.CONNECTED;
      return;
    } else if (queryResponse instanceof ListPartitionsResponse
        || queryResponse instanceof ListAndSubscribePartitionsResponse
        || queryResponse instanceof SubscribeToPartitionContentsResponse
        || queryResponse instanceof UnsubscribeFromPartitionContentsResponse) {
      return; // Callers receive these via the return value of the send methods
    }
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void sendSignOnRequest() {
    channel.sendQuery(
        queryId -> {
          queriesSent.add(queryId);
          return new SignOnRequest(queryId, DeltaProtocolVersion.v2025_1, clientId);
        });
  }

  /** Sends a SignOffRequest to terminate the current participation. */
  public void sendSignOffRequest() {
    channel.sendQuery(
        queryId -> {
          queriesSent.add(queryId);
          return new SignOffRequest(queryId);
        });
  }

  /**
   * Resumes a prior participation after a transport-level disconnect.
   *
   * @param existingParticipationId the participationId from the previous session
   * @param lastReceivedSequenceNumber the sequence number of the last event the client received
   */
  public void sendReconnectRequest(
      @NotNull String existingParticipationId, long lastReceivedSequenceNumber) {
    Objects.requireNonNull(existingParticipationId, "existingParticipationId must not be null");
    this.pendingReconnectParticipationId = existingParticipationId;
    channel.sendQuery(
        queryId -> {
          queriesSent.add(queryId);
          return new ReconnectRequest(queryId, existingParticipationId, lastReceivedSequenceNumber);
        });
  }

  /** Requests the list of partitions currently held in the repository. */
  public @NotNull ListPartitionsResponse sendListPartitionsRequest() {
    return (ListPartitionsResponse)
        channel.sendQuery(
            queryId -> {
              queriesSent.add(queryId);
              return new ListPartitionsRequest(queryId);
            });
  }

  /**
   * Requests the list of partitions and registers the participation for future partition-lifecycle
   * events.
   */
  public @NotNull ListAndSubscribePartitionsResponse sendListAndSubscribePartitionsRequest() {
    return (ListAndSubscribePartitionsResponse)
        channel.sendQuery(
            queryId -> {
              queriesSent.add(queryId);
              return new ListAndSubscribePartitionsRequest(queryId);
            });
  }

  /**
   * Subscribes to a specific partition, returning its current contents.
   *
   * @param partitionId node id of the partition to subscribe to
   */
  public @NotNull SubscribeToPartitionContentsResponse sendSubscribeToPartitionContentsRequest(
      @NotNull String partitionId) {
    Objects.requireNonNull(partitionId, "partitionId must not be null");
    return (SubscribeToPartitionContentsResponse)
        channel.sendQuery(
            queryId -> {
              queriesSent.add(queryId);
              return new SubscribeToPartitionContentsRequest(queryId, partitionId);
            });
  }

  /**
   * Unsubscribes from a previously subscribed partition.
   *
   * @param partitionId node id of the partition to unsubscribe from
   */
  public @NotNull UnsubscribeFromPartitionContentsResponse
      sendUnsubscribeFromPartitionContentsRequest(@NotNull String partitionId) {
    Objects.requireNonNull(partitionId, "partitionId must not be null");
    return (UnsubscribeFromPartitionContentsResponse)
        channel.sendQuery(
            queryId -> {
              queriesSent.add(queryId);
              return new UnsubscribeFromPartitionContentsRequest(queryId, partitionId);
            });
  }

  /**
   * Sends a command to create a new partition in the repository.
   *
   * @param partition the root node of the partition to add
   */
  public void sendAddPartitionCommand(@NotNull Node partition) {
    Objects.requireNonNull(partition, "partition must not be null");
    SerializationChunk chunk = serialization.serializeNodesToSerializationChunk(partition);
    channel.sendCommand(participationId, commandId -> new AddPartition(commandId, chunk));
  }

  /**
   * Sends a command to delete an existing partition from the repository.
   *
   * @param partitionId node id of the partition to delete
   */
  public void sendDeletePartitionCommand(@NotNull String partitionId) {
    Objects.requireNonNull(partitionId, "partitionId must not be null");
    channel.sendCommand(participationId, commandId -> new DeletePartition(commandId, partitionId));
  }
}
