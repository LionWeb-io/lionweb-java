package io.lionweb.lioncore.java.serialization;

import io.lionweb.lioncore.java.api.ClassifierInstanceResolver;
import io.lionweb.lioncore.java.api.CompositeClassifierInstanceResolver;
import io.lionweb.lioncore.java.api.LocalClassifierInstanceResolver;
import io.lionweb.lioncore.java.model.ClassifierInstance;
import io.lionweb.lioncore.java.model.Node;
import io.lionweb.lioncore.java.model.impl.ProxyNode;
import io.lionweb.lioncore.java.serialization.data.SerializedClassifierInstance;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class is used to track the status of the deserializationg process, and in particular sorting
 * operations. It is also used to track the proxies created while sorting this particular set of
 * nodes, as the proxies created in this process will need to be returned together with the sorted
 * list of unserialized nodes.
 */
class DeserializationStatus {
  final List<SerializedClassifierInstance> sortedList;
  final List<SerializedClassifierInstance> nodesToSort;
  final List<ProxyNode> proxies = new ArrayList<>();
  private LocalClassifierInstanceResolver proxiesInstanceResolver;
  /**
   * Represent the combination of different ways to solve an instances resolver. It considers the
   * instances that are not connected to this deserialization process (outsideInstancesResolver),
   * and the proxies created during this deserialization process.
   */
  private ClassifierInstanceResolver globalInstanceResolver;

  DeserializationStatus(
      List<SerializedClassifierInstance> originalList,
      ClassifierInstanceResolver outsideInstancesResolver) {
    sortedList = new ArrayList<>();
    nodesToSort = new ArrayList<>(originalList);
    this.proxiesInstanceResolver = new LocalClassifierInstanceResolver();
    this.globalInstanceResolver =
        new CompositeClassifierInstanceResolver(outsideInstancesResolver, proxiesInstanceResolver);
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

  /**
   * Resolve ensure that the nodeID is resolved to a Node. If possible it retrieves a proper node or
   * a previously instantiated ProxyNode, otherwise created a ProxyNode and return it.
   */
  @Nullable
  Node resolve(@Nullable String nodeID) {
    if (nodeID == null) {
      return null;
    }
    ClassifierInstance<?> resolved = globalInstanceResolver.resolve(nodeID);
    if (resolved == null) {
      return createProxy(nodeID);
    } else if (resolved instanceof Node) {
      return (Node) resolved;
    } else {
      throw new IllegalStateException(
          "The given ID resolve to a classifier instance which is not a node: " + resolved);
    }
  }

  /**
   * This always create a new ProxyNode. Note that if a ProxyNode has been already created for the
   * given ID then an error will be thrown. To avoid this, consider using the resolve method.
   */
  @Nonnull
  ProxyNode createProxy(@Nonnull String nodeID) {
    Objects.requireNonNull(nodeID, "nodeID should not be null");
    if (globalInstanceResolver.resolve(nodeID) != null) {
      throw new IllegalStateException(
          "Cannot create a Proxy for node ID "
              + nodeID
              + " as there is already a Classifier Instance available for such ID");
    }
    ProxyNode proxyNode = new ProxyNode(nodeID);
    proxiesInstanceResolver.add(proxyNode);
    proxies.add(proxyNode);
    return proxyNode;
  }

  public LocalClassifierInstanceResolver getProxiesInstanceResolver() {
    return proxiesInstanceResolver;
  }
}
