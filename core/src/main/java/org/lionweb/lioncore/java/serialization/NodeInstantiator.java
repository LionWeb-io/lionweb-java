package org.lionweb.lioncore.java.serialization;

import com.google.gson.JsonObject;
import org.lionweb.lioncore.java.metamodel.*;
import org.lionweb.lioncore.java.model.Node;
import org.lionweb.lioncore.java.model.impl.DynamicNode;
import org.lionweb.lioncore.java.self.LionCore;

import java.util.HashMap;
import java.util.Map;

/**
 * This knows how to instantiate a Node, given the information provided by the unserialization mechanism.
 */
public class NodeInstantiator {

    public interface ConceptSpecificNodeInstantiator<T extends Node> {
        T instantiate(Concept concept, JsonObject data, String id);
    }

    private Map<String, ConceptSpecificNodeInstantiator<?>> customUnserializers = new HashMap<>();
    private ConceptSpecificNodeInstantiator<?> defaultNodeUnserializer = (ConceptSpecificNodeInstantiator<Node>) (concept, data, id) -> {
        throw new IllegalArgumentException("Unable to unserialize node with concept "  + concept);
    };

    public NodeInstantiator enableDynamicNodes() {
        defaultNodeUnserializer = (concept, data, nodeID) -> new DynamicNode(nodeID, concept);
        return this;
    }

    public Node instantiate(Concept concept, JsonObject data, String nodeID) {
        if (customUnserializers.containsKey(concept.getID())) {
            return customUnserializers.get(concept.getID()).instantiate(concept, data, nodeID);
        } else {
            return defaultNodeUnserializer.instantiate(concept, data, nodeID);
        }
    }

    public void registerLionCoreCustomUnserializers() {
        customUnserializers.put(LionCore.getMetamodel().getID(), (concept, data, id) -> new Metamodel(null).setID(id));
        customUnserializers.put(LionCore.getConcept().getID(), (concept, data, id) -> new Concept((String) null).setID(id));
        customUnserializers.put(LionCore.getConceptInterface().getID(), (concept, data, id) -> new ConceptInterface((String) null).setID(id));
        customUnserializers.put(LionCore.getProperty().getID(), (concept, data, id) -> new Property(null, null, id));
        customUnserializers.put(LionCore.getReference().getID(), (concept, data, id) -> new Reference(null, id));
        customUnserializers.put(LionCore.getContainment().getID(), (concept, data, id) -> new Containment(null, id));
        customUnserializers.put(LionCore.getPrimitiveType().getID(), (concept, data, id) -> new PrimitiveType(id));
    }
}