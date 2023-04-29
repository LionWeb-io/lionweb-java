package org.lionweb.lioncore.java.serialization;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.lionweb.lioncore.java.api.CompositeNodeResolver;
import org.lionweb.lioncore.java.api.LocalNodeResolver;
import org.lionweb.lioncore.java.api.NodeResolver;
import org.lionweb.lioncore.java.metamodel.*;
import org.lionweb.lioncore.java.model.Node;
import org.lionweb.lioncore.java.model.ReferenceValue;
import org.lionweb.lioncore.java.self.LionCore;
import org.lionweb.lioncore.java.serialization.data.*;

/**
 * This class is responsible for unserializing models.
 *
 * <p>The unserialization of each node _requires_ the unserializer to be able to resolve the Concept
 * used. If this requirement is not satisfied the unserialization will fail. The actual class
 * implementing Node being instantiated will depend on the configuration. Specific classes for
 * specific Concepts can be registered, and the usage of DynamicNode for all others can be enabled.
 *
 * <p>Note that by default JsonSerialization will require specific Node subclasses to be specified.
 * For example, it will need to know that the concept with id 'foo-library' can be unserialized to
 * instances of the class Library. If you want serialization to instantiate DynamicNodes for
 * concepts for which you do not have a corresponding Node subclass, then you need to enable that
 * behavior explicitly by calling getNodeInstantiator().enableDynamicNodes().
 */
public class JsonSerialization {

  /** This has specific support for LionCore or LionCoreBuiltins. */
  public static JsonSerialization getStandardSerialization() {
    JsonSerialization jsonSerialization = new JsonSerialization();
    jsonSerialization.conceptResolver.registerMetamodel(LionCore.getInstance());
    jsonSerialization.nodeInstantiator.registerLionCoreCustomUnserializers();
    jsonSerialization.primitiveValuesSerialization
        .registerLionBuiltinsPrimitiveSerializersAndUnserializers();
    jsonSerialization.nodeResolver.addAll(LionCore.getInstance().thisAndAllDescendants());
    jsonSerialization.nodeResolver.addAll(LionCoreBuiltins.getInstance().thisAndAllDescendants());
    return jsonSerialization;
  }

  /** This has no specific support for LionCore or LionCoreBuiltins. */
  public static JsonSerialization getBasicSerialization() {
    JsonSerialization jsonSerialization = new JsonSerialization();
    return jsonSerialization;
  }

  private ConceptResolver conceptResolver;
  private NodeInstantiator nodeInstantiator;
  private PrimitiveValuesSerialization primitiveValuesSerialization;

  private LocalNodeResolver nodeResolver;

  private JsonSerialization() {
    // prevent public access
    conceptResolver = new ConceptResolver();
    nodeInstantiator = new NodeInstantiator();
    primitiveValuesSerialization = new PrimitiveValuesSerialization();
    nodeResolver = new LocalNodeResolver();
  }

  //
  // Configuration
  //

  public ConceptResolver getConceptResolver() {
    return conceptResolver;
  }

  public NodeInstantiator getNodeInstantiator() {
    return nodeInstantiator;
  }

  public PrimitiveValuesSerialization getPrimitiveValuesSerialization() {
    return primitiveValuesSerialization;
  }

  public LocalNodeResolver getNodeResolver() {
    return nodeResolver;
  }

  //
  // Serialization
  //

  public SerializedChunk serializeTreeToSerializationBlock(Node root) {
    return serializeNodesToSerializationBlock(root.thisAndAllDescendants());
  }

  public SerializedChunk serializeNodesToSerializationBlock(List<Node> nodes) {
    SerializedChunk serializationBlock = new SerializedChunk();
    serializationBlock.setSerializationFormatVersion("1");
    for (Node node : nodes) {
      Objects.requireNonNull(node, "nodes should not contain null values");
      serializationBlock.addNode(serializeNode(node));
      Objects.requireNonNull(
          node.getConcept(), "A node should have a concept in order to be serialized");
      Objects.requireNonNull(
          node.getConcept().getMetamodel(),
          "A Concept should be part of a Metamodel in order to be serialized. Concept "
              + node.getConcept()
              + " is not");
      MetamodelKeyVersion metamodelKeyVersion =
          MetamodelKeyVersion.fromMetamodel(node.getConcept().getMetamodel());
      if (!serializationBlock.getMetamodels().contains(metamodelKeyVersion)) {
        serializationBlock.getMetamodels().add(metamodelKeyVersion);
      }
    }
    return serializationBlock;
  }

  public SerializedChunk serializeNodesToSerializationBlock(Node... nodes) {
    return serializeNodesToSerializationBlock(Arrays.asList(nodes));
  }

  public JsonElement serializeTreeToJsonElement(Node node) {
    return serializeNodesToJsonElement(node.thisAndAllDescendants());
  }

  public JsonElement serializeTreesToJsonElement(Node... roots) {
    Set<String> nodesIDs = new HashSet<>();
    List<Node> allNodes = new ArrayList<>();
    for (Node root : roots) {
      root.thisAndAllDescendants()
          .forEach(
              n -> {
                // We support serialization of incorrect nodes, so we allow nodes without ID to be
                // serialized
                if (n.getID() != null) {
                  if (!nodesIDs.contains(n.getID())) {
                    allNodes.add(n);
                    nodesIDs.add(n.getID());
                  }
                } else {
                    allNodes.add(n);
                }
              });
    }
    return serializeNodesToJsonElement(allNodes);
  }

  public JsonElement serializeNodesToJsonElement(List<Node> nodes) {
    SerializedChunk serializationBlock = serializeNodesToSerializationBlock(nodes);
    return new LowLevelJsonSerialization().serializeToJsonElement(serializationBlock);
  }

  public JsonElement serializeNodesToJsonElement(Node... nodes) {
    return serializeNodesToJsonElement(Arrays.asList(nodes));
  }

  public String serializeTreeToJsonString(Node node) {
    return jsonElementToString(serializeTreeToJsonElement(node));
  }

  public String serializeTreesToJsonString(Node... nodes) {
    return jsonElementToString(serializeTreesToJsonElement(nodes));
  }

  public String serializeNodesToJsonString(List<Node> nodes) {
    return jsonElementToString(serializeNodesToJsonElement(nodes));
  }

  public String serializeNodesToJsonString(Node... nodes) {
    return jsonElementToString(serializeNodesToJsonElement(nodes));
  }

  //
  // Serialization - Private
  //

  private String jsonElementToString(JsonElement element) {
    return new GsonBuilder().setPrettyPrinting().serializeNulls().create().toJson(element);
  }

  private SerializedNode serializeNode(@Nonnull Node node) {
    Objects.requireNonNull(node, "Node should not be null");
    SerializedNode serializedNode = new SerializedNode();
    serializedNode.setID(node.getID());
    serializedNode.setConcept(MetaPointer.from(node.getConcept()));
    if (node.getParent() != null) {
      serializedNode.setParentNodeID(node.getParent().getID());
    }
    serializeNodeProperties(node, serializedNode);
    serializeNodeContainments(node, serializedNode);
    serializeNodeReferences(node, serializedNode);
    return serializedNode;
  }

  private static void serializeNodeReferences(@Nonnull Node node, SerializedNode serializedNode) {
    Objects.requireNonNull(node, "Node should not be null");
    node.getConcept()
        .allReferences()
        .forEach(
            reference -> {
              SerializedReferenceValue referenceValue = new SerializedReferenceValue();
              referenceValue.setMetaPointer(
                  MetaPointer.from(
                      reference, ((MetamodelElement) reference.getContainer()).getMetamodel()));
              referenceValue.setValue(
                  node.getReferenceValues(reference).stream()
                      .map(
                          rv -> {
                            String referredID =
                                rv.getReferred() == null ? null : rv.getReferred().getID();
                            return new SerializedReferenceValue.Entry(
                                referredID, rv.getResolveInfo());
                          })
                      .collect(Collectors.toList()));
              serializedNode.addReferenceValue(referenceValue);
            });
  }

  private static void serializeNodeContainments(@Nonnull Node node, SerializedNode serializedNode) {
    Objects.requireNonNull(node, "Node should not be null");
    node.getConcept()
        .allContainments()
        .forEach(
            containment -> {
              SerializedContainmentValue containmentValue = new SerializedContainmentValue();
              containmentValue.setMetaPointer(
                  MetaPointer.from(
                      containment, ((MetamodelElement) containment.getContainer()).getMetamodel()));
              containmentValue.setValue(
                  node.getChildren(containment).stream()
                      .map(c -> c.getID())
                      .collect(Collectors.toList()));
              serializedNode.addContainmentValue(containmentValue);
            });
  }

  private void serializeNodeProperties(Node node, SerializedNode serializedNode) {
    node.getConcept()
        .allProperties()
        .forEach(
            property -> {
              SerializedPropertyValue propertyValue = new SerializedPropertyValue();
              propertyValue.setMetaPointer(
                  MetaPointer.from(
                      property, ((MetamodelElement) property.getContainer()).getMetamodel()));
              propertyValue.setValue(
                  serializePropertyValue(property.getType(), node.getPropertyValue(property)));
              serializedNode.addPropertyValue(propertyValue);
            });
  }

  //
  // Unserialization
  //

  public List<Node> unserializeToNodes(JsonElement jsonElement) {
    SerializedChunk serializationBlock =
        new LowLevelJsonSerialization().unserializeSerializationBlock(jsonElement);
    validateSerializationBlock(serializationBlock);
    return unserializeSerializationBlock(serializationBlock);
  }

  public List<Node> unserializeToNodes(String json) {
    return unserializeToNodes(JsonParser.parseString(json));
  }

  public List<Node> unserializeToNodes(InputStream inputStream) {
    return unserializeToNodes(JsonParser.parseReader(new InputStreamReader(inputStream)));
  }

  //
  // Unserialization - Private
  //

  private String serializePropertyValue(DataType dataType, Object value) {
    if (value == null) {
      return null;
    }
    return primitiveValuesSerialization.serialize(dataType.getID(), value);
  }

  private void validateSerializationBlock(SerializedChunk serializationBlock) {
    if (!serializationBlock.getSerializationFormatVersion().equals("1")) {
      throw new IllegalArgumentException("Only serializationFormatVersion = '1' is supported");
    }
  }

  /**
   * This method returned a sorted version of the original list, so that leaves nodes comes first,
   * or in other words that a parent never precedes its children.
   */
  private List<SerializedNode> sortLeavesFirst(List<SerializedNode> originalList) {
    List<SerializedNode> sortedList = new ArrayList<>();
    List<SerializedNode> nodesToSort = new ArrayList<>(originalList);
    // We create the list going from the roots, to their children and so on, and then we will revert
    // the list

    // Nodes with null IDs are ambiguous but they cannot be the children of any node: they can just
    // be parent of other nodes, so we put all of them at the start (so they end up at the end when we reverse
    // the list)
    nodesToSort.stream().filter(n -> n.getID() == null).forEach(n -> sortedList.add(n));
    nodesToSort.removeAll(sortedList);

    // We can start by putting at the start all the elements which either have no parent,
    // or had a parent already added to the list
    while (sortedList.size() < originalList.size()) {
      int initialLength = sortedList.size();
      for (int i = 0; i < nodesToSort.size(); i++) {
        SerializedNode n = nodesToSort.get(i);
        if (n.getParentNodeID() == null
            || sortedList.stream()
                .anyMatch(sn -> Objects.equals(sn.getID(), n.getParentNodeID()))) {
          sortedList.add(n);
          nodesToSort.remove(i);
          i--;
        }
      }
      if (initialLength == sortedList.size()) {
        throw new IllegalStateException(
            "Something is not right: we are unable to complete sorting the list " + originalList);
      }
    }

    Collections.reverse(sortedList);
    return sortedList;
  }

  private List<Node> unserializeSerializationBlock(SerializedChunk serializationBlock) {
    return unserializeNodes(serializationBlock.getNodes());
  }

  private List<Node> unserializeNodes(List<SerializedNode> serializedNodes) {
    // We want to unserialize the nodes starting from the leaves. This is useful because in certain
    // cases we may want to use the children as constructor parameters of the parent
    List<SerializedNode> sortedSerializedNodes = sortLeavesFirst(serializedNodes);
    if (sortedSerializedNodes.size() != serializedNodes.size()) {
      throw new IllegalStateException();
    }
    Map<String, Node> unserializedNodesByID = new HashMap<>();
    IdentityHashMap<SerializedNode, Node> serializedToNodeMap = new IdentityHashMap<>();
    sortedSerializedNodes.stream()
        .forEach(
            n -> {
              Node instantiatedNode = instantiateNodeFromSerialized(n, unserializedNodesByID);
              if (n.getID() != null && unserializedNodesByID.containsKey(n.getID())) {
                throw new IllegalStateException("Duplicate ID found: " + n.getID());
              }
              unserializedNodesByID.put(n.getID(), instantiatedNode);
              serializedToNodeMap.put(n, instantiatedNode);
            });
    if (sortedSerializedNodes.size() != serializedToNodeMap.size()) {
      throw new IllegalStateException("We got " + sortedSerializedNodes.size() + " nodes to unserialize, but we unserialized " + serializedToNodeMap.size());
    }
    NodeResolver nodeResolver =
        new CompositeNodeResolver(new MapBasedResolver(unserializedNodesByID), this.nodeResolver);
    serializedNodes.stream().forEach(n -> populateNode(n, serializedToNodeMap.get(n), nodeResolver));

    // We want the nodes returned to be sorted as the original serializedNodes
    List<Node> nodesWithOriginalSorting = serializedNodes.stream().map(sn -> serializedToNodeMap.get(sn)).collect(Collectors.toList());
    return nodesWithOriginalSorting;
  }

  private Node instantiateNodeFromSerialized(
      SerializedNode serializedNode, Map<String, Node> unserializedNodesByID) {
    Concept concept = getConceptResolver().resolveConcept(serializedNode.getConcept());

    // We prepare all the properties values and pass them to instantiator, as it could use them to
    // build the node
    Map<Property, Object> propertiesValues = new HashMap<>();
    serializedNode
        .getProperties()
        .forEach(
            serializedPropertyValue -> {
              Property property =
                  concept.getPropertyByMetaPointer(serializedPropertyValue.getMetaPointer());
              Objects.requireNonNull(
                  property,
                  "Property with metaPointer "
                      + serializedPropertyValue.getMetaPointer()
                      + " not found in concept "
                      + concept
                      + ". SerializedNode: "
                      + serializedNode);
              Object unserializedValue =
                  primitiveValuesSerialization.unserialize(
                      property.getType().getID(), serializedPropertyValue.getValue());
              propertiesValues.put(property, unserializedValue);
            });
    Node node =
        getNodeInstantiator()
            .instantiate(concept, serializedNode, unserializedNodesByID, propertiesValues);

    // We ensure that the properties values are set correctly. They could already have been set
    // while instantiating the node. If that is the case, we have nothing to do, otherwise we set
    // the values
    propertiesValues
        .entrySet()
        .forEach(
            pv -> {
              Object unserializedValue = pv.getValue();
              Property property = pv.getKey();
              // Avoiding calling setters, in case the value has been already set at construction
              // time

              if (!property.isDerived()
                  && !Objects.equals(unserializedValue, node.getPropertyValue(property))) {
                node.setPropertyValue(property, unserializedValue);
              }
            });

    return node;
  }

  private void populateNode(SerializedNode serializedNode, Node node, NodeResolver nodeResolver) {
    populateNodeContainments(serializedNode, node, nodeResolver);
    populateNodeReferences(serializedNode, node, nodeResolver);
  }

  private void populateNodeContainments(
      SerializedNode serializedNode, Node node, NodeResolver nodeResolver) {
    Concept concept = node.getConcept();
    serializedNode
        .getContainments()
        .forEach(
            serializedContainmentValue -> {
              Containment containment =
                  concept.getContainmentByMetaPointer(serializedContainmentValue.getMetaPointer());
              Objects.requireNonNull(
                  containment,
                  "Unable to resolve containment " + serializedContainmentValue.getMetaPointer());
              Objects.requireNonNull(
                  serializedContainmentValue.getValue(),
                  "The containment value should not be null");
              List<Node> unserializedValue =
                  serializedContainmentValue.getValue().stream()
                      .map(childNodeID -> nodeResolver.strictlyResolve(childNodeID))
                      .collect(Collectors.toList());
              if (!containment.isDerived()
                  && !Objects.equals(unserializedValue, node.getChildren(containment))) {
                unserializedValue.forEach(child -> node.addChild(containment, child));
              }
            });
  }

  private void populateNodeReferences(
      SerializedNode serializedNode, Node node, NodeResolver nodeResolver) {
    Concept concept = node.getConcept();
    // TODO resolve references to Nodes in different models
    serializedNode
        .getReferences()
        .forEach(
            serializedReferenceValue -> {
              Reference reference =
                  concept.getReferenceByMetaPointer(serializedReferenceValue.getMetaPointer());
              if (reference == null) {
                throw new IllegalStateException(
                    "Unable to solve reference "
                        + serializedReferenceValue.getMetaPointer()
                        + ". Concept "
                        + concept
                        + ". SerializedNode "
                        + serializedNode);
              }
              serializedReferenceValue
                  .getValue()
                  .forEach(
                      entry -> {
                        Node referred = nodeResolver.resolve(entry.getReference());
                        if (entry.getReference() != null && referred == null) {
                          throw new IllegalArgumentException(
                              "Unable to resolve reference to "
                                  + entry.getReference()
                                  + " for feature "
                                  + serializedReferenceValue.getMetaPointer());
                        }
                        ReferenceValue referenceValue =
                            new ReferenceValue(referred, entry.getResolveInfo());
                        node.addReferenceValue(reference, referenceValue);
                      });
            });
  }
}
