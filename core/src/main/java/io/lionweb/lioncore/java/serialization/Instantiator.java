package io.lionweb.lioncore.java.serialization;

import io.lionweb.lioncore.java.language.*;
import io.lionweb.lioncore.java.model.ClassifierInstance;
import io.lionweb.lioncore.java.model.Node;
import io.lionweb.lioncore.java.model.impl.DynamicAnnotationInstance;
import io.lionweb.lioncore.java.model.impl.DynamicNode;
import io.lionweb.lioncore.java.self.LionCore;
import io.lionweb.lioncore.java.serialization.data.SerializedClassifierInstance;
import java.util.HashMap;
import java.util.Map;

/**
 * This knows how to instantiate a Classifier Instance (either a Node or an Annotation Instance),
 * given the information provided by the deserialization mechanism.
 */
public class Instantiator {

  public interface ClassifierSpecificInstantiator<T extends ClassifierInstance<?>> {
    T instantiate(
        Classifier<?> classifier,
        SerializedClassifierInstance serializedClassifierInstance,
        Map<String, ClassifierInstance<?>> deserializedNodesByID,
        Map<Property, Object> propertiesValues);
  }

  private final Map<String, ClassifierSpecificInstantiator<?>> customDeserializers =
      new HashMap<>();
  private ClassifierSpecificInstantiator<?> defaultNodeDeserializer =
      (ClassifierSpecificInstantiator<Node>)
          (classifier, serializedNode, deserializedNodesByID, propertiesValues) -> {
            throw new IllegalArgumentException(
                "Unable to instantiate instance with classifier " + classifier);
          };

  public Instantiator enableDynamicNodes() {
    defaultNodeDeserializer =
        (classifier, serializedNode, deserializedNodesByID, propertiesValues) -> {
          if (classifier instanceof Concept) {
            return new DynamicNode(serializedNode.getID(), (Concept) classifier);
          } else if (classifier instanceof Annotation) {
            return new DynamicAnnotationInstance(serializedNode.getID(), (Annotation) classifier);
          } else {
            throw new IllegalStateException();
          }
        };
    return this;
  }

  public ClassifierInstance<?> instantiate(
      Classifier<?> classifier,
      SerializedClassifierInstance serializedClassifierInstance,
      Map<String, ClassifierInstance<?>> deserializedInstancesByID,
      Map<Property, Object> propertiesValues) {
    if (customDeserializers.containsKey(classifier.getID())) {
      return customDeserializers
          .get(classifier.getID())
          .instantiate(
              classifier,
              serializedClassifierInstance,
              deserializedInstancesByID,
              propertiesValues);
    } else {
      return defaultNodeDeserializer.instantiate(
          classifier, serializedClassifierInstance, deserializedInstancesByID, propertiesValues);
    }
  }

  public Instantiator registerCustomDeserializer(
      String classifierID, ClassifierSpecificInstantiator<?> classifierSpecificInstantiator) {
    customDeserializers.put(classifierID, classifierSpecificInstantiator);
    return this;
  }

  public void registerLionCoreCustomDeserializers() {
    customDeserializers.put(
        LionCore.getLanguage().getID(),
        (concept, serializedNode, deserializedNodesByID, propertiesValues) ->
            new Language().setID(serializedNode.getID()));
    customDeserializers.put(
        LionCore.getConcept().getID(),
        (concept, serializedNode, deserializedNodesByID, propertiesValues) ->
            new Concept().setID(serializedNode.getID()));
    customDeserializers.put(
        LionCore.getInterface().getID(),
        (concept, serializedNode, deserializedNodesByID, propertiesValues) ->
            new Interface().setID(serializedNode.getID()));
    customDeserializers.put(
        LionCore.getProperty().getID(),
        (concept, serializedNode, deserializedNodesByID, propertiesValues) ->
            new Property(null, null, serializedNode.getID()));
    customDeserializers.put(
        LionCore.getReference().getID(),
        (concept, serializedNode, deserializedNodesByID, propertiesValues) ->
            new Reference(null, serializedNode.getID()));
    customDeserializers.put(
        LionCore.getContainment().getID(),
        (concept, serializedNode, deserializedNodesByID, propertiesValues) ->
            new Containment(null, serializedNode.getID()));
    customDeserializers.put(
        LionCore.getPrimitiveType().getID(),
        (concept, serializedNode, deserializedNodesByID, propertiesValues) ->
            new PrimitiveType(serializedNode.getID()));
    customDeserializers.put(
        LionCore.getEnumeration().getID(),
        (concept, serializedNode, deserializedNodesByID, propertiesValues) ->
            new Enumeration().setID(serializedNode.getID()));
    customDeserializers.put(
        LionCore.getEnumerationLiteral().getID(),
        (concept, serializedNode, deserializedNodesByID, propertiesValues) ->
            new EnumerationLiteral().setID(serializedNode.getID()));
    customDeserializers.put(
        LionCore.getAnnotation().getID(),
        (concept, serializedNode, deserializedNodesByID, propertiesValues) ->
            new Annotation().setID(serializedNode.getID()));
  }
}
