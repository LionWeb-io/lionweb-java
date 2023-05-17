package io.lionweb.lioncore.java.emf.support;

import io.lionweb.lioncore.java.language.*;
import io.lionweb.lioncore.java.model.Node;
import io.lionweb.lioncore.java.model.impl.DynamicNode;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.emf.ecore.EObject;

/**
 * This knows how to instantiate a Node, given the information provided by the unserialization
 * mechanism.
 */
public class NodeInstantiator {

  public interface ConceptSpecificNodeInstantiator<T extends Node> {
    T instantiate(
        Concept concept,
        EObject emfObject,
        Map<String, Node> unserializedNodesByID,
        Map<Property, Object> propertiesValues);
  }

  private Map<String, ConceptSpecificNodeInstantiator<?>> customUnserializers = new HashMap<>();
  private ConceptSpecificNodeInstantiator<?> defaultNodeUnserializer =
      (ConceptSpecificNodeInstantiator<Node>)
          (concept, serializedNode, unserializedNodesByID, propertiesValues) -> {
            throw new IllegalArgumentException(
                "Unable to unserialize node with concept " + concept);
          };

  public NodeInstantiator enableDynamicNodes() {
    defaultNodeUnserializer =
        (concept, emfObject, unserializedNodesByID, propertiesValues) ->
            new DynamicNode(null, concept);
    return this;
  }

  public Node instantiate(
      Concept concept,
      EObject eObject,
      Map<String, Node> unserializedNodesByID,
      Map<Property, Object> propertiesValues) {
    if (customUnserializers.containsKey(concept.getID())) {
      return customUnserializers
          .get(concept.getID())
          .instantiate(concept, eObject, unserializedNodesByID, propertiesValues);
    } else {
      return defaultNodeUnserializer.instantiate(
          concept, eObject, unserializedNodesByID, propertiesValues);
    }
  }

  public NodeInstantiator registerCustomUnserializer(
      String conceptID, ConceptSpecificNodeInstantiator<?> conceptSpecificNodeInstantiator) {
    customUnserializers.put(conceptID, conceptSpecificNodeInstantiator);
    return this;
  }
}
