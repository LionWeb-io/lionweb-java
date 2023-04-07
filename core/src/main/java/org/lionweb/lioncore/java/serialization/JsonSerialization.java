package org.lionweb.lioncore.java.serialization;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.lionweb.lioncore.java.api.CompositeNodeResolver;
import org.lionweb.lioncore.java.api.LocalNodeResolver;
import org.lionweb.lioncore.java.api.NodeResolver;
import org.lionweb.lioncore.java.metamodel.*;
import org.lionweb.lioncore.java.model.Node;
import org.lionweb.lioncore.java.model.ReferenceValue;
import org.lionweb.lioncore.java.self.LionCore;
import org.lionweb.lioncore.java.serialization.data.*;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This class is responsible for unserializing models.
 *
 * The unserialization of each node _requires_ the unserializer to be able to resolve the Concept used.
 * If this requirement is not satisfied the unserialization will fail.
 * The actual class implementing Node being instantiated will depend on the configuration.
 * Specific classes for specific Concepts can be registered, and the usage of DynamicNode for all others can be enabled.
 *
 * Note that by default JsonSerialization will require specific Node subclasses to be specified.
 * For example, it will need to know that the concept with id 'foo-library' can be unserialized to instances of the
 * class Library.
 * If you want serialization to instantiate DynamicNodes for concepts for which you do not have a corresponding Node
 * subclass, then you need to enable that behavior explicitly by calling getNodeInstantiator().enableDynamicNodes().
 */
public class JsonSerialization {

    /**
     * This has specific support for LionCore or LionCoreBuiltins.
     */
    public static JsonSerialization getStandardSerialization() {
        JsonSerialization jsonSerialization = new JsonSerialization();
        jsonSerialization.conceptResolver.registerMetamodel(LionCore.getInstance());
        jsonSerialization.nodeInstantiator.registerLionCoreCustomUnserializers();
        jsonSerialization.primitiveValuesSerialization.registerLionBuiltinsPrimitiveSerializersAndUnserializers();
        jsonSerialization.nodeResolver.addAll(LionCore.getInstance().thisAndAllDescendants());
        jsonSerialization.nodeResolver.addAll(LionCoreBuiltins.getInstance().thisAndAllDescendants());
        return jsonSerialization;
    }

    /**
     * This has no specific support for LionCore or LionCoreBuiltins.
     */
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
        for (Node node: nodes) {
            Objects.requireNonNull(node, "nodes should not contain null values");
            serializationBlock.addNode(serializeNode(node));
            Objects.requireNonNull(node.getConcept(), "A node should have a concept in order to be serialized");
            Objects.requireNonNull(node.getConcept().getMetamodel(),
                    "A Concept should be part of a Metamodel in order to be serialized. Concept " + node.getConcept() + " is not");
            MetamodelKeyVersion metamodelKeyVersion = MetamodelKeyVersion.fromMetamodel(node.getConcept().getMetamodel());
            if (!serializationBlock.getMetamodels().contains(metamodelKeyVersion)){
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

    public JsonElement serializeTreesToJsonElement(Node... nodes) {
        List<Node> allNodes = new ArrayList<>();
        for (Node n : nodes) {
            allNodes.addAll(n.thisAndAllDescendants());
        }
        return serializeNodesToJsonElement(allNodes);
    }

    public JsonElement serializeNodesToJsonElement(List<Node> nodes) {
        SerializedChunk serializationBlock = serializeNodesToSerializationBlock(nodes);
        return new LowLevelJsonSerialization().serializeToJson(serializationBlock);
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
        node.getConcept().allReferences().forEach(reference -> {
            SerializedReferenceValue referenceValue = new SerializedReferenceValue();
            referenceValue.setMetaPointer(MetaPointer.from(reference, ((MetamodelElement)reference.getContainer()).getMetamodel() ));
            referenceValue.setValue(node.getReferenceValues(reference).stream().map(rv -> {
                String referredID = rv.getReferred() == null ? null : rv.getReferred().getID();
                return new SerializedReferenceValue.Entry(referredID, rv.getResolveInfo());
            }).collect(Collectors.toList()));
            serializedNode.addReferenceValue(referenceValue);
        });
    }

    private static void serializeNodeContainments(@Nonnull Node node, SerializedNode serializedNode) {
        Objects.requireNonNull(node, "Node should not be null");
        node.getConcept().allContainments().forEach(containment -> {
            SerializedContainmentValue containmentValue = new SerializedContainmentValue();
            containmentValue.setMetaPointer(MetaPointer.from(containment, ((MetamodelElement)containment.getContainer()).getMetamodel() ));
            containmentValue.setValue(node.getChildren(containment).stream().map(c -> c.getID()).collect(Collectors.toList()));
            serializedNode.addContainmentValue(containmentValue);
        });
    }

    private void serializeNodeProperties(Node node, SerializedNode serializedNode) {
        node.getConcept().allProperties().forEach(property -> {
            SerializedPropertyValue propertyValue = new SerializedPropertyValue();
            propertyValue.setMetaPointer(MetaPointer.from(property, ((MetamodelElement)property.getContainer()).getMetamodel() ));
            propertyValue.setValue(serializePropertyValue(property.getType(), node.getPropertyValue(property)));
            serializedNode.addPropertyValue(propertyValue);
        });
    }

    //
    // Unserialization
    //

    public List<Node> unserializeToNode(JsonElement jsonElement) {
        SerializedChunk serializationBlock = new LowLevelJsonSerialization().unserializeSerializationBlock(jsonElement);
        validateSerializationBlock(serializationBlock);
        return unserializeSerializationBlock(serializationBlock);
    }

    public List<Node> unserializeToNode(String json) {
        return unserializeToNode(JsonParser.parseString(json));
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

    private List<Node> unserializeSerializationBlock(SerializedChunk serializationBlock) {
        List<Node> nodes = serializationBlock.getNodes().stream().map(n -> instantiateNodeFromSerialized(n)).collect(Collectors.toList());
        NodeResolver nodeResolver = new CompositeNodeResolver(new LocalNodeResolver(nodes), this.nodeResolver);
        serializationBlock.getNodes().stream().forEach(n -> populateNode(n, nodeResolver));
        return nodes;
    }

    private Node instantiateNodeFromSerialized(SerializedNode serializedNode) {
        Concept concept = getConceptResolver().resolveConcept(serializedNode.getConcept());
        Node node = getNodeInstantiator().instantiate(concept, serializedNode);
        serializedNode.getProperties().forEach(serializedPropertyValue ->{
            Property property = concept.getPropertyByMetaPointer(serializedPropertyValue.getMetaPointer());
            Objects.requireNonNull(property, "Property with metaPointer " + serializedPropertyValue.getMetaPointer() + " not found");
            Object unserializedValue = primitiveValuesSerialization.unserialize(property.getType().getID(), serializedPropertyValue.getValue());
            node.setPropertyValue(property, unserializedValue);
        });
        return node;
    }

    private void populateNode(SerializedNode serializedNode, NodeResolver nodeResolver) {
        Node node = nodeResolver.strictlyResolve(serializedNode.getID());
        populateNodeContainments(serializedNode, node, nodeResolver);
        populateNodeReferences(serializedNode, node, nodeResolver);
    }

    private void populateNodeContainments(SerializedNode serializedNode, Node node, NodeResolver nodeResolver) {
        Concept concept = node.getConcept();
        serializedNode.getContainments().forEach(serializedContainmentValue ->{
            Containment containment = concept.getContainmentByMetaPointer(serializedContainmentValue.getMetaPointer());
            Objects.requireNonNull(serializedContainmentValue.getValue(), "The containment value should not be null");
            serializedContainmentValue.getValue().forEach(childNodeID -> {
                Node child = nodeResolver.strictlyResolve(childNodeID);
                node.addChild(containment, child);
            });
        });
    }

    private void populateNodeReferences(SerializedNode serializedNode, Node node, NodeResolver nodeResolver) {
        Concept concept = node.getConcept();
        // TODO resolve references to Nodes in different models
        serializedNode.getReferences().forEach(serializedReferenceValue ->{
            Reference reference = concept.getReferenceByMetaPointer(serializedReferenceValue.getMetaPointer());
            serializedReferenceValue.getValue().forEach(entry -> {
                Node referred = nodeResolver.resolve(entry.getReference());
                if (entry.getReference() != null && referred == null) {
                    throw new IllegalArgumentException("Unable to resolve reference to " + entry.getReference());
                }
                ReferenceValue referenceValue = new ReferenceValue(referred, entry.getResolveInfo());
                node.addReferenceValue(reference, referenceValue);
            });
        });
    }

}
