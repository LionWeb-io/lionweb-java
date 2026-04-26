package io.lionweb.client.diskbased;

import io.lionweb.client.api.ClassifierKey;
import io.lionweb.client.api.ClassifierResult;
import io.lionweb.client.api.RepositoryConfiguration;
import io.lionweb.client.api.RepositoryVersionToken;
import io.lionweb.serialization.data.MetaPointer;
import io.lionweb.serialization.data.SerializedClassifierInstance;
import io.lionweb.serialization.data.SerializedContainmentValue;
import io.lionweb.utils.CommonChecks;
import io.lionweb.utils.ValidationResult;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Holds all node data for a single repository.
 *
 * <p><b>Thread-safety:</b> concurrent reads of {@link #nodesByID} are safe (it is a {@link
 * ConcurrentHashMap}), but <em>mutations are not thread-safe</em>. Methods
 * such as {@link #store}, {@link #deleteNodeAndDescendant}, and the internal classifier index
 * updates perform multi-step, non-atomic operations across several data structures. Callers must
 * externally serialize all mutations. The {@code ConcurrentHashMap} for {@code nodesByID} should
 * not be mistaken for a broader thread-safety guarantee.
 */
class DiskBasedRepositoryData {
  @NotNull RepositoryConfiguration configuration;
  final List<String> partitionIDs = new ArrayList<>();
    //final Map<String, SerializedClassifierInstance> nodesByID = new ConcurrentHashMap<>();

  /** Pre-computed index: ClassifierKey → set of node IDs. Kept in sync with nodesByID. */
  private final Map<ClassifierKey, Set<String>> classifierIndex = new HashMap<>();

  private int currentVersion = 0;
  private int nextId = 1;

  private int maxHotPartitions = 100;

    private ColdPartitionManager coldPartitions = new ColdPartitionManager();
    private HotPartitionManager hotPartitions = new HotPartitionManager(maxHotPartitions, coldPartitions);

    public int getMaxHotPartitions() {
        return maxHotPartitions;
    }

    public void setMaxHotPartitions(int maxHotPartitions) {
        this.maxHotPartitions = maxHotPartitions;
        this.hotPartitions.setMaxHotPartitions(maxHotPartitions);
    }

    void deleteNodeAndDescendant(String nodeId) {
//    SerializedClassifierInstance curr = nodesByID.get(nodeId);
//    if (curr == null) {
//      throw new IllegalArgumentException("Node " + nodeId + " does not exist");
//    }
//    indexRemove(curr);
//    nodesByID.remove(nodeId);
//    curr.getChildren().forEach(this::deleteNodeAndDescendant);
        throw new UnsupportedOperationException();
  }

  private ClassifierKey classifierKeyOf(SerializedClassifierInstance node) {
    MetaPointer mp = node.getClassifier();
    return new ClassifierKey(mp.getLanguage(), mp.getKey());
  }

  private void indexAdd(SerializedClassifierInstance node) {
    ClassifierKey key = classifierKeyOf(node);
    classifierIndex.computeIfAbsent(key, k -> new HashSet<>()).add(node.getID());
  }

  private void indexRemove(SerializedClassifierInstance node) {
    ClassifierKey key = classifierKeyOf(node);
    Set<String> ids = classifierIndex.get(key);
    if (ids != null) {
      ids.remove(node.getID());
      if (ids.isEmpty()) {
        classifierIndex.remove(key);
      }
    }
  }

  /**
   * Returns pre-computed classifier → result map, applying an optional node-ID limit per
   * classifier.
   */
  Map<ClassifierKey, ClassifierResult> nodesByClassifier(Integer limit) {
    int actualLimit = (limit != null) ? limit : Integer.MAX_VALUE;
    Map<ClassifierKey, ClassifierResult> result = new HashMap<>(classifierIndex.size() * 2);
    for (Map.Entry<ClassifierKey, Set<String>> entry : classifierIndex.entrySet()) {
      Set<String> allIds = entry.getValue();
      int total = allIds.size();
      Set<String> limitedIds;
      if (actualLimit >= total) {
        limitedIds = Collections.unmodifiableSet(allIds);
      } else {
        limitedIds = new HashSet<>();
        for (String id : allIds) {
          limitedIds.add(id);
          if (limitedIds.size() >= actualLimit) break;
        }
      }
      result.put(entry.getKey(), new ClassifierResult(limitedIds, total));
    }
    return result;
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
//      Set<String> oldStateSet = new HashSet<>(oldState);
//      Set<String> newStateSet = new HashSet<>(newState);
//      for (String n : newState) {
//        if (!oldStateSet.contains(n)) {
//          this.addedNodes.put(n, updatedNodesAsMap.get(n));
//        }
//      }
//      List<String> unknownNodes = null;
//      for (String c : newState) {
//        if (!updatedNodesAsMap.containsKey(c) && !nodesByID.containsKey(c)) {
//          if (unknownNodes == null) unknownNodes = new ArrayList<>();
//          unknownNodes.add(c);
//        }
//      }
//      if (unknownNodes != null) {
//        throw new IllegalArgumentException("We got unknown nodes as " + role + ": " + unknownNodes);
//      }
//      for (String n : oldState) {
//        if (!newStateSet.contains(n)) {
//          this.removedNodes.add(n);
//        }
//      }
        throw new UnsupportedOperationException();
    }

    void store(List<SerializedClassifierInstance> updatedNodes) {
        throw new UnsupportedOperationException();
//      Map<String, SerializedClassifierInstance> updatedNodesAsMap = new HashMap<>();
//      updatedNodes.forEach(n -> updatedNodesAsMap.put(n.getID(), n));
//      for (SerializedClassifierInstance updatedNode : updatedNodes) {
//        if (nodesByID.containsKey(updatedNode.getID())) {
//          SerializedClassifierInstance currentNode = nodesByID.get(updatedNode.getID());
//          if (currentNode.getParentNodeID() != null
//              && !updatedNodesAsMap.containsKey(currentNode.getParentNodeID())) {
//            // If the node currently has a parent, which has not been modified it can only means two
//            // things:
//            // - The node has changed parent, being removed from the old parent
//            // - The node stayed where it was: same parent, same position
//            if (!currentNode.getParentNodeID().equals(updatedNode.getParentNodeID())) {
//              removeContainedNode(currentNode.getParentNodeID(), updatedNode.getID());
//            }
//          }
//          calculateNodeListDifferences(
//              updatedNodesAsMap,
//              nodesByID.get(updatedNode.getID()).getChildren(),
//              updatedNode.getChildren(),
//              "children");
//          calculateNodeListDifferences(
//              updatedNodesAsMap,
//              nodesByID.get(updatedNode.getID()).getAnnotations(),
//              updatedNode.getAnnotations(),
//              "annotations");
//        }
//      }
//      // They have been moved and not removed
//      removedNodes.removeAll(addedNodes.keySet());
//      // Update classifier index for new/updated nodes before replacing nodesByID entries
//      for (SerializedClassifierInstance updatedNode : updatedNodes) {
//        SerializedClassifierInstance existing = nodesByID.get(updatedNode.getID());
//        if (existing != null) {
//          // Handle potential classifier change (rare but correct)
//          ClassifierKey oldKey = classifierKeyOf(existing);
//          ClassifierKey newKey = classifierKeyOf(updatedNode);
//          if (!oldKey.equals(newKey)) {
//            indexRemove(existing);
//            indexAdd(updatedNode);
//          }
//        } else {
//          indexAdd(updatedNode);
//        }
//      }
//      nodesByID.putAll(updatedNodesAsMap);
//      removedNodes.forEach(this::removeNode);
    }

    private void removeNode(String removeNodeId) {
//      SerializedClassifierInstance serializedClassifierInstance = nodesByID.get(removeNodeId);
//      for (String childId : serializedClassifierInstance.getChildren()) {
//        if (!addedNodes.containsKey(childId)) {
//          removeNode(childId);
//        }
//      }
//      for (String annotationId : serializedClassifierInstance.getAnnotations()) {
//        if (!addedNodes.containsKey(annotationId)) {
//          removeNode(annotationId);
//        }
//      }
//      indexRemove(serializedClassifierInstance);
//      nodesByID.remove(removeNodeId);
        throw new UnsupportedOperationException();
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
//    SerializedClassifierInstance container = nodesByID.get(containerId);
//    container.getContainments().forEach(containment -> containment.removeChild(containedId));
//    container.removeAnnotation(containedId);
      throw new UnsupportedOperationException();
  }

  DiskBasedRepositoryData(@NotNull RepositoryConfiguration configuration) {
    this.configuration = configuration;
  }

  RepositoryVersionToken bumpVersion() {
    return new RepositoryVersionToken("v-" + ++currentVersion);
  }

  List<String> ids(int count) {
//    List<String> res = new ArrayList<>(count);
//    while (res.size() < count) {
//      String candidate = "id-" + (nextId++);
//      if (!nodesByID.containsKey(candidate)) {
//        res.add(candidate);
//      }
//    }
//    return res;
      throw new UnsupportedOperationException();
  }

    public Map<String, SerializedClassifierInstance> getNodesByID() {
        throw new UnsupportedOperationException();
    }

    public Set<String> getNodesIDs() {
        Set<String> ids = hotPartitions.getNodesIDs();
        ids.addAll(coldPartitions.getNodesIDs());
        return ids;
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
//    SerializedClassifierInstance n = nodesByID.get(id);
//    nodes.add(n);
//    n.getChildren().forEach(c -> retrieveTree(c, nodes));
      throw new UnsupportedOperationException();
  }

  void retrieve(String nodeId, int limit, List<SerializedClassifierInstance> retrieved) {
//    SerializedClassifierInstance node = nodesByID.get(nodeId);
//    if (node == null) {
//      throw new IllegalArgumentException("Node with id " + nodeId + " cannot be found");
//    }
//    retrieved.add(node);
//    if (limit > 0) {
//      node.getChildren()
//          .forEach(
//              childId -> {
//                try {
//                  retrieve(childId, limit - 1, retrieved);
//                } catch (Exception e) {
//                  throw new RuntimeException("Unable to retrieve child of " + node, e);
//                }
//              });
//      node.getAnnotations()
//          .forEach(
//              annotationId -> {
//                try {
//                  retrieve(annotationId, limit - 1, retrieved);
//                } catch (Exception e) {
//                  throw new RuntimeException("Unable to retrieve annotation of " + node, e);
//                }
//              });
//    }
      throw new UnsupportedOperationException();
  }

  /** This is intended for debugging purposes. It checks if the data is consistent. */
  public @NotNull ValidationResult checkConsistency() {
//    ValidationResult result = new ValidationResult();
//
//    // Check for invalid node IDs
//    for (String nodeId : nodesByID.keySet()) {
//      if (!CommonChecks.isValidID(nodeId)) {
//        result.addError("Invalid node id: " + nodeId);
//      }
//    }
//
//    // Check for duplicate node IDs (this is already guaranteed by using HashMap, but good to be
//    // explicit)
//    // No need for additional check since HashMap ensures uniqueness
//
//    // Ensuring that containments and annotations are the inverse of parent relationships
//    Map<String, Set<String>> containedNodes = new HashMap<>();
//    for (SerializedClassifierInstance node : nodesByID.values()) {
//      for (SerializedContainmentValue containmentValue : node.getContainments()) {
//        for (String childId : containmentValue.getChildrenIds()) {
//          // Verifying nodes do not appear in multiple containments or annotations
//          String newPlacement = node.getID() + " at " + containmentValue.getMetaPointer();
//          if (containedNodes.containsKey(childId)) {
//            result.addError(
//                childId
//                    + " is listed in multiple places: "
//                    + containedNodes.get(childId)
//                    + " and now "
//                    + newPlacement);
//          } else {
//            containedNodes.put(childId, new HashSet<>(Arrays.asList(newPlacement)));
//          }
//          SerializedClassifierInstance child = nodesByID.get(childId);
//          if (child != null && !child.getParentNodeID().equals(node.getID())) {
//            result.addError(
//                childId
//                    + " is listed as child of "
//                    + node.getID()
//                    + " but it has "
//                    + child.getParentNodeID()
//                    + " as parent");
//          }
//        }
//      }
//      for (String annotationId : node.getAnnotations()) {
//        // Verifying nodes do not appear in multiple containments or annotations
//        String newPlacement = node.getID() + " among annotations";
//        if (containedNodes.containsKey(annotationId)) {
//          result.addError(
//              annotationId
//                  + " is listed in multiple places: "
//                  + containedNodes.get(annotationId)
//                  + " and now "
//                  + newPlacement);
//        } else {
//          containedNodes.put(annotationId, new HashSet<>(Arrays.asList(newPlacement)));
//        }
//        SerializedClassifierInstance annotation = nodesByID.get(annotationId);
//        if (annotationId != null
//            && annotation != null
//            && !Objects.equals(annotation.getParentNodeID(), node.getID())) {
//          result.addError(
//              annotationId
//                  + " is listed as an annotation of "
//                  + node.getID()
//                  + " but it has "
//                  + annotation.getParentNodeID()
//                  + "as parent ");
//        }
//      }
//
//      if (node.getParentNodeID() != null) {
//        SerializedClassifierInstance parent = nodesByID.get(node.getParentNodeID());
//        if (parent != null && !parent.contains(node.getID())) {
//          String msg =
//              node.getID()
//                  + " lists as parent "
//                  + node.getParentNodeID()
//                  + " but such parent does not contain it. It contains these children: "
//                  + parent.getChildren().stream().collect(Collectors.joining(", "));
//          if (!node.getAnnotations().isEmpty()) {
//            msg +=
//                " and these annotations: "
//                    + node.getAnnotations().stream().collect(Collectors.joining(", "));
//          }
//          result.addError(msg);
//        }
//      }
//    }
//
//    return result;
      throw new UnsupportedOperationException();
  }
}
