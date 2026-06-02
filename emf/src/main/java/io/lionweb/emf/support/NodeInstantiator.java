package io.lionweb.emf.support;

import io.lionweb.language.*;
import io.lionweb.model.Node;
import io.lionweb.model.impl.DynamicNode;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.eclipse.emf.ecore.EObject;
import org.jetbrains.annotations.NotNull;

/**
 * This knows how to instantiate a Node, given the information provided by the deserialization
 * mechanism.
 */
public class NodeInstantiator {

  /** Creates a concrete {@link Node} subtype for a specific Concept during EMF import. */
  public interface ConceptSpecificNodeInstantiator<T extends Node> {
    @NotNull
    T instantiate(
        @NotNull Concept concept,
        @NotNull EObject emfObject,
        @NotNull Map<String, Node> deserializedNodesByID,
        @NotNull Map<Property, Object> propertiesValues);
  }

  private final Map<String, ConceptSpecificNodeInstantiator<?>> customDeserializers =
      new HashMap<>();
  private ConceptSpecificNodeInstantiator<?> defaultNodeDeserializer =
      (ConceptSpecificNodeInstantiator<Node>)
          (concept, serializedNode, deserializedNodesByID, propertiesValues) -> {
            throw new IllegalArgumentException(
                "Unable to deserialize node with concept " + concept);
          };

  public @NotNull NodeInstantiator enableDynamicNodes() {
    defaultNodeDeserializer =
        (concept, emfObject, deserializedNodesByID, propertiesValues) ->
            new DynamicNode(null, concept);
    return this;
  }

  public @NotNull Node instantiate(
      @NotNull Concept concept,
      @NotNull EObject eObject,
      @NotNull Map<String, Node> deserializedNodesByID,
      @NotNull Map<Property, Object> propertiesValues) {
    Objects.requireNonNull(concept, "concept cannot be null");
    Objects.requireNonNull(eObject, "eObject cannot be null");
    Objects.requireNonNull(deserializedNodesByID, "deserializedNodesByID cannot be null");
    Objects.requireNonNull(propertiesValues, "propertiesValues cannot be null");
    if (customDeserializers.containsKey(concept.getID())) {
      return customDeserializers
          .get(concept.getID())
          .instantiate(concept, eObject, deserializedNodesByID, propertiesValues);
    } else {
      return defaultNodeDeserializer.instantiate(
          concept, eObject, deserializedNodesByID, propertiesValues);
    }
  }

  public @NotNull NodeInstantiator registerCustomDeserializer(
      @NotNull String conceptID,
      @NotNull ConceptSpecificNodeInstantiator<?> conceptSpecificNodeInstantiator) {
    Objects.requireNonNull(conceptID, "conceptID cannot be null");
    Objects.requireNonNull(
        conceptSpecificNodeInstantiator, "conceptSpecificNodeInstantiator cannot be null");
    customDeserializers.put(conceptID, conceptSpecificNodeInstantiator);
    return this;
  }
}
