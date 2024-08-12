package io.lionweb.lioncore.java.serialization;

import com.google.common.collect.Sets;
import io.lionweb.lioncore.java.api.ClassifierInstanceResolver;
import io.lionweb.lioncore.java.api.CompositeClassifierInstanceResolver;
import io.lionweb.lioncore.java.api.LocalClassifierInstanceResolver;
import io.lionweb.lioncore.java.language.*;
import io.lionweb.lioncore.java.model.AnnotationInstance;
import io.lionweb.lioncore.java.model.ClassifierInstance;
import io.lionweb.lioncore.java.model.HasSettableParent;
import io.lionweb.lioncore.java.model.impl.AbstractClassifierInstance;
import io.lionweb.lioncore.java.model.impl.ProxyNode;
import io.lionweb.lioncore.java.serialization.data.*;
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This is a common ancestor to all Serialization classes. It contains logic to move between the
 * actual Nodes and the intermediate format (SerializedChunk). The step between the SerializedChunk
 * and the actual physical formats is done in other classes.
 */
public abstract class AbstractSerialization {
  public static final String DEFAULT_SERIALIZATION_FORMAT = "2023.1";

  protected ClassifierResolver classifierResolver;
  protected Instantiator instantiator;
  protected PrimitiveValuesSerialization primitiveValuesSerialization;

  protected LocalClassifierInstanceResolver instanceResolver;

  /**
   * This guides what we do when deserializing a sub-tree and not being able to resolve the parent.
   */
  protected UnavailableNodePolicy unavailableParentPolicy = UnavailableNodePolicy.THROW_ERROR;

  /**
   * This guides what we do when deserializing a sub-tree and not being able to resolve the
   * children.
   */
  protected UnavailableNodePolicy unavailableChildrenPolicy = UnavailableNodePolicy.THROW_ERROR;

  /**
   * This guides what we do when deserializing a sub-tree and not being able to resolve a reference
   * target.
   */
  protected UnavailableNodePolicy unavailableReferenceTargetPolicy =
      UnavailableNodePolicy.THROW_ERROR;

  protected AbstractSerialization() {
    // prevent public access
    classifierResolver = new ClassifierResolver();
    instantiator = new Instantiator();
    primitiveValuesSerialization = new PrimitiveValuesSerialization();
    instanceResolver = new LocalClassifierInstanceResolver();
  }

  //
  // Configuration
  //

  public ClassifierResolver getClassifierResolver() {
    return classifierResolver;
  }

  public void setClassifierResolver(ClassifierResolver classifierResolver) {
    this.classifierResolver = classifierResolver;
  }

  public void setInstantiator(Instantiator instantiator) {
    this.instantiator = instantiator;
  }

  public void setPrimitiveValuesSerialization(
      PrimitiveValuesSerialization primitiveValuesSerialization) {
    this.primitiveValuesSerialization = primitiveValuesSerialization;
  }

  public void setInstanceResolver(LocalClassifierInstanceResolver instanceResolver) {
    this.instanceResolver = instanceResolver;
  }

  public Instantiator getInstantiator() {
    return instantiator;
  }

  public PrimitiveValuesSerialization getPrimitiveValuesSerialization() {
    return primitiveValuesSerialization;
  }

  public LocalClassifierInstanceResolver getInstanceResolver() {
    return instanceResolver;
  }

  public void enableDynamicNodes() {
    instantiator.enableDynamicNodes();
    primitiveValuesSerialization.enableDynamicNodes();
  }

  public @Nonnull UnavailableNodePolicy getUnavailableParentPolicy() {
    return this.unavailableParentPolicy;
  }

  public @Nonnull UnavailableNodePolicy getUnavailableReferenceTargetPolicy() {
    return this.unavailableReferenceTargetPolicy;
  }

  public @Nonnull UnavailableNodePolicy getUnavailableChildrenPolicy() {
    return this.unavailableChildrenPolicy;
  }

  public void setUnavailableParentPolicy(@Nonnull UnavailableNodePolicy unavailableParentPolicy) {
    Objects.requireNonNull(unavailableParentPolicy);
    this.unavailableParentPolicy = unavailableParentPolicy;
  }

  public void setUnavailableChildrenPolicy(
      @Nonnull UnavailableNodePolicy unavailableChildrenPolicy) {
    Objects.requireNonNull(unavailableChildrenPolicy);
    this.unavailableChildrenPolicy = unavailableChildrenPolicy;
  }

  public void setUnavailableReferenceTargetPolicy(
      @Nonnull UnavailableNodePolicy unavailableReferenceTargetPolicy) {
    Objects.requireNonNull(unavailableReferenceTargetPolicy);
    this.unavailableReferenceTargetPolicy = unavailableReferenceTargetPolicy;
  }

  public void registerLanguage(Language language) {
    getClassifierResolver().registerLanguage(language);
    getPrimitiveValuesSerialization().registerLanguage(language);
  }

  //
  // Serialization to chunk
  //

  public SerializedChunk serializeTreeToSerializationBlock(ClassifierInstance<?> root) {
    Set<ClassifierInstance<?>> classifierInstances = new LinkedHashSet<>();
    ClassifierInstance.collectSelfAndDescendants(root, true, classifierInstances);
    return serializeNodesToSerializationBlock(classifierInstances);
  }

  public SerializedChunk serializeNodesToSerializationBlock(
      Collection<ClassifierInstance<?>> classifierInstances) {
    SerializedChunk serializedChunk = new SerializedChunk();
    serializedChunk.setSerializationFormatVersion(DEFAULT_SERIALIZATION_FORMAT);
    for (ClassifierInstance<?> classifierInstance : classifierInstances) {
      Objects.requireNonNull(classifierInstance, "nodes should not contain null values");
      serializedChunk.addClassifierInstance(serializeNode(classifierInstance));
      classifierInstance.getAnnotations().stream()
          .filter(a -> !classifierInstances.contains(a))
          .forEach(
              annotationInstance -> {
                serializedChunk.addClassifierInstance(
                    serializeAnnotationInstance(annotationInstance));
                considerLanguageDuringSerialization(
                    serializedChunk, annotationInstance.getClassifier().getLanguage());
              });
      Objects.requireNonNull(
          classifierInstance.getClassifier(),
          "A node should have a concept in order to be serialized");
      Objects.requireNonNull(
          classifierInstance.getClassifier().getLanguage(),
          "A Concept should be part of a Language in order to be serialized. Concept "
              + classifierInstance.getClassifier()
              + " is not");
      considerLanguageDuringSerialization(
          serializedChunk, classifierInstance.getClassifier().getLanguage());
      classifierInstance
          .getClassifier()
          .allFeatures()
          .forEach(
              f -> considerLanguageDuringSerialization(serializedChunk, f.getDeclaringLanguage()));
      classifierInstance
          .getClassifier()
          .allProperties()
          .forEach(
              p -> considerLanguageDuringSerialization(serializedChunk, p.getType().getLanguage()));
      classifierInstance
          .getClassifier()
          .allLinks()
          .forEach(
              l -> considerLanguageDuringSerialization(serializedChunk, l.getType().getLanguage()));
    }
    return serializedChunk;
  }

  private void considerLanguageDuringSerialization(
      SerializedChunk serializedChunk, Language language) {
    registerLanguage(language);
    UsedLanguage languageKeyVersion = UsedLanguage.fromLanguage(language);
    if (!serializedChunk.getLanguages().contains(languageKeyVersion)) {
      serializedChunk.addLanguage(languageKeyVersion);
    }
  }

  public SerializedChunk serializeNodesToSerializationBlock(
      ClassifierInstance<?>... classifierInstances) {
    return serializeNodesToSerializationBlock(Arrays.asList(classifierInstances));
  }

  private SerializedClassifierInstance serializeNode(
      @Nonnull ClassifierInstance<?> classifierInstance) {
    Objects.requireNonNull(classifierInstance, "Node should not be null");
    SerializedClassifierInstance serializedClassifierInstance = new SerializedClassifierInstance();
    serializedClassifierInstance.setID(classifierInstance.getID());
    serializedClassifierInstance.setClassifier(
        MetaPointer.from(classifierInstance.getClassifier()));
    if (classifierInstance.getParent() != null) {
      serializedClassifierInstance.setParentNodeID(classifierInstance.getParent().getID());
    }
    serializeProperties(classifierInstance, serializedClassifierInstance);
    serializeContainments(classifierInstance, serializedClassifierInstance);
    serializeReferences(classifierInstance, serializedClassifierInstance);
    serializeAnnotations(classifierInstance, serializedClassifierInstance);
    return serializedClassifierInstance;
  }

  private SerializedClassifierInstance serializeAnnotationInstance(
      @Nonnull AnnotationInstance annotationInstance) {
    Objects.requireNonNull(annotationInstance, "AnnotationInstance should not be null");
    SerializedClassifierInstance serializedClassifierInstance = new SerializedClassifierInstance();
    serializedClassifierInstance.setID(annotationInstance.getID());
    serializedClassifierInstance.setParentNodeID(annotationInstance.getParent().getID());
    serializedClassifierInstance.setClassifier(
        MetaPointer.from(annotationInstance.getAnnotationDefinition()));
    serializeProperties(annotationInstance, serializedClassifierInstance);
    serializeContainments(annotationInstance, serializedClassifierInstance);
    serializeReferences(annotationInstance, serializedClassifierInstance);
    serializeAnnotations(annotationInstance, serializedClassifierInstance);
    return serializedClassifierInstance;
  }

  private static void serializeAnnotations(
      @Nonnull ClassifierInstance<?> classifierInstance,
      SerializedClassifierInstance serializedClassifierInstance) {
    Objects.requireNonNull(classifierInstance, "ClassifierInstance should not be null");
    serializedClassifierInstance.setAnnotations(
        classifierInstance.getAnnotations().stream()
            .map(a -> a.getID())
            .collect(Collectors.toList()));
  }

  private static void serializeReferences(
      @Nonnull ClassifierInstance<?> classifierInstance,
      SerializedClassifierInstance serializedClassifierInstance) {
    Objects.requireNonNull(classifierInstance, "ClassifierInstance should not be null");
    classifierInstance
        .getClassifier()
        .allReferences()
        .forEach(
            reference -> {
              SerializedReferenceValue referenceValue = new SerializedReferenceValue();
              referenceValue.setMetaPointer(
                  MetaPointer.from(
                      reference, ((LanguageEntity) reference.getContainer()).getLanguage()));
              referenceValue.setValue(
                  classifierInstance.getReferenceValues(reference).stream()
                      .map(
                          rv -> {
                            String referredID =
                                rv.getReferred() == null ? null : rv.getReferred().getID();
                            return new SerializedReferenceValue.Entry(
                                referredID, rv.getResolveInfo());
                          })
                      .collect(Collectors.toList()));
              serializedClassifierInstance.addReferenceValue(referenceValue);
            });
  }

  private static void serializeContainments(
      @Nonnull ClassifierInstance<?> classifierInstance,
      SerializedClassifierInstance serializedClassifierInstance) {
    Objects.requireNonNull(classifierInstance, "ClassifierInstance should not be null");
    classifierInstance
        .getClassifier()
        .allContainments()
        .forEach(
            containment -> {
              SerializedContainmentValue containmentValue = new SerializedContainmentValue();
              containmentValue.setMetaPointer(
                  MetaPointer.from(
                      containment, ((LanguageEntity) containment.getContainer()).getLanguage()));
              containmentValue.setValue(
                  classifierInstance.getChildren(containment).stream()
                      .map(c -> c.getID())
                      .collect(Collectors.toList()));
              serializedClassifierInstance.addContainmentValue(containmentValue);
            });
  }

  private void serializeProperties(
      ClassifierInstance<?> classifierInstance,
      SerializedClassifierInstance serializedClassifierInstance) {
    classifierInstance
        .getClassifier()
        .allProperties()
        .forEach(
            property -> {
              SerializedPropertyValue propertyValue = new SerializedPropertyValue();
              propertyValue.setMetaPointer(
                  MetaPointer.from(
                      property, ((LanguageEntity) property.getContainer()).getLanguage()));
              propertyValue.setValue(
                  serializePropertyValue(
                      property.getType(), classifierInstance.getPropertyValue(property)));
              serializedClassifierInstance.addPropertyValue(propertyValue);
            });
  }

  private String serializePropertyValue(@Nonnull DataType dataType, @Nullable Object value) {
    Objects.requireNonNull(dataType == null, "cannot serialize property when the dataType is null");
    Objects.requireNonNull(
        dataType.getID() == null, "cannot serialize property when the dataType.ID is null");
    if (value == null) {
      return null;
    }
    return primitiveValuesSerialization.serialize(dataType.getID(), value);
  }

  //
  // Deserialization - Protected and Private
  //

  protected void validateSerializationBlock(@Nonnull SerializedChunk serializationBlock) {
    Objects.requireNonNull(serializationBlock, "serializationBlock should not be null");
    if (serializationBlock.getSerializationFormatVersion() == null) {
      throw new IllegalArgumentException("The serializationFormatVersion should not be null");
    }
    if (!serializationBlock.getSerializationFormatVersion().equals(DEFAULT_SERIALIZATION_FORMAT)) {
      throw new IllegalArgumentException(
          "Only serializationFormatVersion = '" + DEFAULT_SERIALIZATION_FORMAT + "' is supported");
    }
  }

  /**
   * This method returned a sorted version of the original list, so that leaves nodes comes first,
   * or in other words that a parent never precedes its children.
   */
  private DeserializationStatus sortLeavesFirst(List<SerializedClassifierInstance> originalList) {
    DeserializationStatus deserializationStatus =
        new DeserializationStatus(originalList, instanceResolver);

    // We create the list going from the roots, to their children and so on, and then we will revert
    // the list

    deserializationStatus.putNodesWithNullIDsInFront();

    switch (unavailableParentPolicy) {
      case NULL_REFERENCES:
        {
          // Let's find all the IDs of nodes present here. The nodes with parents not present here
          // are effectively treated as roots and their parent will be set to null, as we cannot
          // retrieve them or set them (until we decide to provide some sort of NodeResolver)
          Set<String> knownIDs =
              originalList.stream().map(ci -> ci.getID()).collect(Collectors.toSet());
          originalList.stream()
              .filter(ci -> !knownIDs.contains(ci.getParentNodeID()))
              .forEach(effectivelyRoot -> deserializationStatus.place(effectivelyRoot));
          break;
        }
      case PROXY_NODES:
        {
          // Let's find all the IDs of nodes present here. The nodes with parents not present here
          // are effectively treated as roots and their parent will be set to an instance of a
          // ProxyNode, as we cannot retrieve them or set them (until we decide to provide some
          // sort of NodeResolver)
          Set<String> knownIDs =
              originalList.stream().map(ci -> ci.getID()).collect(Collectors.toSet());
          Set<String> parentIDs =
              originalList.stream()
                  .map(n -> n.getParentNodeID())
                  .filter(id -> id != null)
                  .collect(Collectors.toSet());
          Set<String> unknownParentIDs = Sets.difference(parentIDs, knownIDs);
          originalList.stream()
              .filter(ci -> unknownParentIDs.contains(ci.getParentNodeID()))
              .forEach(effectivelyRoot -> deserializationStatus.place(effectivelyRoot));

          unknownParentIDs.forEach(id -> deserializationStatus.createProxy(id));
          break;
        }
    }

    // We can start by putting at the start all the elements which either have no parent,
    // or had a parent already added to the list
    while (deserializationStatus.howManySorted() < originalList.size()) {
      int initialLength = deserializationStatus.howManySorted();
      for (int i = 0; i < deserializationStatus.howManyToSort(); i++) {
        SerializedClassifierInstance node = deserializationStatus.getNodeToSort(i);
        if (node.getParentNodeID() == null
            || deserializationStatus
                .streamSorted()
                .anyMatch(sn -> Objects.equals(sn.getID(), node.getParentNodeID()))) {
          deserializationStatus.place(node);
          i--;
        }
      }
      if (initialLength == deserializationStatus.howManySorted()) {
        if (deserializationStatus.howManySorted() == 0) {
          throw new DeserializationException(
              "No root found, we cannot deserialize this tree. Original list: " + originalList);
        } else {
          throw new DeserializationException(
              "Something is not right: we are unable to complete sorting the list "
                  + originalList
                  + ". Probably there is a containment loop");
        }
      }
    }

    deserializationStatus.reverse();
    return deserializationStatus;
  }

  public List<ClassifierInstance<?>> deserializeSerializationBlock(
      SerializedChunk serializationBlock) {
    return deserializeClassifierInstances(serializationBlock.getClassifierInstances());
  }

  private List<ClassifierInstance<?>> deserializeClassifierInstances(
      List<SerializedClassifierInstance> serializedClassifierInstances) {
    // We want to deserialize the nodes starting from the leaves. This is useful because in certain
    // cases we may want to use the children as constructor parameters of the parent
    DeserializationStatus deserializationStatus = sortLeavesFirst(serializedClassifierInstances);
    List<SerializedClassifierInstance> sortedSerializedClassifierInstances =
        deserializationStatus.sortedList;
    if (sortedSerializedClassifierInstances.size() != serializedClassifierInstances.size()) {
      throw new IllegalStateException();
    }
    Map<String, ClassifierInstance<?>> deserializedByID = new HashMap<>();
    IdentityHashMap<SerializedClassifierInstance, ClassifierInstance<?>> serializedToInstanceMap =
        new IdentityHashMap<>();
    sortedSerializedClassifierInstances.stream()
        .forEach(
            n -> {
              ClassifierInstance<?> instantiated = instantiateFromSerialized(n, deserializedByID);
              if (n.getID() != null && deserializedByID.containsKey(n.getID())) {
                throw new IllegalStateException("Duplicate ID found: " + n.getID());
              }
              deserializedByID.put(n.getID(), instantiated);
              serializedToInstanceMap.put(n, instantiated);
            });
    if (sortedSerializedClassifierInstances.size() != serializedToInstanceMap.size()) {
      throw new IllegalStateException(
          "We got "
              + sortedSerializedClassifierInstances.size()
              + " nodes to deserialize, but we deserialized "
              + serializedToInstanceMap.size());
    }
    ClassifierInstanceResolver classifierInstanceResolver =
        new CompositeClassifierInstanceResolver(
            new MapBasedResolver(deserializedByID),
            deserializationStatus.getProxiesInstanceResolver(),
            this.instanceResolver);
    NodePopulator nodePopulator =
        new NodePopulator(this, classifierInstanceResolver, deserializationStatus);
    serializedClassifierInstances.stream()
        .forEach(
            node -> {
              nodePopulator.populateClassifierInstance(serializedToInstanceMap.get(node), node);
              ClassifierInstance<?> classifierInstance = serializedToInstanceMap.get(node);
              ClassifierInstance<?> parent =
                  classifierInstanceResolver.resolve(node.getParentNodeID());
              if (parent instanceof ProxyNode
                  && unavailableParentPolicy == UnavailableNodePolicy.PROXY_NODES) {
                // For real parents, the parent is not set directly, but it is set indirectly
                // when adding the child to the parent. For proxy nodes instead we need to set
                // the parent explicitly
                ProxyNode proxyParent = (ProxyNode) parent;
                if (proxyParent != null) {
                  if (classifierInstance instanceof HasSettableParent) {
                    ((HasSettableParent) classifierInstance).setParent(proxyParent);
                  } else {
                    throw new UnsupportedOperationException(
                        "We do not know how to set explicitly the parent of " + classifierInstance);
                  }
                }
              }
              if (classifierInstance instanceof AnnotationInstance) {
                if (node == null) {
                  throw new IllegalStateException(
                      "Dangling annotation instance found (annotated node is null). ");
                }
                AbstractClassifierInstance abstractClassifierInstance =
                    (AbstractClassifierInstance) deserializedByID.get(node.getParentNodeID());
                AnnotationInstance annotationInstance = (AnnotationInstance) classifierInstance;
                if (abstractClassifierInstance != null) {
                  abstractClassifierInstance.addAnnotation(annotationInstance);
                } else {
                  throw new IllegalStateException(
                      "Cannot resolved annotated node " + annotationInstance.getParent());
                }
              }
            });

    // We want the nodes returned to be sorted as the original serializedNodes
    List<ClassifierInstance<?>> nodesWithOriginalSorting =
        serializedClassifierInstances.stream()
            .map(sn -> serializedToInstanceMap.get(sn))
            .collect(Collectors.toList());
    nodesWithOriginalSorting.addAll(deserializationStatus.proxies);
    return nodesWithOriginalSorting;
  }

  private ClassifierInstance<?> instantiateFromSerialized(
      SerializedClassifierInstance serializedClassifierInstance,
      Map<String, ClassifierInstance<?>> deserializedByID) {
    MetaPointer serializedClassifier = serializedClassifierInstance.getClassifier();
    if (serializedClassifier == null) {
      throw new RuntimeException("No metaPointer available for " + serializedClassifierInstance);
    }
    Classifier<?> classifier = getClassifierResolver().resolveClassifier(serializedClassifier);

    // We prepare all the properties values and pass them to instantiator, as it could use them to
    // build the node
    Map<Property, Object> propertiesValues = new HashMap<>();
    serializedClassifierInstance
        .getProperties()
        .forEach(
            serializedPropertyValue -> {
              Property property =
                  classifier.getPropertyByMetaPointer(serializedPropertyValue.getMetaPointer());
              Objects.requireNonNull(
                  property,
                  "Property with metaPointer "
                      + serializedPropertyValue.getMetaPointer()
                      + " not found in classifier "
                      + classifier
                      + ". Properties: "
                      + classifier.allProperties().stream()
                          .map(p -> MetaPointer.from(p))
                          .collect(Collectors.toList()));
              Object deserializedValue =
                  primitiveValuesSerialization.deserialize(
                      property.getType(),
                      serializedPropertyValue.getValue(),
                      property.isRequired());
              propertiesValues.put(property, deserializedValue);
            });
    ClassifierInstance<?> classifierInstance =
        getInstantiator()
            .instantiate(
                classifier, serializedClassifierInstance, deserializedByID, propertiesValues);

    // We ensure that the properties values are set correctly. They could already have been set
    // while instantiating the node. If that is the case, we have nothing to do, otherwise we set
    // the values
    propertiesValues
        .entrySet()
        .forEach(
            pv -> {
              Object deserializedValue = pv.getValue();
              Property property = pv.getKey();
              // Avoiding calling setters, in case the value has been already set at construction
              // time

              if (!Objects.equals(
                  deserializedValue, classifierInstance.getPropertyValue(property))) {
                classifierInstance.setPropertyValue(property, deserializedValue);
              }
            });

    return classifierInstance;
  }
}
