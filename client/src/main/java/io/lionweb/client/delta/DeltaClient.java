package io.lionweb.client.delta;

import io.lionweb.LionWebVersion;
import io.lionweb.client.delta.messages.BaseDeltaEvent;
import io.lionweb.client.delta.messages.DeltaEvent;
import io.lionweb.client.delta.messages.DeltaQueryResponse;
import io.lionweb.client.delta.messages.commands.children.AddChild;
import io.lionweb.client.delta.messages.commands.children.DeleteChild;
import io.lionweb.client.delta.messages.commands.properties.ChangeProperty;
import io.lionweb.client.delta.messages.commands.references.AddReference;
import io.lionweb.client.delta.messages.events.ErrorEvent;
import io.lionweb.client.delta.messages.events.children.ChildAdded;
import io.lionweb.client.delta.messages.events.children.ChildDeleted;
import io.lionweb.client.delta.messages.events.properties.PropertyChanged;
import io.lionweb.client.delta.messages.events.references.ReferenceAdded;
import io.lionweb.client.delta.messages.queries.partitcipations.SignOnRequest;
import io.lionweb.client.delta.messages.queries.partitcipations.SignOnResponse;
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
    if (event instanceof BaseDeltaEvent) {
      BaseDeltaEvent<?> baseDeltaEvent = (BaseDeltaEvent<?>) event;

      if (baseDeltaEvent.originCommands.stream()
          .anyMatch(
              (CommandSource originCommand) ->
                  originCommand.participationId.equals(this.participationId))) {
        return;
      }
    }
    observer.paused = true;
    if (event instanceof PropertyChanged) {
      PropertyChanged propertyChanged = (PropertyChanged) event;
      Set<WeakReference<ClassifierInstance<?>>> matchingNodes =
          this.nodes.get(propertyChanged.node);
      if (matchingNodes != null) {
        for (WeakReference<ClassifierInstance<?>> classifierInstanceRef : matchingNodes) {
          ClassifierInstance<?> classifierInstance = classifierInstanceRef.get();
          if (classifierInstance != null) {
            ClassifierInstanceUtils.setPropertyValueByMetaPointer(
                classifierInstance, propertyChanged.property, propertyChanged.newValue);
          }
        }
      }
    } else if (event instanceof ChildAdded) {
      ChildAdded childAdded = (ChildAdded) event;
      for (WeakReference<ClassifierInstance<?>> classifierInstanceRef :
          nodes.get(childAdded.parent)) {
        ClassifierInstance<?> classifierInstance = classifierInstanceRef.get();
        if (classifierInstance != null) {
          Node child =
              (Node) serialization.deserializeSerializationChunk(childAdded.newChild).get(0);
          monitorNode(child);
          Containment containment =
              classifierInstance
                  .getClassifier()
                  .getContainmentByMetaPointer(childAdded.containment);
          if (containment == null) {
            throw new IllegalStateException(
                "Containment not found for "
                    + classifierInstance
                    + " using metapointer "
                    + childAdded.containment);
          }
          classifierInstance.addChild(containment, child, childAdded.index);
        }
      }
    } else if (event instanceof ChildDeleted) {
      ChildDeleted childDeleted = (ChildDeleted) event;
      for (WeakReference<ClassifierInstance<?>> classifierInstanceRef :
          nodes.get(childDeleted.parent)) {
        ClassifierInstance<?> classifierInstance = classifierInstanceRef.get();
        if (classifierInstance != null) {
          Containment containment =
              classifierInstance
                  .getClassifier()
                  .getContainmentByMetaPointer(childDeleted.containment);
          if (containment == null) {
            throw new IllegalStateException(
                "Containment not found for "
                    + classifierInstance
                    + " using metapointer "
                    + childDeleted.containment);
          }
          classifierInstance.removeChild(containment, childDeleted.index);
        }
      }
    } else if (event instanceof ErrorEvent) {
      ErrorEvent errorEvent = (ErrorEvent) event;
      observer.paused = false;
      throw new ErrorEventReceivedException(errorEvent.errorCode, errorEvent.message);
    } else if (event instanceof ReferenceAdded) {
      ReferenceAdded referenceAdded = (ReferenceAdded) event;
      for (WeakReference<ClassifierInstance<?>> classifierInstanceRef :
          nodes.get(referenceAdded.parent)) {
        ClassifierInstance<?> classifierInstance = classifierInstanceRef.get();
        if (classifierInstance != null) {
          Reference reference =
              classifierInstance
                  .getClassifier()
                  .getReferenceByMetaPointer(referenceAdded.reference);
          if (reference == null) {
            throw new IllegalStateException(
                "Reference not found for "
                    + classifierInstance
                    + " using metapointer "
                    + referenceAdded.reference);
          }
          classifierInstance.addReferenceValue(
              reference,
              referenceAdded.index,
              new ReferenceValue(
                  new ProxyNode(referenceAdded.newTarget), referenceAdded.newResolveInfo));
        }
      }
    } else {
      observer.paused = false;
      throw new UnsupportedOperationException(
          "Unsupported event type: " + event.getClass().getName());
    }
    observer.paused = false;
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

  @Override
  public void receiveQueryResponse(DeltaQueryResponse queryResponse) {
    if (!queriesSent.contains(queryResponse.queryId)) return;
    if (queryResponse instanceof SignOnResponse) {
      SignOnResponse signOnResponse = (SignOnResponse) queryResponse;
      this.participationId = signOnResponse.participationId;
      return;
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
}
