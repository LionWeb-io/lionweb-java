package io.lionweb.lioncore.java.serialization;

import io.lionweb.lioncore.java.versions.LionWebVersion;
import io.lionweb.lioncore.java.language.*;
import io.lionweb.lioncore.java.model.ClassifierInstance;
import io.lionweb.lioncore.java.model.Node;
import io.lionweb.lioncore.java.model.impl.DynamicAnnotationInstance;
import io.lionweb.lioncore.java.model.impl.DynamicNode;
import io.lionweb.lioncore.java.self.LionCore;
import io.lionweb.lioncore.java.serialization.data.SerializedClassifierInstance;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;

/**
 * This knows how to instantiate a Classifier Instance (either a Node or an Annotation Instance),
 * given the information provided by the deserialization mechanism.
 */
public class Instantiator {

  public interface ClassifierSpecificInstantiator<T extends ClassifierInstance<?, ?>> {
    T instantiate(
        Classifier<?, ?> classifier,
        SerializedClassifierInstance serializedClassifierInstance,
        Map<String, ClassifierInstance<?, ?>> deserializedNodesByID,
        Map<Property, Object> propertiesValues);
  }

  private final Map<String, ClassifierSpecificInstantiator<?>> customDeserializers =
      new HashMap<>();
  private ClassifierSpecificInstantiator<?> defaultNodeDeserializer =
      (ClassifierSpecificInstantiator<Node<?>>)
          (classifier, serializedNode, deserializedNodesByID, propertiesValues) -> {
            throw new IllegalArgumentException(
                "Unable to instantiate instance with classifier " + classifier);
          };

  public Instantiator() {}

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

  public ClassifierInstance<?, ?> instantiate(
      Classifier<?, ?> classifier,
      SerializedClassifierInstance serializedClassifierInstance,
      Map<String, ClassifierInstance<?, ?>> deserializedInstancesByID,
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

  public void registerLionCoreCustomDeserializers(@Nonnull LionWebVersion lionWebVersion) {
    customDeserializers.put(
        LionCore.getLanguage(lionWebVersion).getID(),
        (concept, serializedNode, deserializedNodesByID, propertiesValues) ->
            new Language(lionWebVersion).setID(serializedNode.getID()));
    customDeserializers.put(
        LionCore.getConcept(lionWebVersion).getID(),
        (concept, serializedNode, deserializedNodesByID, propertiesValues) ->
            new Concept(lionWebVersion).setID(serializedNode.getID()));
    customDeserializers.put(
        LionCore.getInterface(lionWebVersion).getID(),
        (concept, serializedNode, deserializedNodesByID, propertiesValues) ->
            new Interface(lionWebVersion).setID(serializedNode.getID()));
    customDeserializers.put(
        LionCore.getProperty(lionWebVersion).getID(),
        (concept, serializedNode, deserializedNodesByID, propertiesValues) ->
            new Property(lionWebVersion, null, null, serializedNode.getID()));
    customDeserializers.put(
        LionCore.getReference(lionWebVersion).getID(),
        (concept, serializedNode, deserializedNodesByID, propertiesValues) ->
            new Reference(lionWebVersion, null, serializedNode.getID()));
    customDeserializers.put(
        LionCore.getContainment(lionWebVersion).getID(),
        (concept, serializedNode, deserializedNodesByID, propertiesValues) ->
            new Containment(lionWebVersion, null, serializedNode.getID()));
    customDeserializers.put(
        LionCore.getPrimitiveType(lionWebVersion).getID(),
        (concept, serializedNode, deserializedNodesByID, propertiesValues) ->
            new PrimitiveType(lionWebVersion, serializedNode.getID()));
    customDeserializers.put(
        LionCore.getEnumeration(lionWebVersion).getID(),
        (concept, serializedNode, deserializedNodesByID, propertiesValues) ->
            new Enumeration(lionWebVersion).setID(serializedNode.getID()));
    customDeserializers.put(
        LionCore.getEnumerationLiteral(lionWebVersion).getID(),
        (concept, serializedNode, deserializedNodesByID, propertiesValues) ->
            new EnumerationLiteral(lionWebVersion).setID(serializedNode.getID()));
    customDeserializers.put(
        LionCore.getAnnotation(lionWebVersion).getID(),
        (concept, serializedNode, deserializedNodesByID, propertiesValues) ->
            new Annotation(lionWebVersion).setID(serializedNode.getID()));
  }
}
