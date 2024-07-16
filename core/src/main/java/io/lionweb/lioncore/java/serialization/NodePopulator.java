package io.lionweb.lioncore.java.serialization;

import io.lionweb.lioncore.java.api.ClassifierInstanceResolver;
import io.lionweb.lioncore.java.language.Classifier;
import io.lionweb.lioncore.java.language.Containment;
import io.lionweb.lioncore.java.language.Reference;
import io.lionweb.lioncore.java.model.ClassifierInstance;
import io.lionweb.lioncore.java.model.Node;
import io.lionweb.lioncore.java.model.ReferenceValue;
import io.lionweb.lioncore.java.serialization.data.SerializedClassifierInstance;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * This helper class take care of populating containments and references of a node, while we are
 * deserializing it.
 */
class NodePopulator {
  private final AbstractSerialization serialization;
  private final ClassifierInstanceResolver classifierInstanceResolver;
  private final DeserializationStatus deserializationStatus;

  NodePopulator(
      AbstractSerialization serialization,
      ClassifierInstanceResolver classifierInstanceResolver,
      DeserializationStatus deserializationStatus) {
    this.serialization = serialization;
    this.classifierInstanceResolver = classifierInstanceResolver;
    this.deserializationStatus = deserializationStatus;
  }

  void populateClassifierInstance(
      ClassifierInstance<?> node, SerializedClassifierInstance serializedClassifierInstance) {
    populateContainments(node, serializedClassifierInstance);
    populateNodeReferences(node, serializedClassifierInstance);
  }

  private void populateContainments(
      ClassifierInstance<?> node, SerializedClassifierInstance serializedClassifierInstance) {
    Classifier<?> concept = node.getClassifier();
    serializedClassifierInstance
        .getContainments()
        .forEach(
            serializedContainmentValue -> {
              Containment containment =
                  concept.getContainmentByMetaPointer(serializedContainmentValue.getMetaPointer());
              Objects.requireNonNull(
                  containment,
                  "Unable to resolve containment "
                      + serializedContainmentValue.getMetaPointer()
                      + " in concept "
                      + concept);
              Objects.requireNonNull(
                  serializedContainmentValue.getValue(),
                  "The containment value should not be null");
              List<ClassifierInstance<?>> deserializedValue =
                  serializedContainmentValue.getValue().stream()
                      .map(
                          childNodeID -> {
                            if (serialization.getUnavailableChildrenPolicy()
                                == UnavailableNodePolicy.PROXY_NODES) {
                              return classifierInstanceResolver.resolveOrProxy(childNodeID);
                            } else {
                              return classifierInstanceResolver.strictlyResolve(childNodeID);
                            }
                          })
                      .collect(Collectors.toList());
              if (!Objects.equals(deserializedValue, node.getChildren(containment))) {
                deserializedValue.forEach(child -> node.addChild(containment, (Node) child));
              }
            });
  }

  private void populateNodeReferences(
      ClassifierInstance<?> node, SerializedClassifierInstance serializedClassifierInstance) {
    Classifier<?> concept = node.getClassifier();
    // TODO resolve references to Nodes in different models
    serializedClassifierInstance
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
                        + serializedClassifierInstance);
              }
              serializedReferenceValue
                  .getValue()
                  .forEach(
                      entry -> {
                        Node referred =
                            (Node) classifierInstanceResolver.resolve(entry.getReference());
                        if (entry.getReference() != null && referred == null) {
                          switch (serialization.getUnavailableReferenceTargetPolicy()) {
                            case NULL_REFERENCES:
                              referred = null;
                              break;
                            case PROXY_NODES:
                              referred = deserializationStatus.resolve(entry.getReference());
                              break;
                            case THROW_ERROR:
                              throw new DeserializationException(
                                  "Unable to resolve reference to "
                                      + entry.getReference()
                                      + " for feature "
                                      + serializedReferenceValue.getMetaPointer());
                          }
                        }
                        ReferenceValue referenceValue =
                            new ReferenceValue(referred, entry.getResolveInfo());
                        node.addReferenceValue(reference, referenceValue);
                      });
            });
  }
}
