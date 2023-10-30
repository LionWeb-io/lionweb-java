package io.lionweb.lioncore.java.emf.support;

import io.lionweb.lioncore.java.language.*;
import io.lionweb.lioncore.java.model.Node;
import io.lionweb.lioncore.java.model.impl.DynamicNode;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.emf.ecore.EObject;

/**
 * This knows how to instantiate a Node, given the information provided by the deserialization
 * mechanism.
 */
public class NodeInstantiator {

  public interface ConceptSpecificNodeInstantiator<T extends Node> {
    T instantiate(
        Concept concept,
        EObject emfObject,
        Map<String, Node> deserializedNodesByID,
        Map<Property, Object> propertiesValues);
  }

  private final Map<String, ConceptSpecificNodeInstantiator<?>> customDeserializers =
      new HashMap<>();
  private ConceptSpecificNodeInstantiator<?> defaultNodeDeserializer =
      (ConceptSpecificNodeInstantiator<Node>)
          (concept, serializedNode, deserializedNodesByID, propertiesValues) -> {
            throw new IllegalArgumentException(
                "Unable to deserialize node with concept " + concept);
          };

  public NodeInstantiator enableDynamicNodes() {
    defaultNodeDeserializer =
        (concept, emfObject, deserializedNodesByID, propertiesValues) ->
            new DynamicNode(null, concept);
    return this;
  }

  public Node instantiate(
      Concept concept,
      EObject eObject,
      Map<String, Node> deserializedNodesByID,
      Map<Property, Object> propertiesValues) {
    if (customDeserializers.containsKey(concept.getID())) {
      return customDeserializers
          .get(concept.getID())
          .instantiate(concept, eObject, deserializedNodesByID, propertiesValues);
    } else {
      return defaultNodeDeserializer.instantiate(
          concept, eObject, deserializedNodesByID, propertiesValues);
    }
  }

  public NodeInstantiator registerCustomDeserializer(
      String conceptID, ConceptSpecificNodeInstantiator<?> conceptSpecificNodeInstantiator) {
    customDeserializers.put(conceptID, conceptSpecificNodeInstantiator);
    return this;
  }
}
