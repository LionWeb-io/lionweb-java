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
    private final Set<String> removedNodes = new HashSet<>();

    /**
     * Given the two lists of nodes, it track the changes in terms of added or removed nodes
     *
     * @param updatedNodesAsMap the map id -> Node
     * @param oldState the list of nodes before the change
     * @param newState the list of nodes after the change
     * @param role the role of the list, used for debugging purposes
     */
    private void calculateNodeListDifferences(
        Map<String, SerializedClassifierInstance> updatedNodesAsMap,
        List<String> oldState,
        List<String> newState,
        String role) {
      newState.stream()
          .filter(n -> !oldState.contains(n))
          .forEach(n -> this.addedNodes.put(n, updatedNodesAsMap.get(n)));
      List<String> unknownNodes =
          newState.stream()
              .filter(c -> !updatedNodesAsMap.containsKey(c) && !nodesByID.containsKey(c))
              .collect(Collectors.toList());
      if (!unknownNodes.isEmpty()) {
        throw new IllegalArgumentException("We got unknown nodes as " + role + ": " + unknownNodes);
      }
      this.removedNodes.addAll(
          oldState.stream().filter(n -> !newState.contains(n)).collect(Collectors.toList()));
    }

    void store(List<SerializedClassifierInstance> updatedNodes) {
      Map<String, SerializedClassifierInstance> updatedNodesAsMap = new HashMap<>();
      updatedNodes.forEach(n -> updatedNodesAsMap.put(n.getID(), n));
      for (SerializedClassifierInstance updatedNode : updatedNodes) {
        if (nodesByID.containsKey(updatedNode.getID())) {
            SerializedClassifierInstance currNode = nodesByID.get(updatedNode.getID());
            if (currNode.getParentNodeID() != null && !updatedNodesAsMap.containsKey(currNode.getParentNodeID())) {
                // If the node currently has a parent, which has not been modified it can only means two things:
                // - The node has changed parent, being removed from the old parent
                // - The node stayed where it was: same parent, same position
                if (!currNode.getParentNodeID().equals(updatedNode.getParentNodeID())) {
                    removeContainedNode(currNode.getParentNodeID(), updatedNode.getID());
                }
            }
          calculateNodeListDifferences(
              updatedNodesAsMap,
              nodesByID.get(updatedNode.getID()).getChildren(),
              updatedNode.getChildren(),
              "children");
          calculateNodeListDifferences(
              updatedNodesAsMap,
              nodesByID.get(updatedNode.getID()).getAnnotations(),
              updatedNode.getAnnotations(),
              "annotations");
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
    for (String child : serializedClassifierInstance.getAnnotations()) {
        if (!addedNodes.containsKey(child)) {
            removeNode(child);
        }
    }
      nodesByID.remove(removeNodeId);
    }
  }

    private void removeContainedNode(String containerId, String containedId) {
      SerializedClassifierInstance container = nodesByID.get(containerId);
      container.getContainments().forEach(containment -> containment.removeChild(containedId));
      container.removeAnnotation(containedId);
    }

    RepositoryData(@NotNull RepositoryConfiguration configuration) {
    this.configuration = configuration;
  }

  RepositoryVersionToken bumpVersion() {
    return new RepositoryVersionToken("v-" + ++currentVersion);
  }

  List<String> ids(int count) {
    List<String> res = new ArrayList<>(count);
    while (res.size() < count) {
      String candidate = "id-" + (nextId++);
      if (!nodesByID.containsKey(candidate)) {
        res.add(candidate);
      }
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
      node.getChildren().forEach(c -> {try {
          retrieve(c, limit - 1, retrieved);
        } catch (Exception e){
          throw new RuntimeException("Unable to retrieve child of " + node, e);
        }});
      node.getAnnotations().forEach(a -> {
          try {
              retrieve(a, limit - 1, retrieved);
          } catch (Exception e) {
              throw new RuntimeException("Unable to retrieve annotation of " + node, e);
          }

      });
    }
  }
}
