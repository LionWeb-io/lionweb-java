package io.lionweb.lioncore.java.emf;

import io.lionweb.lioncore.java.metamodel.*;
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

  //  public void registerLionCoreCustomUnserializers() {
  //    customUnserializers.put(
  //        LionCore.getMetamodel().getID(),
  //        (concept, serializedNode, unserializedNodesByID, propertiesValues) ->
  //            new Metamodel().setID(serializedNode.getID()));
  //    customUnserializers.put(
  //        LionCore.getConcept().getID(),
  //        (concept, serializedNode, unserializedNodesByID, propertiesValues) ->
  //            new Concept((String) null).setID(serializedNode.getID()));
  //    customUnserializers.put(
  //        LionCore.getConceptInterface().getID(),
  //        (concept, serializedNode, unserializedNodesByID, propertiesValues) ->
  //            new ConceptInterface((String) null).setID(serializedNode.getID()));
  //    customUnserializers.put(
  //        LionCore.getProperty().getID(),
  //        (concept, serializedNode, unserializedNodesByID, propertiesValues) ->
  //            new Property(null, null, serializedNode.getID()));
  //    customUnserializers.put(
  //        LionCore.getReference().getID(),
  //        (concept, serializedNode, unserializedNodesByID, propertiesValues) ->
  //            new Reference(null, serializedNode.getID()));
  //    customUnserializers.put(
  //        LionCore.getContainment().getID(),
  //        (concept, serializedNode, unserializedNodesByID, propertiesValues) ->
  //            new Containment(null, serializedNode.getID()));
  //    customUnserializers.put(
  //        LionCore.getPrimitiveType().getID(),
  //        (concept, serializedNode, unserializedNodesByID, propertiesValues) ->
  //            new PrimitiveType(serializedNode.getID()));
  //    customUnserializers.put(
  //        LionCore.getEnumeration().getID(),
  //        (concept, serializedNode, unserializedNodesByID, propertiesValues) ->
  //            new Enumeration().setID(serializedNode.getID()));
  //    customUnserializers.put(
  //        LionCore.getEnumerationLiteral().getID(),
  //        (concept, serializedNode, unserializedNodesByID, propertiesValues) ->
  //            new EnumerationLiteral().setID(serializedNode.getID()));
  //  }
}
