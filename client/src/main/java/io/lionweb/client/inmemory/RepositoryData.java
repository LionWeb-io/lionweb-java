package io.lionweb.client.inmemory;

import io.lionweb.client.api.RepositoryConfiguration;
import io.lionweb.client.api.RepositoryVersionToken;
import io.lionweb.serialization.data.SerializedClassifierInstance;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;

class RepositoryData {
  @NotNull RepositoryConfiguration configuration;
  List<String> partitionIDs = new ArrayList<>();
  private Map<String, SerializedClassifierInstance> nodesByID = new HashMap<>();
  private int currentVersion = 0;
  private int nextId = 1;

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
    Map<String, SerializedClassifierInstance> newNodesByID = new HashMap<>(newNodes.size());
    newNodes.stream().forEach(n -> newNodesByID.put(n.getID(), n));
    Stream<SerializedClassifierInstance> heads =
        newNodes.stream()
            .filter(
                n -> n.getParentNodeID() == null || !newNodesByID.containsKey(n.getParentNodeID()));
    heads.forEach(h -> store(h, newNodesByID));
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

  private void store(
      SerializedClassifierInstance serializedClassifierInstance,
      Map<String, SerializedClassifierInstance> newNodesByID) {
    if (nodesByID.get(serializedClassifierInstance.getID()) != serializedClassifierInstance) {
      nodesByID.put(serializedClassifierInstance.getID(), serializedClassifierInstance);
      serializedClassifierInstance
          .getChildren()
          .forEach(c -> store(newNodesByID.get(c), newNodesByID));
    }
  }

  private boolean isRoot(SerializedClassifierInstance node) {
    return node.getParentNodeID() == null;
  }

  private Stream<SerializedClassifierInstance> thisAndAllDescendants(
      SerializedClassifierInstance root) {
    throw new UnsupportedOperationException();
  }
}
