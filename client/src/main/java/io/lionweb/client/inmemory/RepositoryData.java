package io.lionweb.client.inmemory;

import io.lionweb.client.api.RepositoryConfiguration;
import io.lionweb.client.api.RepositoryVersionToken;
import io.lionweb.serialization.data.SerializedClassifierInstance;
import io.lionweb.serialization.data.SerializedContainmentValue;
import io.lionweb.utils.CommonChecks;
import io.lionweb.utils.ValidationResult;
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
    for (String childId : curr.getChildren()) {
      deleteNodeAndDescendant(childId);
    }
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
      for (String n : newState) {
        if (!oldState.contains(n)) {
          this.addedNodes.put(n, updatedNodesAsMap.get(n));
        }
      }
      List<String> unknownNodes = new ArrayList<>();
      for (String c : newState) {
        if (!updatedNodesAsMap.containsKey(c) && !nodesByID.containsKey(c)) {
          unknownNodes.add(c);
        }
      }
      if (!unknownNodes.isEmpty()) {
        throw new IllegalArgumentException("We got unknown nodes as " + role + ": " + unknownNodes);
      }
      for (String n : oldState) {
        if (!newState.contains(n)) {
          this.removedNodes.add(n);
        }
      }
    }

    void store(List<SerializedClassifierInstance> updatedNodes) {
      addedNodes.clear();
      removedNodes.clear();

      Map<String, SerializedClassifierInstance> updatedNodesAsMap = new HashMap<>();
      for (SerializedClassifierInstance n : updatedNodes) {
        updatedNodesAsMap.put(n.getID(), n);
      }

      for (SerializedClassifierInstance updatedNode : updatedNodes) {
        if (nodesByID.containsKey(updatedNode.getID())) {
          SerializedClassifierInstance currentNode = nodesByID.get(updatedNode.getID());
          if (currentNode.getParentNodeID() != null
              && !updatedNodesAsMap.containsKey(currentNode.getParentNodeID())) {
            // If the node currently has a parent, which has not been modified it can only means two
            // things:
            // - The node has changed parent, being removed from the old parent
            // - The node stayed where it was: same parent, same position
            if (!currentNode.getParentNodeID().equals(updatedNode.getParentNodeID())) {
              removeContainedNode(currentNode.getParentNodeID(), updatedNode.getID());
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
      for (String removeNodeId : removedNodes) {
        removeNode(removeNodeId);
      }
    }

    private void removeNode(String removeNodeId) {
      SerializedClassifierInstance serializedClassifierInstance = nodesByID.get(removeNodeId);
      for (String childId : serializedClassifierInstance.getChildren()) {
        if (!addedNodes.containsKey(childId)) {
          removeNode(childId);
        }
      }
      for (String annotationId : serializedClassifierInstance.getAnnotations()) {
        if (!addedNodes.containsKey(annotationId)) {
          removeNode(annotationId);
        }
      }
      nodesByID.remove(removeNodeId);
    }
  }

  /**
   * Removes a contained node (a child or an annotation) from the specified container in the
   * repository data.
   *
   * @param containerId the identifier of the container node from which the contained node should be
   *     removed
   * @param containedId the identifier of the contained node to be removed
   */
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
    for (String n : ids) {
      retrieveTree(n, nodes);
    }
    return nodes;
  }

  private void retrieveTree(String id, List<SerializedClassifierInstance> nodes) {
    SerializedClassifierInstance n = nodesByID.get(id);
    nodes.add(n);
    for (String c : n.getChildren()) {
      retrieveTree(c, nodes);
    }
  }

  void retrieve(String nodeId, int limit, List<SerializedClassifierInstance> retrieved) {
    SerializedClassifierInstance node = nodesByID.get(nodeId);
    if (node == null) {
      throw new IllegalArgumentException("Node with id " + nodeId + " cannot be found");
    }
    retrieved.add(node);
    if (limit > 0) {
      for (String childId : node.getChildren()) {
        try {
          retrieve(childId, limit - 1, retrieved);
        } catch (Exception e) {
          throw new RuntimeException("Unable to retrieve child of " + node, e);
        }
      }
      for (String annotationId : node.getAnnotations()) {
        try {
          retrieve(annotationId, limit - 1, retrieved);
        } catch (Exception e) {
          throw new RuntimeException("Unable to retrieve annotation of " + node, e);
        }
      }
    }
  }

  /** This is intended for debugging purposes. It checks if the data is consistent. */
  public @NotNull ValidationResult checkConsistency() {
    ValidationResult result = new ValidationResult();

    // Check for invalid node IDs
    for (String nodeId : nodesByID.keySet()) {
      if (!CommonChecks.isValidID(nodeId)) {
        result.addError("Invalid node id: " + nodeId);
      }
    }

    // Check for duplicate node IDs (this is already guaranteed by using HashMap, but good to be
    // explicit)
    // No need for additional check since HashMap ensures uniqueness

    // Ensuring that containments and annotations are the inverse of parent relationships
    Map<String, Set<String>> containedNodes = new HashMap<>();
    for (SerializedClassifierInstance node : nodesByID.values()) {
      for (SerializedContainmentValue containmentValue : node.getContainments()) {
        for (String childId : containmentValue.getChildrenIds()) {
          // Verifying nodes do not appear in multiple containments or annotations
          String newPlacement = node.getID() + " at " + containmentValue.getMetaPointer();
          if (containedNodes.containsKey(childId)) {
            result.addError(
                childId
                    + " is listed in multiple places: "
                    + containedNodes.get(childId)
                    + " and now "
                    + newPlacement);
          } else {
            containedNodes.put(childId, new HashSet<>(Arrays.asList(newPlacement)));
          }
          SerializedClassifierInstance child = nodesByID.get(childId);
          if (child != null && !child.getParentNodeID().equals(node.getID())) {
            result.addError(
                childId
                    + " is listed as child of "
                    + node.getID()
                    + " but it has "
                    + child.getParentNodeID()
                    + " as parent");
          }
        }
      }
      for (String annotationId : node.getAnnotations()) {
        // Verifying nodes do not appear in multiple containments or annotations
        String newPlacement = node.getID() + " among annotations";
        if (containedNodes.containsKey(annotationId)) {
          result.addError(
              annotationId
                  + " is listed in multiple places: "
                  + containedNodes.get(annotationId)
                  + " and now "
                  + newPlacement);
        } else {
          containedNodes.put(annotationId, new HashSet<>(Arrays.asList(newPlacement)));
        }
        SerializedClassifierInstance annotation = nodesByID.get(annotationId);
        if (annotationId != null
            && annotation != null
            && !Objects.equals(annotation.getParentNodeID(), node.getID())) {
          result.addError(
              annotationId
                  + " is listed as an annotation of "
                  + node.getID()
                  + " but it has "
                  + annotation.getParentNodeID()
                  + "as parent ");
        }
      }

      if (node.getParentNodeID() != null) {
        SerializedClassifierInstance parent = nodesByID.get(node.getParentNodeID());
        if (parent != null && !parent.contains(node.getID())) {
          String msg =
              node.getID()
                  + " lists as parent "
                  + node.getParentNodeID()
                  + " but such parent does not contain it. It contains these children: "
                  + parent.getChildren().stream().collect(Collectors.joining(", "));
          if (!node.getAnnotations().isEmpty()) {
            msg +=
                " and these annotations: "
                    + node.getAnnotations().stream().collect(Collectors.joining(", "));
          }
          result.addError(msg);
        }
      }
    }

    return result;
  }
}
