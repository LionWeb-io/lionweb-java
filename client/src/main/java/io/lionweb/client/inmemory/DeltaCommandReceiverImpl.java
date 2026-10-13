package io.lionweb.client.inmemory;

import io.lionweb.client.delta.CommandSource;
import io.lionweb.client.delta.DeltaChannel;
import io.lionweb.client.delta.DeltaCommandReceiver;
import io.lionweb.client.delta.messages.DeltaCommand;
import io.lionweb.client.delta.messages.DeltaEvent;
import io.lionweb.client.delta.messages.commands.ChangeClassifier;
import io.lionweb.client.delta.messages.commands.annotations.*;
import io.lionweb.client.delta.messages.commands.children.*;
import io.lionweb.client.delta.messages.commands.partitions.AddPartition;
import io.lionweb.client.delta.messages.commands.partitions.DeletePartition;
import io.lionweb.client.delta.messages.commands.properties.AddProperty;
import io.lionweb.client.delta.messages.commands.properties.ChangeProperty;
import io.lionweb.client.delta.messages.commands.properties.DeleteProperty;
import io.lionweb.client.delta.messages.commands.references.*;
import io.lionweb.client.delta.messages.events.ClassifierChanged;
import io.lionweb.client.delta.messages.events.CustomErrorCode;
import io.lionweb.client.delta.messages.events.ErrorEvent;
import io.lionweb.client.delta.messages.events.StandardErrorCode;
import io.lionweb.client.delta.messages.events.annotations.*;
import io.lionweb.client.delta.messages.events.children.*;
import io.lionweb.client.delta.messages.events.partitions.PartitionAdded;
import io.lionweb.client.delta.messages.events.partitions.PartitionDeleted;
import io.lionweb.client.delta.messages.events.properties.PropertyAdded;
import io.lionweb.client.delta.messages.events.properties.PropertyChanged;
import io.lionweb.client.delta.messages.events.properties.PropertyDeleted;
import io.lionweb.client.delta.messages.events.references.*;
import io.lionweb.serialization.data.MetaPointer;
import io.lionweb.serialization.data.SerializedClassifierInstance;
import io.lionweb.serialization.data.SerializedReferenceValue;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Implementation of {@link DeltaCommandReceiver} that receives commands from the server and applies
 * them to the in-memory server.
 */
class DeltaCommandReceiverImpl implements DeltaCommandReceiver {
  private final @NotNull String repositoryName;
  private final @NotNull DeltaChannel channel;
  private final @NotNull ParticipationManager participationManager;
  private final @NotNull InMemoryServer inMemoryServer;

  DeltaCommandReceiverImpl(
      @NotNull String repositoryName,
      @NotNull DeltaChannel channel,
      @NotNull ParticipationManager participationManager,
      @NotNull InMemoryServer inMemoryServer) {
    this.repositoryName = repositoryName;
    this.channel = channel;
    this.participationManager = participationManager;
    this.inMemoryServer = inMemoryServer;
  }

  @Override
  public void receiveCommand(@NotNull String participationId, @NotNull DeltaCommand command) {
    if (!participationManager.isActiveParticipation(participationId)) {
      sendError(
          participationId,
          StandardErrorCode.INVALID_PARTICIPATION.code,
          "Invalid participation: " + participationId,
          null);
      return;
    }
    CommandSource source = new CommandSource(participationId, command.commandId);
    RepositoryData data = inMemoryServer.getRepository(repositoryName);
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
        handleMoveAnnotationFromOtherParent((MoveAnnotationFromOtherParent) command, data, source);
      else if (command instanceof ReplaceAnnotation)
        handleReplaceAnnotation((ReplaceAnnotation) command, data, source);
      else if (command instanceof MoveChildInSameContainment)
        handleMoveChildInSameContainment((MoveChildInSameContainment) command, data, source);
      else if (command instanceof MoveChildFromOtherContainmentInSameParent)
        handleMoveChildFromOtherContainmentInSameParent(
            (MoveChildFromOtherContainmentInSameParent) command, data, source);
      else if (command instanceof MoveChildFromOtherContainment)
        handleMoveChildFromOtherContainment((MoveChildFromOtherContainment) command, data, source);
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
      else if (command instanceof AddReferenceTarget)
        handleAddReferenceTarget((AddReferenceTarget) command, data, source);
      else if (command instanceof ChangeReferenceTarget)
        handleChangeReferenceTarget((ChangeReferenceTarget) command, data, source);
      else if (command instanceof DeleteReferenceTarget)
        handleDeleteReferenceTarget((DeleteReferenceTarget) command, data, source);
      else if (command instanceof AddReferenceResolveInfo)
        handleAddReferenceResolveInfo((AddReferenceResolveInfo) command, data, source);
      else if (command instanceof ChangeReferenceResolveInfo)
        handleChangeReferenceResolveInfo((ChangeReferenceResolveInfo) command, data, source);
      else if (command instanceof DeleteReferenceResolveInfo)
        handleDeleteReferenceResolveInfo((DeleteReferenceResolveInfo) command, data, source);
      else if (command instanceof MoveEntryInSameReference)
        handleMoveEntryInSameReference((MoveEntryInSameReference) command, data, source);
      else if (command instanceof MoveEntryFromOtherReferenceInSameParent)
        handleMoveEntryFromOtherReferenceInSameParent(
            (MoveEntryFromOtherReferenceInSameParent) command, data, source);
      else if (command instanceof MoveEntryFromOtherReference)
        handleMoveEntryFromOtherReference((MoveEntryFromOtherReference) command, data, source);
      else if (command instanceof MoveAndReplaceEntryInSameReference)
        handleMoveAndReplaceEntryInSameReference(
            (MoveAndReplaceEntryInSameReference) command, data, source);
      else if (command instanceof MoveAndReplaceEntryFromOtherReferenceInSameParent)
        handleMoveAndReplaceEntryFromOtherReferenceInSameParent(
            (MoveAndReplaceEntryFromOtherReferenceInSameParent) command, data, source);
      else if (command instanceof MoveAndReplaceEntryFromOtherReference)
        handleMoveAndReplaceEntryFromOtherReference(
            (MoveAndReplaceEntryFromOtherReference) command, data, source);
      else if (command instanceof MoveAndReplaceChildInSameContainment)
        handleMoveAndReplaceChildInSameContainment(
            (MoveAndReplaceChildInSameContainment) command, data, source);
      else if (command instanceof MoveAndReplaceChildFromOtherContainmentInSameParent)
        handleMoveAndReplaceChildFromOtherContainmentInSameParent(
            (MoveAndReplaceChildFromOtherContainmentInSameParent) command, data, source);
      else if (command instanceof MoveAndReplaceChildFromOtherContainment)
        handleMoveAndReplaceChildFromOtherContainment(
            (MoveAndReplaceChildFromOtherContainment) command, data, source);
      else if (command instanceof MoveAndReplaceAnnotationInSameParent)
        handleMoveAndReplaceAnnotationInSameParent(
            (MoveAndReplaceAnnotationInSameParent) command, data, source);
      else if (command instanceof MoveAndReplaceAnnotationFromOtherParent)
        handleMoveAndReplaceAnnotationFromOtherParent(
            (MoveAndReplaceAnnotationFromOtherParent) command, data, source);
      else if (command instanceof ChangeClassifier)
        handleChangeClassifier((ChangeClassifier) command, data, source);
      else if (command instanceof AddPartition) handleAddPartition((AddPartition) command, source);
      else if (command instanceof DeletePartition)
        handleDeletePartition((DeletePartition) command, data, source);
      else sendNotImplemented(command, source);
    } catch (NodeNotFoundException e) {
      sendError(participationId, StandardErrorCode.UNKNOWN_NODE.code, e.getMessage(), null);
    } catch (InvalidCommandException e) {
      sendError(participationId, e.errorCode, e.getMessage(), source);
    }
  }

  /** Sends an error event for a recognized command that this server does not implement. */
  private void sendNotImplemented(@NotNull DeltaCommand command, @NotNull CommandSource source) {
    sendError(
        source.participationId,
        CustomErrorCode.NOT_IMPLEMENTED,
        "Unsupported command type: " + command.getClass().getName(),
        source);
  }

  /**
   * Sends an error event, optionally attributing it to the command that caused it. Error events are
   * sent only to the participation which issued the command.
   */
  private void sendError(
      @NotNull String participationId,
      @NotNull String errorCode,
      @Nullable String message,
      @Nullable CommandSource source) {
    inMemoryServer.sendEvent(
        channel,
        List.of(participationId),
        seqNum -> {
          ErrorEvent event = new ErrorEvent(seqNum, errorCode, message);
          return source == null ? event : event.addSource(source);
        });
  }

  private void handleChangeProperty(ChangeProperty cmd, RepositoryData data, CommandSource source) {
    SerializedClassifierInstance node = requireNode(data, cmd.node);
    String oldValue = node.getPropertyValue(cmd.property);
    node.setPropertyValue(cmd.property, cmd.newValue);
    String newValue = node.getPropertyValue(cmd.property);
    sendEventToSubscribers(
        partitionsOf(data, cmd.node),
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
    sendEventToSubscribers(
        partitionsOf(data, cmd.parent),
        seqNum ->
            new ChildAdded(seqNum, cmd.parent, cmd.newChild, cmd.containment, cmd.index)
                .addSource(source));
  }

  private void handleDeleteChild(DeleteChild cmd, RepositoryData data, CommandSource source) {
    SerializedClassifierInstance parent = requireNode(data, cmd.parent);
    requireNode(data, cmd.deletedChild);
    Set<String> partitionIds = partitionsOf(data, cmd.parent);
    List<String> descendants = new ArrayList<>();
    collectDescendants(data, cmd.deletedChild, descendants);
    parent.removeChild(cmd.deletedChild);
    data.deleteNodeAndDescendant(cmd.deletedChild);
    sendEventToSubscribers(
        partitionIds,
        seqNum ->
            new ChildDeleted(
                    seqNum, cmd.parent, cmd.deletedChild, descendants, cmd.index, cmd.containment)
                .addSource(source));
  }

  private void handleAddReference(AddReference cmd, RepositoryData data, CommandSource source) {
    SerializedClassifierInstance node = requireNode(data, cmd.parent);
    node.addReferenceValue(
        cmd.reference,
        cmd.index,
        new SerializedReferenceValue.Entry(cmd.newReference, cmd.newResolveInfo));
    sendEventToSubscribers(
        partitionsOf(data, cmd.parent),
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
    sendEventToSubscribers(
        partitionsOf(data, cmd.parent),
        seqNum ->
            new AnnotationAdded(seqNum, cmd.parent, cmd.newAnnotation, cmd.index)
                .addSource(source));
  }

  private void handleDeleteAnnotation(
      @NotNull DeleteAnnotation cmd, @NotNull RepositoryData data, @NotNull CommandSource source) {
    SerializedClassifierInstance parent = requireNode(data, cmd.parent);
    List<String> descendants = new ArrayList<>();
    collectDescendants(data, cmd.deletedAnnotation, descendants);
    List<String> annotations = new ArrayList<>(parent.getAnnotations());
    annotations.remove(cmd.index);
    parent.setAnnotations(annotations);
    data.deleteNodeAndDescendant(cmd.deletedAnnotation);
    sendEventToSubscribers(
        partitionsOf(data, cmd.parent),
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
    int newIndex = cmd.oldIndex + cmd.indexOffset;
    annotations.add(newIndex, id);
    parent.setAnnotations(annotations);
    sendEventToSubscribers(
        partitionsOf(data, cmd.parent),
        seqNum ->
            new AnnotationMovedInSameParent(
                    seqNum, cmd.indexOffset, cmd.movedAnnotation, cmd.parent, cmd.oldIndex)
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
    sendEventToSubscribers(
        partitionsOf(data, cmd.oldParent, cmd.newParent),
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
      @NotNull ReplaceAnnotation cmd, @NotNull RepositoryData data, @NotNull CommandSource source) {
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
    sendEventToSubscribers(
        partitionsOf(data, cmd.parent),
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
    int newIndex = cmd.oldIndex + cmd.indexOffset;
    parent.addChild(cmd.containment, cmd.movedChild, newIndex);
    sendEventToSubscribers(
        partitionsOf(data, cmd.parent),
        seqNum ->
            new ChildMovedInSameContainment(
                    seqNum,
                    cmd.indexOffset,
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
    sendEventToSubscribers(
        partitionsOf(data, cmd.parent),
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
    sendEventToSubscribers(
        partitionsOf(data, cmd.oldParent, cmd.newParent),
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
    sendEventToSubscribers(
        partitionsOf(data, cmd.parent),
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
    sendEventToSubscribers(
        partitionsOf(data, cmd.node),
        seqNum ->
            new PropertyAdded(seqNum, cmd.node, cmd.property, cmd.newValue).addSource(source));
  }

  private void handleDeleteProperty(
      @NotNull DeleteProperty cmd, @NotNull RepositoryData data, @NotNull CommandSource source) {
    SerializedClassifierInstance node = requireNode(data, cmd.node);
    String oldValue = node.getPropertyValue(cmd.property);
    node.setPropertyValue(cmd.property, null);
    sendEventToSubscribers(
        partitionsOf(data, cmd.node),
        seqNum -> new PropertyDeleted(seqNum, cmd.node, cmd.property, oldValue).addSource(source));
  }

  private void handleChangeReference(
      @NotNull ChangeReference cmd, @NotNull RepositoryData data, @NotNull CommandSource source) {
    SerializedClassifierInstance node = requireNode(data, cmd.parent);
    List<SerializedReferenceValue.Entry> entries =
        new ArrayList<>(node.getReferenceValues(cmd.reference));
    entries.set(
        cmd.index, new SerializedReferenceValue.Entry(cmd.newReference, cmd.newResolveInfo));
    node.setReferenceValue(cmd.reference, entries);
    sendEventToSubscribers(
        partitionsOf(data, cmd.parent),
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
    sendEventToSubscribers(
        partitionsOf(data, cmd.parent),
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

  private void handleAddReferenceTarget(
      @NotNull AddReferenceTarget cmd,
      @NotNull RepositoryData data,
      @NotNull CommandSource source) {
    String newTarget = requireTarget(cmd.newTarget);
    SerializedClassifierInstance node = requireNode(data, cmd.parent);
    List<SerializedReferenceValue.Entry> entries = referenceEntries(node, cmd.reference);
    SerializedReferenceValue.Entry entry = requireEntry(entries, cmd.index);
    String resolveInfo = entry.getResolveInfo();
    entries.set(cmd.index, new SerializedReferenceValue.Entry(newTarget, resolveInfo));
    node.setReferenceValue(cmd.reference, entries);
    sendEventToSubscribers(
        partitionsOf(data, cmd.parent),
        seqNum ->
            new ReferenceTargetAdded(
                    seqNum, cmd.parent, cmd.reference, cmd.index, newTarget, resolveInfo)
                .addSource(source));
  }

  private void handleChangeReferenceTarget(
      @NotNull ChangeReferenceTarget cmd,
      @NotNull RepositoryData data,
      @NotNull CommandSource source) {
    String newTarget = requireTarget(cmd.newTarget);
    SerializedClassifierInstance node = requireNode(data, cmd.parent);
    List<SerializedReferenceValue.Entry> entries = referenceEntries(node, cmd.reference);
    SerializedReferenceValue.Entry entry = requireEntry(entries, cmd.index);
    String replacedTarget = requireTarget(entry.getReference());
    String resolveInfo = entry.getResolveInfo();
    entries.set(cmd.index, new SerializedReferenceValue.Entry(newTarget, resolveInfo));
    node.setReferenceValue(cmd.reference, entries);
    sendEventToSubscribers(
        partitionsOf(data, cmd.parent),
        seqNum ->
            new ReferenceTargetChanged(
                    seqNum,
                    cmd.parent,
                    cmd.reference,
                    cmd.index,
                    newTarget,
                    resolveInfo,
                    replacedTarget)
                .addSource(source));
  }

  private void handleDeleteReferenceTarget(
      @NotNull DeleteReferenceTarget cmd,
      @NotNull RepositoryData data,
      @NotNull CommandSource source) {
    SerializedClassifierInstance node = requireNode(data, cmd.parent);
    List<SerializedReferenceValue.Entry> entries = referenceEntries(node, cmd.reference);
    SerializedReferenceValue.Entry entry = requireEntry(entries, cmd.index);
    String deletedTarget = requireTarget(entry.getReference());
    String resolveInfo = entry.getResolveInfo();
    entries.set(cmd.index, new SerializedReferenceValue.Entry(null, resolveInfo));
    node.setReferenceValue(cmd.reference, entries);
    sendEventToSubscribers(
        partitionsOf(data, cmd.parent),
        seqNum ->
            new ReferenceTargetDeleted(
                    seqNum, cmd.parent, cmd.reference, cmd.index, resolveInfo, deletedTarget)
                .addSource(source));
  }

  private void handleAddReferenceResolveInfo(
      @NotNull AddReferenceResolveInfo cmd,
      @NotNull RepositoryData data,
      @NotNull CommandSource source) {
    String newResolveInfo = requireResolveInfo(cmd.newResolveInfo);
    SerializedClassifierInstance node = requireNode(data, cmd.parent);
    List<SerializedReferenceValue.Entry> entries = referenceEntries(node, cmd.reference);
    SerializedReferenceValue.Entry entry = requireEntry(entries, cmd.index);
    String target = entry.getReference();
    entries.set(cmd.index, new SerializedReferenceValue.Entry(target, newResolveInfo));
    node.setReferenceValue(cmd.reference, entries);
    sendEventToSubscribers(
        partitionsOf(data, cmd.parent),
        seqNum ->
            new ReferenceResolveInfoAdded(
                    seqNum, cmd.parent, cmd.reference, cmd.index, newResolveInfo, target)
                .addSource(source));
  }

  private void handleChangeReferenceResolveInfo(
      @NotNull ChangeReferenceResolveInfo cmd,
      @NotNull RepositoryData data,
      @NotNull CommandSource source) {
    String newResolveInfo = requireResolveInfo(cmd.newResolveInfo);
    SerializedClassifierInstance node = requireNode(data, cmd.parent);
    List<SerializedReferenceValue.Entry> entries = referenceEntries(node, cmd.reference);
    SerializedReferenceValue.Entry entry = requireEntry(entries, cmd.index);
    String oldResolveInfo = requireResolveInfo(entry.getResolveInfo());
    String target = entry.getReference();
    entries.set(cmd.index, new SerializedReferenceValue.Entry(target, newResolveInfo));
    node.setReferenceValue(cmd.reference, entries);
    sendEventToSubscribers(
        partitionsOf(data, cmd.parent),
        seqNum ->
            new ReferenceResolveInfoChanged(
                    seqNum,
                    cmd.parent,
                    cmd.reference,
                    cmd.index,
                    newResolveInfo,
                    target,
                    oldResolveInfo)
                .addSource(source));
  }

  private void handleDeleteReferenceResolveInfo(
      @NotNull DeleteReferenceResolveInfo cmd,
      @NotNull RepositoryData data,
      @NotNull CommandSource source) {
    SerializedClassifierInstance node = requireNode(data, cmd.parent);
    List<SerializedReferenceValue.Entry> entries = referenceEntries(node, cmd.reference);
    SerializedReferenceValue.Entry entry = requireEntry(entries, cmd.index);
    String deletedResolveInfo = requireResolveInfo(entry.getResolveInfo());
    String target = entry.getReference();
    entries.set(cmd.index, new SerializedReferenceValue.Entry(target, null));
    node.setReferenceValue(cmd.reference, entries);
    sendEventToSubscribers(
        partitionsOf(data, cmd.parent),
        seqNum ->
            new ReferenceResolveInfoDeleted(
                    seqNum, cmd.parent, cmd.reference, cmd.index, target, deletedResolveInfo)
                .addSource(source));
  }

  /**
   * Moves an entry within the same reference. Consistently with {@link MoveChildInSameContainment},
   * the moved entry ends up at index {@code oldIndex + indexOffset}.
   */
  private void handleMoveEntryInSameReference(
      @NotNull MoveEntryInSameReference cmd,
      @NotNull RepositoryData data,
      @NotNull CommandSource source) {
    SerializedClassifierInstance node = requireNode(data, cmd.parent);
    List<SerializedReferenceValue.Entry> entries = referenceEntries(node, cmd.reference);
    SerializedReferenceValue.Entry moved = requireEntry(entries, cmd.oldIndex);
    entries.remove(cmd.oldIndex);
    int newIndex = requireInsertionIndex(entries, cmd.oldIndex + cmd.indexOffset);
    entries.add(newIndex, moved);
    node.setReferenceValue(cmd.reference, entries);
    sendEventToSubscribers(
        partitionsOf(data, cmd.parent),
        seqNum ->
            new EntryMovedInSameReference(
                    seqNum,
                    cmd.parent,
                    cmd.reference,
                    cmd.oldIndex,
                    cmd.indexOffset,
                    moved.getReference(),
                    moved.getResolveInfo())
                .addSource(source));
  }

  private void handleMoveEntryFromOtherReferenceInSameParent(
      @NotNull MoveEntryFromOtherReferenceInSameParent cmd,
      @NotNull RepositoryData data,
      @NotNull CommandSource source) {
    String parentId = requireParent(cmd.parent);
    SerializedClassifierInstance node = requireNode(data, parentId);
    SerializedReferenceValue.Entry moved =
        moveEntry(node, cmd.oldReference, cmd.oldIndex, node, cmd.newReference, cmd.newIndex);
    sendEventToSubscribers(
        partitionsOf(data, parentId),
        seqNum ->
            new EntryMovedFromOtherReferenceInSameParent(
                    seqNum,
                    parentId,
                    cmd.newReference,
                    cmd.newIndex,
                    cmd.oldReference,
                    cmd.oldIndex,
                    moved.getReference(),
                    moved.getResolveInfo())
                .addSource(source));
  }

  private void handleMoveEntryFromOtherReference(
      @NotNull MoveEntryFromOtherReference cmd,
      @NotNull RepositoryData data,
      @NotNull CommandSource source) {
    String oldParentId = requireParent(cmd.oldParent);
    String newParentId = requireParent(cmd.newParent);
    SerializedClassifierInstance oldParent = requireNode(data, oldParentId);
    SerializedClassifierInstance newParent = requireNode(data, newParentId);
    Set<String> partitionIds = partitionsOf(data, oldParentId, newParentId);
    SerializedReferenceValue.Entry moved =
        moveEntry(
            oldParent, cmd.oldReference, cmd.oldIndex, newParent, cmd.newReference, cmd.newIndex);
    sendEventToSubscribers(
        partitionIds,
        seqNum ->
            new EntryMovedFromOtherReference(
                    seqNum,
                    newParentId,
                    cmd.newReference,
                    cmd.newIndex,
                    oldParentId,
                    cmd.oldReference,
                    cmd.oldIndex,
                    moved.getReference(),
                    moved.getResolveInfo())
                .addSource(source));
  }

  /**
   * Moves an entry within the same reference, replacing the entry found at the destination.
   * Consistently with {@link #handleMoveEntryInSameReference}, the destination index {@code
   * oldIndex + indexOffset} is the final index of the moved entry, i.e., it is computed after the
   * moved entry has been removed from its original position.
   */
  private void handleMoveAndReplaceEntryInSameReference(
      @NotNull MoveAndReplaceEntryInSameReference cmd,
      @NotNull RepositoryData data,
      @NotNull CommandSource source) {
    SerializedClassifierInstance node = requireNode(data, cmd.parent);
    List<SerializedReferenceValue.Entry> entries = referenceEntries(node, cmd.reference);
    SerializedReferenceValue.Entry moved = requireEntry(entries, cmd.oldIndex);
    entries.remove(cmd.oldIndex);
    int newIndex = cmd.oldIndex + cmd.indexOffset;
    SerializedReferenceValue.Entry replaced = requireEntry(entries, newIndex);
    entries.set(newIndex, moved);
    node.setReferenceValue(cmd.reference, entries);
    sendEventToSubscribers(
        partitionsOf(data, cmd.parent),
        seqNum ->
            new EntryMovedAndReplacedInSameReference(
                    seqNum,
                    cmd.parent,
                    cmd.reference,
                    cmd.indexOffset,
                    moved.getReference(),
                    moved.getResolveInfo(),
                    cmd.oldIndex,
                    replaced.getReference(),
                    replaced.getResolveInfo())
                .addSource(source));
  }

  private void handleMoveAndReplaceEntryFromOtherReferenceInSameParent(
      @NotNull MoveAndReplaceEntryFromOtherReferenceInSameParent cmd,
      @NotNull RepositoryData data,
      @NotNull CommandSource source) {
    SerializedClassifierInstance node = requireNode(data, cmd.parent);
    SerializedReferenceValue.Entry[] movedAndReplaced =
        moveAndReplaceEntry(
            node, cmd.oldReference, cmd.oldIndex, node, cmd.newReference, cmd.newIndex);
    sendEventToSubscribers(
        partitionsOf(data, cmd.parent),
        seqNum ->
            new EntryMovedAndReplacedFromOtherReferenceInSameParent(
                    seqNum,
                    cmd.parent,
                    cmd.newReference,
                    cmd.newIndex,
                    movedAndReplaced[0].getReference(),
                    movedAndReplaced[0].getResolveInfo(),
                    cmd.oldReference,
                    cmd.oldIndex,
                    movedAndReplaced[1].getReference(),
                    movedAndReplaced[1].getResolveInfo())
                .addSource(source));
  }

  private void handleMoveAndReplaceEntryFromOtherReference(
      @NotNull MoveAndReplaceEntryFromOtherReference cmd,
      @NotNull RepositoryData data,
      @NotNull CommandSource source) {
    String oldParentId = requireParent(cmd.oldParent);
    String newParentId = requireParent(cmd.newParent);
    SerializedClassifierInstance oldParent = requireNode(data, oldParentId);
    SerializedClassifierInstance newParent = requireNode(data, newParentId);
    Set<String> partitionIds = partitionsOf(data, oldParentId, newParentId);
    SerializedReferenceValue.Entry[] movedAndReplaced =
        moveAndReplaceEntry(
            oldParent, cmd.oldReference, cmd.oldIndex, newParent, cmd.newReference, cmd.newIndex);
    sendEventToSubscribers(
        partitionIds,
        seqNum ->
            new EntryMovedAndReplacedFromOtherReference(
                    seqNum,
                    newParentId,
                    cmd.newReference,
                    cmd.newIndex,
                    movedAndReplaced[0].getReference(),
                    movedAndReplaced[0].getResolveInfo(),
                    oldParentId,
                    cmd.oldReference,
                    cmd.oldIndex,
                    movedAndReplaced[1].getReference(),
                    movedAndReplaced[1].getResolveInfo())
                .addSource(source));
  }

  /**
   * Moves a child within the same containment, replacing (and deleting) {@code replacedChild}. The
   * moved child takes the position the replaced child has once the moved child has been removed
   * from its original position.
   */
  private void handleMoveAndReplaceChildInSameContainment(
      @NotNull MoveAndReplaceChildInSameContainment cmd,
      @NotNull RepositoryData data,
      @NotNull CommandSource source) {
    SerializedClassifierInstance parent = requireNode(data, cmd.parent);
    requireNode(data, cmd.movedChild);
    List<String> replacedDescendants =
        moveAndReplaceChild(
            data, parent, cmd.movedChild, parent, cmd.containment, cmd.replacedChild);
    sendEventToSubscribers(
        partitionsOf(data, cmd.parent),
        seqNum ->
            new ChildMovedAndReplacedInSameContainment(
                    seqNum,
                    cmd.indexOffset,
                    cmd.movedChild,
                    cmd.parent,
                    cmd.containment,
                    cmd.oldIndex,
                    cmd.replacedChild,
                    replacedDescendants)
                .addSource(source));
  }

  private void handleMoveAndReplaceChildFromOtherContainmentInSameParent(
      @NotNull MoveAndReplaceChildFromOtherContainmentInSameParent cmd,
      @NotNull RepositoryData data,
      @NotNull CommandSource source) {
    SerializedClassifierInstance parent = requireNode(data, cmd.parent);
    requireNode(data, cmd.movedChild);
    List<String> replacedDescendants =
        moveAndReplaceChild(
            data, parent, cmd.movedChild, parent, cmd.newContainment, cmd.replacedChild);
    sendEventToSubscribers(
        partitionsOf(data, cmd.parent),
        seqNum ->
            new ChildMovedAndReplacedFromOtherContainmentInSameParent(
                    seqNum,
                    cmd.newContainment,
                    cmd.newIndex,
                    cmd.movedChild,
                    cmd.parent,
                    cmd.oldContainment,
                    cmd.oldIndex,
                    cmd.replacedChild,
                    replacedDescendants)
                .addSource(source));
  }

  private void handleMoveAndReplaceChildFromOtherContainment(
      @NotNull MoveAndReplaceChildFromOtherContainment cmd,
      @NotNull RepositoryData data,
      @NotNull CommandSource source) {
    SerializedClassifierInstance oldParent = requireNode(data, cmd.oldParent);
    SerializedClassifierInstance newParent = requireNode(data, cmd.newParent);
    SerializedClassifierInstance child = requireNode(data, cmd.movedChild);
    Set<String> partitionIds = partitionsOf(data, cmd.oldParent, cmd.newParent);
    List<String> replacedDescendants =
        moveAndReplaceChild(
            data, oldParent, cmd.movedChild, newParent, cmd.newContainment, cmd.replacedChild);
    child.setParentNodeID(cmd.newParent);
    sendEventToSubscribers(
        partitionIds,
        seqNum ->
            new ChildMovedAndReplacedFromOtherContainment(
                    seqNum,
                    cmd.newParent,
                    cmd.newContainment,
                    cmd.newIndex,
                    cmd.movedChild,
                    cmd.oldParent,
                    cmd.oldContainment,
                    cmd.oldIndex,
                    cmd.replacedChild,
                    replacedDescendants)
                .addSource(source));
  }

  /**
   * Moves an annotation within the same parent, replacing (and deleting) {@code
   * replacedAnnotation}. The moved annotation takes the position the replaced annotation has once
   * the moved annotation has been removed from its original position.
   */
  private void handleMoveAndReplaceAnnotationInSameParent(
      @NotNull MoveAndReplaceAnnotationInSameParent cmd,
      @NotNull RepositoryData data,
      @NotNull CommandSource source) {
    SerializedClassifierInstance parent = requireNode(data, cmd.parent);
    requireNode(data, cmd.movedAnnotation);
    List<String> replacedDescendants =
        moveAndReplaceAnnotation(data, parent, cmd.movedAnnotation, parent, cmd.replacedAnnotation);
    sendEventToSubscribers(
        partitionsOf(data, cmd.parent),
        seqNum ->
            new AnnotationMovedAndReplacedInSameParent(
                    seqNum,
                    cmd.indexOffset,
                    cmd.movedAnnotation,
                    cmd.parent,
                    cmd.oldIndex,
                    cmd.replacedAnnotation,
                    replacedDescendants.toArray(new String[0]))
                .addSource(source));
  }

  private void handleMoveAndReplaceAnnotationFromOtherParent(
      @NotNull MoveAndReplaceAnnotationFromOtherParent cmd,
      @NotNull RepositoryData data,
      @NotNull CommandSource source) {
    SerializedClassifierInstance oldParent = requireNode(data, cmd.oldParent);
    SerializedClassifierInstance newParent = requireNode(data, cmd.newParent);
    SerializedClassifierInstance annotation = requireNode(data, cmd.movedAnnotation);
    Set<String> partitionIds = partitionsOf(data, cmd.oldParent, cmd.newParent);
    List<String> replacedDescendants =
        moveAndReplaceAnnotation(
            data, oldParent, cmd.movedAnnotation, newParent, cmd.replacedAnnotation);
    annotation.setParentNodeID(cmd.newParent);
    sendEventToSubscribers(
        partitionIds,
        seqNum ->
            new AnnotationMovedAndReplacedFromOtherParent(
                    seqNum,
                    cmd.newParent,
                    cmd.newIndex,
                    cmd.movedAnnotation,
                    cmd.oldParent,
                    cmd.oldIndex,
                    cmd.replacedAnnotation,
                    replacedDescendants.toArray(new String[0]))
                .addSource(source));
  }

  private void handleChangeClassifier(
      @NotNull ChangeClassifier cmd, @NotNull RepositoryData data, @NotNull CommandSource source) {
    SerializedClassifierInstance node = requireNode(data, cmd.node);
    io.lionweb.serialization.data.MetaPointer oldClassifier = node.getClassifier();
    node.setClassifier(cmd.newClassifier);
    sendEventToSubscribers(
        partitionsOf(data, cmd.node),
        seqNum ->
            new ClassifierChanged(seqNum, cmd.node, cmd.newClassifier, oldClassifier)
                .addSource(source));
  }

  private void handleAddPartition(@NotNull AddPartition cmd, @NotNull CommandSource source) {
    inMemoryServer.createPartitionFromChunk(
        repositoryName, cmd.newPartition.getClassifierInstances());
    // The participation creating a partition is automatically subscribed to it
    List<String> newPartitionIds =
        cmd.newPartition.getClassifierInstances().stream()
            .filter(n -> n.getParentNodeID() == null)
            .map(SerializedClassifierInstance::getID)
            .collect(Collectors.toList());
    newPartitionIds.forEach(
        id -> participationManager.subscribeToPartition(source.participationId, id));
    sendPartitionEvent(
        newPartitionIds, seqNum -> new PartitionAdded(seqNum, cmd.newPartition).addSource(source));
  }

  private void handleDeletePartition(
      @NotNull DeletePartition cmd, @NotNull RepositoryData data, @NotNull CommandSource source) {
    List<String> descendants = new ArrayList<>();
    collectDescendants(data, cmd.deletedPartition, descendants);
    inMemoryServer.deletePartitions(repositoryName, List.of(cmd.deletedPartition));
    sendPartitionEvent(
        List.of(cmd.deletedPartition),
        seqNum ->
            new PartitionDeleted(seqNum, cmd.deletedPartition, descendants).addSource(source));
    // As per the specification of PartitionDeleted, clients are now unsubscribed from the deleted
    // partition: if a partition with the same ID is created later, they need to subscribe again
    participationManager.removeAllSubscriptionsTo(cmd.deletedPartition);
  }

  /**
   * Sends the event to the participations subscribed to the contents of any of the given
   * partitions.
   */
  private void sendEventToSubscribers(
      @NotNull Collection<String> partitionIds,
      @NotNull Function<Integer, DeltaEvent> eventProducer) {
    inMemoryServer.sendEvent(
        repositoryName,
        participationManager.participationsSubscribedToAnyOf(partitionIds),
        eventProducer);
  }

  /**
   * Sends an event concerning the list of partitions: it is received by the participations
   * subscribed to the list of partitions and by the participations subscribed to the contents of
   * the given partitions.
   */
  private void sendPartitionEvent(
      @NotNull Collection<String> partitionIds,
      @NotNull Function<Integer, DeltaEvent> eventProducer) {
    Set<String> recipients =
        new LinkedHashSet<>(participationManager.participationsSubscribedToPartitionList());
    recipients.addAll(participationManager.participationsSubscribedToAnyOf(partitionIds));
    inMemoryServer.sendEvent(repositoryName, recipients, eventProducer);
  }

  /** Returns the IDs of the partitions containing the given nodes. */
  private @NotNull Set<String> partitionsOf(
      @NotNull RepositoryData data, @NotNull String... nodeIds) {
    Set<String> partitionIds = new LinkedHashSet<>();
    for (String nodeId : nodeIds) {
      SerializedClassifierInstance node = data.nodesByID.get(nodeId);
      while (node != null && node.getParentNodeID() != null) {
        node = data.nodesByID.get(node.getParentNodeID());
      }
      if (node != null) {
        partitionIds.add(node.getID());
      }
    }
    return partitionIds;
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

  /** Returns a mutable copy of the entries of the given reference of the given node. */
  private static @NotNull List<SerializedReferenceValue.Entry> referenceEntries(
      @NotNull SerializedClassifierInstance node, @NotNull MetaPointer reference) {
    return new ArrayList<>(node.getReferenceValues(reference));
  }

  private static @NotNull SerializedReferenceValue.Entry requireEntry(
      @NotNull List<SerializedReferenceValue.Entry> entries, int index) {
    if (index < 0 || index >= entries.size()) {
      throw new InvalidCommandException(
          StandardErrorCode.UNKNOWN_INDEX.code,
          "No reference entry at index " + index + ", the reference has " + entries.size());
    }
    return entries.get(index);
  }

  private static int requireInsertionIndex(
      @NotNull List<SerializedReferenceValue.Entry> entries, int index) {
    if (index < 0 || index > entries.size()) {
      throw new InvalidCommandException(
          StandardErrorCode.UNKNOWN_INDEX.code,
          "Cannot insert a reference entry at index "
              + index
              + ", the reference has "
              + entries.size());
    }
    return index;
  }

  private static @NotNull String requireTarget(@Nullable String target) {
    if (target == null) {
      throw new InvalidCommandException(
          StandardErrorCode.UNDEFINED_REFERENCE_TARGET.code, "Reference target is not defined");
    }
    return target;
  }

  private static @NotNull String requireResolveInfo(@Nullable String resolveInfo) {
    if (resolveInfo == null) {
      throw new InvalidCommandException(
          CustomErrorCode.UNDEFINED_RESOLVE_INFO, "Reference resolveInfo is not defined");
    }
    return resolveInfo;
  }

  private static @NotNull String requireParent(@Nullable String parent) {
    if (parent == null) {
      throw new InvalidCommandException(
          StandardErrorCode.MOVE_WITHOUT_PARENT.code, "Parent of the move is not specified");
    }
    return parent;
  }

  /**
   * Moves the entry at {@code oldIndex} of {@code oldParent}'s {@code oldReference} to {@code
   * newIndex} of {@code newParent}'s {@code newReference}. Nothing is modified if the command is
   * invalid.
   *
   * @return the moved entry
   */
  private static @NotNull SerializedReferenceValue.Entry moveEntry(
      @NotNull SerializedClassifierInstance oldParent,
      @NotNull MetaPointer oldReference,
      int oldIndex,
      @NotNull SerializedClassifierInstance newParent,
      @NotNull MetaPointer newReference,
      int newIndex) {
    List<SerializedReferenceValue.Entry> oldEntries = referenceEntries(oldParent, oldReference);
    SerializedReferenceValue.Entry moved = requireEntry(oldEntries, oldIndex);
    oldEntries.remove(oldIndex);
    boolean sameReference = oldParent == newParent && oldReference.equals(newReference);
    List<SerializedReferenceValue.Entry> newEntries =
        sameReference ? oldEntries : referenceEntries(newParent, newReference);
    newEntries.add(requireInsertionIndex(newEntries, newIndex), moved);
    oldParent.setReferenceValue(oldReference, oldEntries);
    newParent.setReferenceValue(newReference, newEntries);
    return moved;
  }

  /**
   * Moves the entry at {@code oldIndex} of {@code oldParent}'s {@code oldReference} to {@code
   * newIndex} of {@code newParent}'s {@code newReference}, replacing the entry found there. Nothing
   * is modified if the command is invalid.
   *
   * @return the moved entry and the replaced entry, in this order
   */
  private static @NotNull SerializedReferenceValue.Entry[] moveAndReplaceEntry(
      @NotNull SerializedClassifierInstance oldParent,
      @NotNull MetaPointer oldReference,
      int oldIndex,
      @NotNull SerializedClassifierInstance newParent,
      @NotNull MetaPointer newReference,
      int newIndex) {
    List<SerializedReferenceValue.Entry> oldEntries = referenceEntries(oldParent, oldReference);
    SerializedReferenceValue.Entry moved = requireEntry(oldEntries, oldIndex);
    oldEntries.remove(oldIndex);
    boolean sameReference = oldParent == newParent && oldReference.equals(newReference);
    List<SerializedReferenceValue.Entry> newEntries =
        sameReference ? oldEntries : referenceEntries(newParent, newReference);
    SerializedReferenceValue.Entry replaced = requireEntry(newEntries, newIndex);
    newEntries.set(newIndex, moved);
    oldParent.setReferenceValue(oldReference, oldEntries);
    newParent.setReferenceValue(newReference, newEntries);
    return new SerializedReferenceValue.Entry[] {moved, replaced};
  }

  /**
   * Moves {@code movedChild} from {@code oldParent} into {@code newParent}'s {@code
   * newContainment}, at the position of {@code replacedChild}, which is deleted together with its
   * descendants. The position is computed after removing the moved child, so that it is correct
   * also when moving within the same containment. Nothing is modified if the command is invalid.
   *
   * @return the descendants of the replaced child
   */
  private @NotNull List<String> moveAndReplaceChild(
      @NotNull RepositoryData data,
      @NotNull SerializedClassifierInstance oldParent,
      @NotNull String movedChild,
      @NotNull SerializedClassifierInstance newParent,
      @NotNull MetaPointer newContainment,
      @NotNull String replacedChild) {
    List<String> replacedDescendants = requireReplaceable(data, movedChild, replacedChild);
    if (!oldParent.getChildren().contains(movedChild)) {
      throw new InvalidCommandException(
          StandardErrorCode.INDEX_NODE_MISMATCH.code,
          "Node " + movedChild + " is not a child of " + oldParent.getID());
    }
    List<String> siblings = new ArrayList<>(newParent.getContainmentValues(newContainment));
    siblings.remove(movedChild);
    int index = siblings.indexOf(replacedChild);
    if (index == -1) {
      throw new InvalidCommandException(
          StandardErrorCode.INDEX_NODE_MISMATCH.code,
          "Node " + replacedChild + " is not a child of " + newParent.getID());
    }
    oldParent.removeChild(movedChild);
    newParent.removeChild(replacedChild);
    data.deleteNodeAndDescendant(replacedChild);
    newParent.addChild(newContainment, movedChild, index);
    return replacedDescendants;
  }

  /**
   * Moves {@code movedAnnotation} from {@code oldParent} to {@code newParent}, at the position of
   * {@code replacedAnnotation}, which is deleted together with its descendants. The position is
   * computed after removing the moved annotation, so that it is correct also when moving within the
   * same parent. Nothing is modified if the command is invalid.
   *
   * @return the descendants of the replaced annotation
   */
  private @NotNull List<String> moveAndReplaceAnnotation(
      @NotNull RepositoryData data,
      @NotNull SerializedClassifierInstance oldParent,
      @NotNull String movedAnnotation,
      @NotNull SerializedClassifierInstance newParent,
      @NotNull String replacedAnnotation) {
    List<String> replacedDescendants =
        requireReplaceable(data, movedAnnotation, replacedAnnotation);
    List<String> oldAnnotations = new ArrayList<>(oldParent.getAnnotations());
    if (!oldAnnotations.remove(movedAnnotation)) {
      throw new InvalidCommandException(
          StandardErrorCode.INDEX_NODE_MISMATCH.code,
          "Node " + movedAnnotation + " is not an annotation of " + oldParent.getID());
    }
    List<String> newAnnotations =
        oldParent == newParent ? oldAnnotations : new ArrayList<>(newParent.getAnnotations());
    int index = newAnnotations.indexOf(replacedAnnotation);
    if (index == -1) {
      throw new InvalidCommandException(
          StandardErrorCode.INDEX_NODE_MISMATCH.code,
          "Node " + replacedAnnotation + " is not an annotation of " + newParent.getID());
    }
    newAnnotations.set(index, movedAnnotation);
    oldParent.setAnnotations(oldAnnotations);
    newParent.setAnnotations(newAnnotations);
    data.deleteNodeAndDescendant(replacedAnnotation);
    return replacedDescendants;
  }

  /**
   * Verifies that {@code replaced} exists and can be replaced by {@code moved}, i.e., {@code moved}
   * is neither {@code replaced} nor one of its descendants.
   *
   * @return the descendants of {@code replaced}
   */
  private @NotNull List<String> requireReplaceable(
      @NotNull RepositoryData data, @NotNull String moved, @NotNull String replaced) {
    requireNode(data, replaced);
    List<String> replacedDescendants = new ArrayList<>();
    collectDescendants(data, replaced, replacedDescendants);
    if (moved.equals(replaced) || replacedDescendants.contains(moved)) {
      throw new InvalidCommandException(
          StandardErrorCode.INVALID_MOVE.code,
          "Node " + moved + " cannot replace itself or one of its ancestors");
    }
    return replacedDescendants;
  }

  private class NodeNotFoundException extends RuntimeException {
    NodeNotFoundException(String nodeId) {
      super("Node with id " + nodeId + " not found");
    }
  }

  /**
   * A command which cannot be applied: it is answered with an error event having the given code.
   */
  private static class InvalidCommandException extends RuntimeException {
    final @NotNull String errorCode;

    InvalidCommandException(@NotNull String errorCode, @NotNull String message) {
      super(message);
      this.errorCode = errorCode;
    }
  }
}
