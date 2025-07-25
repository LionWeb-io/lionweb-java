package io.lionweb.client.inmemory;

import io.lionweb.client.api.RepositoryConfiguration;
import io.lionweb.client.api.RepositoryVersionToken;
import io.lionweb.serialization.data.SerializedClassifierInstance;
import java.util.*;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

class RepositoryData {
  @NotNull RepositoryConfiguration configuration;
  final List<String> partitionIDs = new ArrayList<>();
  final Map<String, SerializedClassifierInstance> nodesByID = new HashMap<>();
  private int currentVersion = 0;
  private int nextId = 1;

  void deleteNodeAndDescendant(String nodeId) {
    SerializedClassifierInstance curr = nodesByID.get(nodeId);
    if (curr == null) {
      throw new IllegalArgumentException("Node " + nodeId + " does not exist");
    }
    nodesByID.remove(nodeId);
    curr.getChildren().forEach(this::deleteNodeAndDescendant);
  }

  private class ChangeCalculator {
    private final Map<String, SerializedClassifierInstance> addedNodes = new HashMap<>();
    private final Map<String, SerializedClassifierInstance> changedNodes = new HashMap<>();
    private final Set<String> removedNodes = new HashSet<>();

    void store(List<SerializedClassifierInstance> updatedNodes) {
      Map<String, SerializedClassifierInstance> updatedNodesAsMap = new HashMap<>();
      updatedNodes.forEach(n -> updatedNodesAsMap.put(n.getID(), n));
      for (SerializedClassifierInstance updatedNode : updatedNodes) {
        if (nodesByID.containsKey(updatedNode.getID())) {
          // Have we changed children?
          List<String> currentChildren = nodesByID.get(updatedNode.getID()).getChildren();
          List<String> updatedChildren = updatedNode.getChildren();
          updatedChildren.stream()
              .filter(n -> !currentChildren.contains(n))
              .forEach(n -> this.addedNodes.put(n, updatedNodesAsMap.get(n)));
          List<String> unknownChildren =
              updatedChildren.stream()
                  .filter(c -> !updatedNodesAsMap.containsKey(c) && !nodesByID.containsKey(c))
                  .collect(Collectors.toList());
          if (!unknownChildren.isEmpty()) {
            throw new IllegalArgumentException(
                "We got unknown nodes as children: " + unknownChildren);
          }
          this.removedNodes.addAll(
              currentChildren.stream()
                  .filter(n -> !updatedChildren.contains(n))
                  .collect(Collectors.toList()));
        } else {
          changedNodes.put(updatedNode.getID(), updatedNode);
        }
      }
      // They have been moved and not removed
      removedNodes.removeAll(addedNodes.keySet());
      nodesByID.putAll(updatedNodesAsMap);
      removedNodes.forEach(this::removeNode);
    }

    private void removeNode(String removeNodeId) {
      SerializedClassifierInstance serializedClassifierInstance = nodesByID.get(removeNodeId);
      for (String child : serializedClassifierInstance.getChildren()) {
        if (!addedNodes.containsKey(child)) {
          removeNode(child);
        }
      }
      nodesByID.remove(removeNodeId);
    }
  }

  RepositoryData(@NotNull RepositoryConfiguration configuration) {
    this.configuration = configuration;
  }

  RepositoryVersionToken bumpVersion() {
    return new RepositoryVersionToken("v-" + ++currentVersion);
  }

  List<String> ids(int count) {
    List<String> res = new ArrayList<>(count);
    for (int i = 0; i < count; i++) {
      res.add("id-" + (nextId++));
    }
    return res;
  }

  void store(List<SerializedClassifierInstance> newNodes) {
    newNodes.stream()
        .filter(n -> n.getParentNodeID() == null)
        .forEach(
            n -> {
              if (!partitionIDs.contains(n.getID())) {
                throw new IllegalArgumentException(
                    "Node " + n + " should be registered as a partition");
              }
            });
    new ChangeCalculator().store(newNodes);
  }

  List<SerializedClassifierInstance> retrieveTrees(List<String> ids) {
    List<SerializedClassifierInstance> nodes = new ArrayList<>();
    ids.forEach(n -> retrieveTree(n, nodes));
    return nodes;
  }

  private void retrieveTree(String id, List<SerializedClassifierInstance> nodes) {
    SerializedClassifierInstance n = nodesByID.get(id);
    nodes.add(n);
    n.getChildren().forEach(c -> retrieveTree(c, nodes));
  }

  void retrieve(String nodeId, int limit, List<SerializedClassifierInstance> retrieved) {
    SerializedClassifierInstance node = nodesByID.get(nodeId);
    if (node == null) {
      throw new IllegalArgumentException("Node with id " + nodeId + " cannot be found");
    }
    retrieved.add(node);
    if (limit > 0) {
      node.getChildren().forEach(c -> retrieve(c, limit - 1, retrieved));
    }
  }
}
