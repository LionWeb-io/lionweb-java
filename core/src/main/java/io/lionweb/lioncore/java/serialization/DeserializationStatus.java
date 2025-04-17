package io.lionweb.lioncore.java.serialization;

import io.lionweb.lioncore.java.api.ClassifierInstanceResolver;
import io.lionweb.lioncore.java.api.CompositeClassifierInstanceResolver;
import io.lionweb.lioncore.java.api.LocalClassifierInstanceResolver;
import io.lionweb.lioncore.java.language.*;
import io.lionweb.lioncore.java.model.ClassifierInstance;
import io.lionweb.lioncore.java.model.Node;
import io.lionweb.lioncore.java.model.impl.ProxyNode;
import io.lionweb.lioncore.java.serialization.data.MetaPointer;
import io.lionweb.lioncore.java.serialization.data.SerializedClassifierInstance;
import java.util.*;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class is used to track the status of the deserializationg process, and in particular sorting
 * operations. It is also used to track the proxies created while sorting this particular set of
 * nodes, as the proxies created in this process will need to be returned together with the sorted
 * list of unserialized nodes.
 */
class DeserializationStatus {
  private final List<SerializedClassifierInstance> sortedList;
  private final List<SerializedClassifierInstance> nodesToSort;
  final List<ProxyNode> proxies = new ArrayList<>();
  private final LocalClassifierInstanceResolver proxiesInstanceResolver;
  private final Set<String> sortedIDs = new HashSet<>();
  private final PrimitiveValuesSerialization primitiveValuesSerialization;
  private final IdentityHashMap<Classifier<?>, Map<MetaPointer, Feature<?>>> featuresCache =
      new IdentityHashMap<>();
  private final IdentityHashMap<DataType<?>, Map<String, Object>> propertyValuesCache =
      new IdentityHashMap<>();

  /**
   * Represent the combination of different ways to solve an instances resolver. It considers the
   * instances that are not connected to this deserialization process (outsideInstancesResolver),
   * and the proxies created during this deserialization process.
   */
  private ClassifierInstanceResolver globalInstanceResolver;

  DeserializationStatus(
      List<SerializedClassifierInstance> originalList,
      ClassifierInstanceResolver outsideInstancesResolver,
      PrimitiveValuesSerialization primitiveValuesSerialization) {
    this.primitiveValuesSerialization = primitiveValuesSerialization;
    sortedList = new ArrayList<>();
    nodesToSort = new ArrayList<>(originalList);
    this.proxiesInstanceResolver = new LocalClassifierInstanceResolver();
    this.globalInstanceResolver =
        new CompositeClassifierInstanceResolver(outsideInstancesResolver, proxiesInstanceResolver);
  }

  public Property getProperty(Classifier<?> classifier, MetaPointer metaPointer) {
    featuresCache.computeIfAbsent(classifier, c -> new HashMap<>());
    Map<MetaPointer, Feature<?>> featuresMap = featuresCache.get(classifier);
    featuresMap.computeIfAbsent(metaPointer, classifier::getPropertyByMetaPointer);
    return (Property) featuresMap.get(metaPointer);
  }

  public Containment getContainment(Classifier<?> classifier, MetaPointer metaPointer) {
    featuresCache.computeIfAbsent(classifier, c -> new HashMap<>());
    Map<MetaPointer, Feature<?>> featuresMap = featuresCache.get(classifier);
    featuresMap.computeIfAbsent(metaPointer, classifier::getContainmentByMetaPointer);
    return (Containment) featuresMap.get(metaPointer);
  }

  public Reference getReference(Classifier<?> classifier, MetaPointer metaPointer) {
    featuresCache.computeIfAbsent(classifier, c -> new HashMap<>());
    Map<MetaPointer, Feature<?>> featuresMap = featuresCache.get(classifier);
    featuresMap.computeIfAbsent(metaPointer, classifier::getReferenceByMetaPointer);
    return (Reference) featuresMap.get(metaPointer);
  }

  public Object deserializePropertyValue(
      DataType<?> dataType, String serializedValue, boolean isRequired) {
    propertyValuesCache.computeIfAbsent(dataType, dt -> new HashMap<>());
    Map<String, Object> map = propertyValuesCache.get(dataType);
    String key = serializedValue + "@required@" + isRequired;
    map.computeIfAbsent(key, k -> primitiveValuesSerialization.deserialize(dataType, serializedValue, isRequired));
    return map.get(key);
  }

  void putNodesWithNullIDsInFront() {
    // Nodes with null IDs are ambiguous but they cannot be the children of any node: they can
    // just be parent of other nodes, so we put all of them at the start (so they end up at the
    // end when we reverse the list)
    nodesToSort.stream().filter(n -> n.getID() == null).forEach(sortedList::add);
    sortedList.forEach(n -> sortedIDs.add(n.getID()));
    nodesToSort.removeAll(sortedList);
  }

  /** We place the node in the sorted list. */
  void place(SerializedClassifierInstance node) {
    sortedList.add(node);
    nodesToSort.remove(node);
    sortedIDs.add(node.getID());
  }

  public List<SerializedClassifierInstance> getSortedList() {
    return sortedList;
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

  public boolean isSortedID(String nodeID) {
    return sortedIDs.contains(nodeID);
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
