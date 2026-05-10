package io.lionweb.serialization;

import static io.lionweb.utils.Autoresolve.LIONCOREBUILTINS_AUTORESOLVE_PREFIX;
import static io.lionweb.utils.Autoresolve.LIONCORE_AUTORESOLVE_PREFIX;

import io.lionweb.LionWebVersion;
import io.lionweb.api.ClassifierInstanceResolver;
import io.lionweb.language.*;
import io.lionweb.lioncore.LionCore;
import io.lionweb.model.ClassifierInstance;
import io.lionweb.model.Node;
import io.lionweb.model.ReferenceValue;
import io.lionweb.serialization.data.SerializedClassifierInstance;
import io.lionweb.serialization.data.SerializedContainmentValue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * This helper class take care of populating containments and references of a node, while we are
 * deserializing it.
 */
class NodePopulator {
  private final AbstractSerialization serialization;
  private final ClassifierInstanceResolver classifierInstanceResolver;
  private final DeserializationStatus deserializationStatus;

  // If there are references to builtins which are broken, we will try to resolve them to this
  // version
  private Map<String, Node> autoResolveMap = new HashMap<>();

  NodePopulator(
      AbstractSerialization serialization,
      ClassifierInstanceResolver classifierInstanceResolver,
      DeserializationStatus deserializationStatus) {
    this(
        serialization,
        classifierInstanceResolver,
        deserializationStatus,
        LionWebVersion.currentVersion);
  }

  NodePopulator(
      AbstractSerialization serialization,
      ClassifierInstanceResolver classifierInstanceResolver,
      DeserializationStatus deserializationStatus,
      LionWebVersion autoResolveVersion) {
    this.serialization = serialization;
    this.classifierInstanceResolver = classifierInstanceResolver;
    this.deserializationStatus = deserializationStatus;

    LionCoreBuiltins lionCoreBuiltins = LionCoreBuiltins.getInstance(autoResolveVersion);
    lionCoreBuiltins
        .getElements()
        .forEach(
            element -> {
              // See
              // https://lionweb.io/specification/2024.1/metametamodel/metametamodel.html#predefined-builtins-keys
              autoResolveMap.put(LIONCOREBUILTINS_AUTORESOLVE_PREFIX + element.getName(), element);
            });
    Language lionCore = LionCore.getInstance(autoResolveVersion);
    lionCore
        .getElements()
        .forEach(
            element -> {
              autoResolveMap.put(LIONCORE_AUTORESOLVE_PREFIX + element.getName(), element);
            });
  }

  void populateClassifierInstance(
      ClassifierInstance<?> node, SerializedClassifierInstance serializedClassifierInstance) {
    populateContainments(node, serializedClassifierInstance);
    populateNodeReferences(node, serializedClassifierInstance);
  }

  private void populateContainments(
      ClassifierInstance<?> node, SerializedClassifierInstance serializedClassifierInstance) {
    Classifier<?> concept = node.getClassifier();
    boolean isProxy =
        serialization.getUnavailableChildrenPolicy() == UnavailableNodePolicy.PROXY_NODES;
    List<SerializedContainmentValue> containments = serializedClassifierInstance.getContainments();
    for (int ci = 0, cn = containments.size(); ci < cn; ci++) {
      SerializedContainmentValue serializedContainmentValue = containments.get(ci);
      Containment containment =
          deserializationStatus.getContainment(
              concept, serializedContainmentValue.getMetaPointer());
      if (containment == null) {
        throw new NullPointerException(
            "Unable to resolve containment "
                + serializedContainmentValue.getMetaPointer()
                + " in concept "
                + concept);
      }
      List<String> childrenIds = serializedContainmentValue.getChildrenIds();
      Objects.requireNonNull(childrenIds, "The containment value should not be null");
      List<ClassifierInstance<?>> deserializedValue = new ArrayList<>(childrenIds.size());
      for (int i = 0, n = childrenIds.size(); i < n; i++) {
        String childNodeID = childrenIds.get(i);
        deserializedValue.add(
            isProxy
                ? classifierInstanceResolver.resolveOrProxy(childNodeID)
                : classifierInstanceResolver.strictlyResolve(childNodeID));
      }
      if (!Objects.equals(deserializedValue, node.getChildren(containment))) {
        for (int i = 0, n = deserializedValue.size(); i < n; i++) {
          node.addChild(containment, (Node) deserializedValue.get(i));
        }
      }
    }
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
                  deserializationStatus.getReference(
                      concept, serializedReferenceValue.getMetaPointer());
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

                        if (entry.getReference() == null) {
                          referred = autoResolveMap.get(entry.getResolveInfo());
                        }
                        if (referred == null && entry.getReference() != null) {
                          // Here we are only interested in references there were set, but to
                          // Nodes we cannot
                          // find
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
