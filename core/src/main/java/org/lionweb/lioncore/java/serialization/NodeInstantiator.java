package org.lionweb.lioncore.java.serialization;

import com.google.gson.JsonObject;
import org.lionweb.lioncore.java.metamodel.*;
import org.lionweb.lioncore.java.model.Node;
import org.lionweb.lioncore.java.model.impl.DynamicNode;
import org.lionweb.lioncore.java.self.LionCore;
import org.lionweb.lioncore.java.serialization.data.SerializedNode;

import java.util.HashMap;
import java.util.Map;

/**
 * This knows how to instantiate a Node, given the information provided by the unserialization mechanism.
 */
public class NodeInstantiator {

    public interface ConceptSpecificNodeInstantiator<T extends Node> {
        T instantiate(Concept concept, SerializedNode serializedNode);
    }

    private Map<String, ConceptSpecificNodeInstantiator<?>> customUnserializers = new HashMap<>();
    private ConceptSpecificNodeInstantiator<?> defaultNodeUnserializer = (ConceptSpecificNodeInstantiator<Node>) (concept, serializedNode) -> {
        throw new IllegalArgumentException("Unable to unserialize node with concept "  + concept);
    };

    public NodeInstantiator enableDynamicNodes() {
        defaultNodeUnserializer = (concept, serializedNode) -> new DynamicNode(serializedNode.getID(), concept);
        return this;
    }

    public Node instantiate(Concept concept, SerializedNode serializedNode) {
        if (customUnserializers.containsKey(concept.getID())) {
            return customUnserializers.get(concept.getID()).instantiate(concept, serializedNode);
        } else {
            return defaultNodeUnserializer.instantiate(concept, serializedNode);
        }
    }

    public NodeInstantiator registerCustomUnserializer(String conceptID, ConceptSpecificNodeInstantiator<?> conceptSpecificNodeInstantiator) {
        customUnserializers.put(conceptID, conceptSpecificNodeInstantiator);
        return this;
    }

    public void registerLionCoreCustomUnserializers() {
        customUnserializers.put(LionCore.getMetamodel().getID(), (concept, serializedNode) -> new Metamodel().setID(serializedNode.getID()));
        customUnserializers.put(LionCore.getConcept().getID(), (concept, serializedNode) -> new Concept((String) null).setID(serializedNode.getID()));
        customUnserializers.put(LionCore.getConceptInterface().getID(), (concept, serializedNode) -> new ConceptInterface((String) null).setID(serializedNode.getID()));
        customUnserializers.put(LionCore.getProperty().getID(), (concept, serializedNode) -> new Property(null, null, serializedNode.getID()));
        customUnserializers.put(LionCore.getReference().getID(), (concept, serializedNode) -> new Reference(null, serializedNode.getID()));
        customUnserializers.put(LionCore.getContainment().getID(), (concept, serializedNode) -> new Containment(null, serializedNode.getID()));
        customUnserializers.put(LionCore.getPrimitiveType().getID(), (concept, serializedNode) -> new PrimitiveType(serializedNode.getID()));
        customUnserializers.put(LionCore.getEnumeration().getID(), (concept, serializedNode) -> new Enumeration().setID(serializedNode.getID()));
        customUnserializers.put(LionCore.getEnumerationLiteral().getID(), (concept, serializedNode) -> new EnumerationLiteral().setID(serializedNode.getID()));
    }
}