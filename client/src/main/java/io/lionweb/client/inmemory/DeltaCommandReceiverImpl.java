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
import io.lionweb.client.delta.messages.commands.references.AddReference;
import io.lionweb.client.delta.messages.commands.references.ChangeReference;
import io.lionweb.client.delta.messages.commands.references.DeleteReference;
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
import io.lionweb.client.delta.messages.events.references.ReferenceAdded;
import io.lionweb.client.delta.messages.events.references.ReferenceChanged;
import io.lionweb.client.delta.messages.events.references.ReferenceDeleted;
import io.lionweb.serialization.data.MetaPointer;
import io.lionweb.serialization.data.SerializedClassifierInstance;
import io.lionweb.serialization.data.SerializedContainmentValue;
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
      else if (command instanceof MoveAndReplaceChildFromContainmentInOtherParent)
        handleMoveAndReplaceChildFromContainmentInOtherParent(
            (MoveAndReplaceChildFromContainmentInOtherParent) command, data, source);
      else if (command instanceof MoveAndReplaceChildFromOtherContainmentInSameParent)
        handleMoveAndReplaceChildFromOtherContainmentInSameParent(
            (MoveAndReplaceChildFromOtherContainmentInSameParent) command, data, source);
      else if (command instanceof MoveAndReplaceChildInSameContainmentInSameParent)
        handleMoveAndReplaceChildInSameContainmentInSameParent(
            (MoveAndReplaceChildInSameContainmentInSameParent) command, data, source);
      else if (command instanceof MoveAndReplaceAnnotationFromOtherParent)
        handleMoveAndReplaceAnnotationFromOtherParent(
            (MoveAndReplaceAnnotationFromOtherParent) command, data, source);
      else if (command instanceof MoveAndReplaceAnnotationInSameParent)
        handleMoveAndReplaceAnnotationInSameParent(
            (MoveAndReplaceAnnotationInSameParent) command, data, source);
      else if (command instanceof ChangeClassifier)
        handleChangeClassifier((ChangeClassifier) command, data, source);
      else if (command instanceof AddPartition) handleAddPartition((AddPartition) command, source);
      else if (command instanceof DeletePartition)
        handleDeletePartition((DeletePartition) command, data, source);
      else sendNotImplemented(command, source);
    } catch (NodeNotFoundException e) {
      sendError(participationId, StandardErrorCode.UNKNOWN_NODE.code, e.getMessage(), null);
    } catch (InvalidCommandException e) {
      sendError(participationId, e.errorCode.code, e.getMessage(), source);
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

  /**
   * Moves movedChild from oldContainment of oldParent to newContainment of newParent, replacing
   * replacedChild, which is deleted together with its descendants.
   */
  private void handleMoveAndReplaceChildFromContainmentInOtherParent(
      @NotNull MoveAndReplaceChildFromContainmentInOtherParent cmd,
      @NotNull RepositoryData data,
      @NotNull CommandSource source) {
    SerializedClassifierInstance newParent = requireExistingNode(data, cmd.newParent, "newParent");
    SerializedClassifierInstance oldParent = requireExistingNode(data, cmd.oldParent, "oldParent");
    SerializedClassifierInstance movedChild =
        requireExistingNode(data, cmd.movedChild, "movedChild");
    SerializedClassifierInstance replacedChild =
        requireExistingNode(data, cmd.replacedChild, "replacedChild");
    SerializedContainmentValue oldContainment =
        requireContainment(oldParent, cmd.oldContainment, "oldContainment");
    SerializedContainmentValue newContainment =
        requireContainment(newParent, cmd.newContainment, "newContainment");
    requireParentOf(oldParent, movedChild, "oldParent", "movedChild");
    requireParentOf(newParent, replacedChild, "newParent", "replacedChild");
    List<String> oldChildren = oldContainment.getChildrenIds();
    List<String> newChildren = newContainment.getChildrenIds();
    requireIndex(oldChildren, cmd.oldIndex, cmd.oldParent, cmd.oldContainment, "oldIndex");
    requireIndex(newChildren, cmd.newIndex, cmd.newParent, cmd.newContainment, "newIndex");
    requireChildAt(oldChildren, cmd.oldIndex, cmd.movedChild, cmd.oldParent, "oldIndex");
    requireChildAt(newChildren, cmd.newIndex, cmd.replacedChild, cmd.newParent, "newIndex");
    // Identical containments or indices are an error only when the parents are identical as well,
    // which is reported as such
    if (cmd.oldParent.equals(cmd.newParent)) {
      throw new InvalidCommandException(
          StandardErrorCode.OLD_AND_NEW_PARENT_IDENTICAL,
          "Old and new parent are both " + cmd.oldParent);
    }
    requireNotInSubtree(data, cmd.newParent, cmd.movedChild);

    Set<String> partitionIds = partitionsOf(data, cmd.oldParent, cmd.newParent);
    List<String> updatedOldChildren = new ArrayList<>(oldChildren);
    updatedOldChildren.remove(cmd.oldIndex);
    oldContainment.setChildrenIds(updatedOldChildren);
    List<String> updatedNewChildren = new ArrayList<>(newChildren);
    updatedNewChildren.set(cmd.newIndex, cmd.movedChild);
    newContainment.setChildrenIds(updatedNewChildren);
    movedChild.setParentNodeID(cmd.newParent);
    List<String> replacedDescendants = deleteReplacedNode(data, cmd.replacedChild);
    sendEventToSubscribers(
        partitionIds,
        seqNum ->
            new ChildMovedAndReplacedFromContainmentInOtherParent(
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
   * Moves movedChild from oldContainment to newContainment of the same parent, replacing
   * replacedChild, which is deleted together with its descendants.
   */
  private void handleMoveAndReplaceChildFromOtherContainmentInSameParent(
      @NotNull MoveAndReplaceChildFromOtherContainmentInSameParent cmd,
      @NotNull RepositoryData data,
      @NotNull CommandSource source) {
    SerializedClassifierInstance parent = requireExistingNode(data, cmd.parent, "parent");
    SerializedClassifierInstance movedChild =
        requireExistingNode(data, cmd.movedChild, "movedChild");
    SerializedClassifierInstance replacedChild =
        requireExistingNode(data, cmd.replacedChild, "replacedChild");
    SerializedContainmentValue oldContainment =
        requireContainment(parent, cmd.oldContainment, "oldContainment");
    SerializedContainmentValue newContainment =
        requireContainment(parent, cmd.newContainment, "newContainment");
    requireParentOf(parent, movedChild, "parent", "movedChild");
    requireParentOf(parent, replacedChild, "parent", "replacedChild");
    List<String> oldChildren = oldContainment.getChildrenIds();
    List<String> newChildren = newContainment.getChildrenIds();
    requireIndex(oldChildren, cmd.oldIndex, cmd.parent, cmd.oldContainment, "oldIndex");
    requireIndex(newChildren, cmd.newIndex, cmd.parent, cmd.newContainment, "newIndex");
    requireChildAt(oldChildren, cmd.oldIndex, cmd.movedChild, cmd.parent, "oldIndex");
    requireChildAt(newChildren, cmd.newIndex, cmd.replacedChild, cmd.parent, "newIndex");
    // Identical indices are an error only when the containments are identical as well, which is
    // reported as such
    if (cmd.oldContainment.equals(cmd.newContainment)) {
      throw new InvalidCommandException(
          StandardErrorCode.OLD_AND_NEW_CONTAINMENT_IDENTICAL,
          "Old and new containment are both " + cmd.oldContainment);
    }

    List<String> updatedOldChildren = new ArrayList<>(oldChildren);
    updatedOldChildren.remove(cmd.oldIndex);
    oldContainment.setChildrenIds(updatedOldChildren);
    List<String> updatedNewChildren = new ArrayList<>(newChildren);
    updatedNewChildren.set(cmd.newIndex, cmd.movedChild);
    newContainment.setChildrenIds(updatedNewChildren);
    List<String> replacedDescendants = deleteReplacedNode(data, cmd.replacedChild);
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

  /**
   * Moves movedChild from oldIndex by indexOffset positions within the same containment, replacing
   * replacedChild, which is deleted together with its descendants (see {@link
   * #moveAndReplaceInList}).
   */
  private void handleMoveAndReplaceChildInSameContainmentInSameParent(
      @NotNull MoveAndReplaceChildInSameContainmentInSameParent cmd,
      @NotNull RepositoryData data,
      @NotNull CommandSource source) {
    SerializedClassifierInstance parent = requireExistingNode(data, cmd.parent, "parent");
    SerializedClassifierInstance movedChild =
        requireExistingNode(data, cmd.movedChild, "movedChild");
    SerializedClassifierInstance replacedChild =
        requireExistingNode(data, cmd.replacedChild, "replacedChild");
    SerializedContainmentValue containment =
        requireContainment(parent, cmd.containment, "containment");
    requireParentOf(parent, movedChild, "parent", "movedChild");
    requireParentOf(parent, replacedChild, "parent", "replacedChild");
    List<String> children = containment.getChildrenIds();
    requireIndex(children, cmd.oldIndex, cmd.parent, cmd.containment, "oldIndex");
    int targetIndex = requireIndexOffset(children, cmd.oldIndex, cmd.indexOffset, cmd.parent);
    requireChildAt(children, cmd.oldIndex, cmd.movedChild, cmd.parent, "oldIndex");
    requireChildAt(children, targetIndex, cmd.replacedChild, cmd.parent, "indexOffset");

    containment.setChildrenIds(moveAndReplaceInList(children, cmd.oldIndex, targetIndex));
    List<String> replacedDescendants = deleteReplacedNode(data, cmd.replacedChild);
    sendEventToSubscribers(
        partitionsOf(data, cmd.parent),
        seqNum ->
            new ChildMovedAndReplacedInSameContainmentInSameParent(
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

  /**
   * Moves movedAnnotation from the annotations of oldParent to the annotations of newParent,
   * replacing replacedAnnotation, which is deleted together with its descendants.
   */
  private void handleMoveAndReplaceAnnotationFromOtherParent(
      @NotNull MoveAndReplaceAnnotationFromOtherParent cmd,
      @NotNull RepositoryData data,
      @NotNull CommandSource source) {
    SerializedClassifierInstance newParent = requireExistingNode(data, cmd.newParent, "newParent");
    SerializedClassifierInstance oldParent = requireExistingNode(data, cmd.oldParent, "oldParent");
    SerializedClassifierInstance movedAnnotation =
        requireExistingNode(data, cmd.movedAnnotation, "movedAnnotation");
    SerializedClassifierInstance replacedAnnotation =
        requireExistingNode(data, cmd.replacedAnnotation, "replacedAnnotation");
    requireParentOf(oldParent, movedAnnotation, "oldParent", "movedAnnotation");
    requireParentOf(newParent, replacedAnnotation, "newParent", "replacedAnnotation");
    List<String> oldAnnotations = oldParent.getAnnotations();
    List<String> newAnnotations = newParent.getAnnotations();
    requireIndex(oldAnnotations, cmd.oldIndex, cmd.oldParent, null, "oldIndex");
    requireIndex(newAnnotations, cmd.newIndex, cmd.newParent, null, "newIndex");
    requireAnnotationAt(
        oldAnnotations, cmd.oldIndex, cmd.movedAnnotation, cmd.oldParent, "oldIndex");
    requireAnnotationAt(
        newAnnotations, cmd.newIndex, cmd.replacedAnnotation, cmd.newParent, "newIndex");
    // Identical indices are an error only when the parents are identical as well, which is
    // reported as such
    if (cmd.oldParent.equals(cmd.newParent)) {
      throw new InvalidCommandException(
          StandardErrorCode.OLD_AND_NEW_PARENT_IDENTICAL,
          "Old and new parent are both " + cmd.oldParent);
    }
    requireNotInSubtree(data, cmd.newParent, cmd.movedAnnotation);

    Set<String> partitionIds = partitionsOf(data, cmd.oldParent, cmd.newParent);
    List<String> updatedOldAnnotations = new ArrayList<>(oldAnnotations);
    updatedOldAnnotations.remove(cmd.oldIndex);
    oldParent.setAnnotations(updatedOldAnnotations);
    List<String> updatedNewAnnotations = new ArrayList<>(newAnnotations);
    updatedNewAnnotations.set(cmd.newIndex, cmd.movedAnnotation);
    newParent.setAnnotations(updatedNewAnnotations);
    movedAnnotation.setParentNodeID(cmd.newParent);
    List<String> replacedDescendants = deleteReplacedNode(data, cmd.replacedAnnotation);
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

  /**
   * Moves movedAnnotation from oldIndex by indexOffset positions within the annotations of the same
   * parent, replacing replacedAnnotation, which is deleted together with its descendants (see
   * {@link #moveAndReplaceInList}).
   */
  private void handleMoveAndReplaceAnnotationInSameParent(
      @NotNull MoveAndReplaceAnnotationInSameParent cmd,
      @NotNull RepositoryData data,
      @NotNull CommandSource source) {
    SerializedClassifierInstance parent = requireExistingNode(data, cmd.parent, "parent");
    SerializedClassifierInstance movedAnnotation =
        requireExistingNode(data, cmd.movedAnnotation, "movedAnnotation");
    SerializedClassifierInstance replacedAnnotation =
        requireExistingNode(data, cmd.replacedAnnotation, "replacedAnnotation");
    requireParentOf(parent, movedAnnotation, "parent", "movedAnnotation");
    requireParentOf(parent, replacedAnnotation, "parent", "replacedAnnotation");
    List<String> annotations = parent.getAnnotations();
    requireIndex(annotations, cmd.oldIndex, cmd.parent, null, "oldIndex");
    int targetIndex = requireIndexOffset(annotations, cmd.oldIndex, cmd.indexOffset, cmd.parent);
    requireAnnotationAt(annotations, cmd.oldIndex, cmd.movedAnnotation, cmd.parent, "oldIndex");
    requireAnnotationAt(
        annotations, targetIndex, cmd.replacedAnnotation, cmd.parent, "indexOffset");

    parent.setAnnotations(moveAndReplaceInList(annotations, cmd.oldIndex, targetIndex));
    List<String> replacedDescendants = deleteReplacedNode(data, cmd.replacedAnnotation);
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

  private void handleChangeClassifier(
      @NotNull ChangeClassifier cmd, @NotNull RepositoryData data, @NotNull CommandSource source) {
    SerializedClassifierInstance node = requireNode(data, cmd.node);
    MetaPointer oldClassifier = node.getClassifier();
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

  /** Collects the IDs of all the descendants of the given node, children and annotations. */
  private void collectDescendants(
      @NotNull RepositoryData data, @NotNull String nodeId, @NotNull List<String> descendants) {
    SerializedClassifierInstance node = data.nodesByID.get(nodeId);
    if (node == null) return;
    List<String> directDescendants = new ArrayList<>(node.getChildren());
    directDescendants.addAll(node.getAnnotations());
    directDescendants.forEach(
        descendantId -> {
          descendants.add(descendantId);
          collectDescendants(data, descendantId, descendants);
        });
  }

  /**
   * Moves the element at oldIndex of the list to targetIndex, replacing the element found there, as
   * described in the specification: first, the element at targetIndex is overwritten with the moved
   * element; then, the element at oldIndex is removed. Hence, the moved element ends up at index
   * (targetIndex - 1) when moving up and at index targetIndex when moving down.
   */
  private static @NotNull List<String> moveAndReplaceInList(
      @NotNull List<String> list, int oldIndex, int targetIndex) {
    List<String> result = new ArrayList<>(list);
    result.set(targetIndex, list.get(oldIndex));
    result.remove(oldIndex);
    return result;
  }

  /**
   * Deletes from the repository the given node, which has already been removed from its parent,
   * together with its descendants.
   *
   * @return the IDs of the deleted descendants (children and annotations)
   */
  private @NotNull List<String> deleteReplacedNode(
      @NotNull RepositoryData data, @NotNull String nodeId) {
    List<String> descendants = new ArrayList<>();
    collectDescendants(data, nodeId, descendants);
    data.deleteNodeAndDescendant(nodeId);
    return descendants;
  }

  /** Returns the node or throws an {@link InvalidCommandException} with code unknownNode. */
  private static @NotNull SerializedClassifierInstance requireExistingNode(
      @NotNull RepositoryData data, @NotNull String nodeId, @NotNull String parameter) {
    SerializedClassifierInstance node = data.nodesByID.get(nodeId);
    if (node == null) {
      throw new InvalidCommandException(
          StandardErrorCode.UNKNOWN_NODE,
          "Unknown node " + nodeId + " (parameter " + parameter + ")");
    }
    return node;
  }

  /**
   * Returns the value of the given containment in the parent, or throws an {@link
   * InvalidCommandException} with code unknownContainment if the parent has no such containment.
   */
  private static @NotNull SerializedContainmentValue requireContainment(
      @NotNull SerializedClassifierInstance parent,
      @NotNull MetaPointer containment,
      @NotNull String parameter) {
    return parent.getContainments().stream()
        .filter(c -> containment.equals(c.getMetaPointer()))
        .findFirst()
        .orElseThrow(
            () ->
                new InvalidCommandException(
                    StandardErrorCode.UNKNOWN_CONTAINMENT,
                    "Node "
                        + parent.getID()
                        + " has no containment "
                        + containment
                        + " (parameter "
                        + parameter
                        + ")"));
  }

  /**
   * Throws an {@link InvalidCommandException} with code parentMismatch if child is not a child of
   * parent.
   */
  private static void requireParentOf(
      @NotNull SerializedClassifierInstance parent,
      @NotNull SerializedClassifierInstance child,
      @NotNull String parentParameter,
      @NotNull String childParameter) {
    if (!parent.getID().equals(child.getParentNodeID())) {
      throw new InvalidCommandException(
          StandardErrorCode.PARENT_MISMATCH,
          "Node "
              + parent.getID()
              + " ("
              + parentParameter
              + ") is not the parent of node "
              + child.getID()
              + " ("
              + childParameter
              + "), whose parent is "
              + child.getParentNodeID());
    }
  }

  /**
   * Throws an {@link InvalidCommandException} with code indexOutOfRange if the index is not a valid
   * index of the list.
   *
   * @param feature the containment holding the list, or null for annotations
   */
  private static void requireIndex(
      @NotNull List<String> list,
      int index,
      @NotNull String parent,
      @Nullable MetaPointer feature,
      @NotNull String parameter) {
    if (index < 0 || index >= list.size()) {
      throw new InvalidCommandException(
          StandardErrorCode.INDEX_OUT_OF_RANGE,
          "Index "
              + index
              + " ("
              + parameter
              + ") is out of range for "
              + (feature == null ? "the annotations" : "containment " + feature)
              + " of node "
              + parent
              + ", which has size "
              + list.size());
    }
  }

  /**
   * Throws an {@link InvalidCommandException} with code invalidIndexOffset if the index offset is 0
   * or (oldIndex + indexOffset) is not a valid index of the list.
   *
   * @return the target index (oldIndex + indexOffset)
   */
  private static int requireIndexOffset(
      @NotNull List<String> list, int oldIndex, int indexOffset, @NotNull String parent) {
    int targetIndex = oldIndex + indexOffset;
    if (indexOffset == 0 || targetIndex < 0 || targetIndex >= list.size()) {
      throw new InvalidCommandException(
          StandardErrorCode.INVALID_INDEX_OFFSET,
          "Index offset "
              + indexOffset
              + " is invalid for old index "
              + oldIndex
              + " in a list of size "
              + list.size()
              + " of node "
              + parent
              + ": valid offsets are in ["
              + (-oldIndex)
              + ".."
              + (list.size() - 1 - oldIndex)
              + "] and different from 0");
    }
    return targetIndex;
  }

  /**
   * Throws an {@link InvalidCommandException} with code indexChildMismatch if the child at the
   * given index is not the expected one.
   */
  private static void requireChildAt(
      @NotNull List<String> children,
      int index,
      @NotNull String expectedChild,
      @NotNull String parent,
      @NotNull String parameter) {
    requireAt(
        StandardErrorCode.INDEX_CHILD_MISMATCH,
        "Child",
        children,
        index,
        expectedChild,
        parent,
        parameter);
  }

  /**
   * Throws an {@link InvalidCommandException} with code indexAnnotationMismatch if the annotation
   * at the given index is not the expected one.
   */
  private static void requireAnnotationAt(
      @NotNull List<String> annotations,
      int index,
      @NotNull String expectedAnnotation,
      @NotNull String parent,
      @NotNull String parameter) {
    requireAt(
        StandardErrorCode.INDEX_ANNOTATION_MISMATCH,
        "Annotation",
        annotations,
        index,
        expectedAnnotation,
        parent,
        parameter);
  }

  private static void requireAt(
      @NotNull StandardErrorCode errorCode,
      @NotNull String elementDescription,
      @NotNull List<String> list,
      int index,
      @NotNull String expected,
      @NotNull String parent,
      @NotNull String parameter) {
    String actual = list.get(index);
    if (!actual.equals(expected)) {
      throw new InvalidCommandException(
          errorCode,
          elementDescription
              + " at index "
              + index
              + " ("
              + parameter
              + ") of node "
              + parent
              + " is "
              + actual
              + ", not "
              + expected);
    }
  }

  /**
   * Throws an {@link InvalidCommandException} with code circularContainment if newParent is the
   * moved node or one of its descendants.
   */
  private static void requireNotInSubtree(
      @NotNull RepositoryData data, @NotNull String newParent, @NotNull String movedNode) {
    List<String> cycle = new ArrayList<>();
    SerializedClassifierInstance node = data.nodesByID.get(newParent);
    while (node != null) {
      cycle.add(0, node.getID());
      if (node.getID().equals(movedNode)) {
        throw new InvalidCommandException(
            StandardErrorCode.CIRCULAR_CONTAINMENT,
            "Moving node "
                + movedNode
                + " under node "
                + newParent
                + " would create a containment cycle: "
                + String.join(",", cycle));
      }
      node = node.getParentNodeID() == null ? null : data.nodesByID.get(node.getParentNodeID());
    }
  }

  private class NodeNotFoundException extends RuntimeException {
    NodeNotFoundException(String nodeId) {
      super("Node with id " + nodeId + " not found");
    }
  }

  /**
   * A command which cannot be applied: it is answered with an error event having the given code,
   * sent only to the issuing participation. The state of the repository is left unchanged.
   */
  private static class InvalidCommandException extends RuntimeException {
    final @NotNull StandardErrorCode errorCode;

    InvalidCommandException(@NotNull StandardErrorCode errorCode, @NotNull String message) {
      super(message);
      this.errorCode = errorCode;
    }
  }
}
