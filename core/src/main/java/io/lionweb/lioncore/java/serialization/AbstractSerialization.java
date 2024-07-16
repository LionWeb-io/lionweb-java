package io.lionweb.lioncore.java.serialization;

import io.lionweb.lioncore.java.api.LocalClassifierInstanceResolver;
import io.lionweb.lioncore.java.language.DataType;
import io.lionweb.lioncore.java.language.Language;
import io.lionweb.lioncore.java.language.LanguageEntity;
import io.lionweb.lioncore.java.model.AnnotationInstance;
import io.lionweb.lioncore.java.model.ClassifierInstance;
import io.lionweb.lioncore.java.serialization.data.*;
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class AbstractSerialization {
  public static final String DEFAULT_SERIALIZATION_FORMAT = "2023.1";

  protected final ClassifierResolver classifierResolver;
  protected final Instantiator instantiator;
  protected final PrimitiveValuesSerialization primitiveValuesSerialization;

  protected final LocalClassifierInstanceResolver instanceResolver;

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
}
