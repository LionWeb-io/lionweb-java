package io.lionweb.lioncore.java.serialization;

import io.lionweb.lioncore.java.model.impl.ProxyNode;
import io.lionweb.lioncore.java.serialization.data.SerializedClassifierInstance;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import javax.annotation.Nullable;

/**
 * This class is used to track the status of the deserializationg process, and in particular sorting
 * operations. It is also used to track the proxies created while sorting this particular set of
 * nodes, as the proxies created in this process will need to be returned together with the sorted
 * list of unserialized nodes.
 */
class DeserializationStatus {
  List<SerializedClassifierInstance> sortedList;
  List<SerializedClassifierInstance> nodesToSort;
  List<ProxyNode> proxies = new ArrayList<>();
  private JsonSerialization jsonSerialization;

  DeserializationStatus(
      JsonSerialization jsonSerialization, List<SerializedClassifierInstance> originalList) {
    sortedList = new ArrayList<>();
    nodesToSort = new ArrayList<>(originalList);
    this.jsonSerialization = jsonSerialization;
  }

  void putNodesWithNullIDsInFront() {
    // Nodes with null IDs are ambiguous but they cannot be the children of any node: they can
    // just be parent of other nodes, so we put all of them at the start (so they end up at the
    // end when we reverse the list)
    nodesToSort.stream().filter(n -> n.getID() == null).forEach(n -> sortedList.add(n));
    nodesToSort.removeAll(sortedList);
  }

  /** We place the node in the sorted list. */
  void place(SerializedClassifierInstance node) {
    sortedList.add(node);
    nodesToSort.remove(node);
  }

  void reverse() {
    Collections.reverse(sortedList);
  }

  int howManySorted() {
    return sortedList.size();
  }

  int howManyToSort() {
    return nodesToSort.size();
  }

  SerializedClassifierInstance getNodeToSort(int index) {
    return nodesToSort.get(index);
  }

  Stream<SerializedClassifierInstance> streamSorted() {
    return sortedList.stream();
  }

  ProxyNode createProxy(String nodeID) {
    ProxyNode proxyNode = this.jsonSerialization.createProxy(nodeID);
    proxies.add(proxyNode);
    return proxyNode;
  }

  @Nullable
  ProxyNode proxyFor(String nodeID) {
    return proxies.stream().filter(n -> n.getID().equals(nodeID)).findFirst().orElse(null);
  }
}
