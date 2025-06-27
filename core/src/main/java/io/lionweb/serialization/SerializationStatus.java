package io.lionweb.serialization;

import io.lionweb.language.*;
import io.lionweb.serialization.data.MetaPointer;
import io.lionweb.serialization.data.SerializedChunk;
import io.lionweb.serialization.data.SerializedClassifierInstance;
import io.lionweb.serialization.data.UsedLanguage;
import java.util.*;
import java.util.function.Consumer;

public class SerializationStatus {
  private final Set<String> classifiersConsidered = new HashSet<>();
  private final IdentityHashMap<String, List<Property>> properties = new IdentityHashMap<>();
  private final IdentityHashMap<String, List<Containment>> containments = new IdentityHashMap<>();
  private final IdentityHashMap<String, List<Reference>> references = new IdentityHashMap<>();
  private final Set<String> consideredLanguageIDs = new HashSet<>();
  private final SerializedChunk serializedChunk;
  // This is a cache, reflecting the list of languages in serializedChunk,
  // but as a set, for faster access
  private final Set<UsedLanguage> usedLanguages = new HashSet<>();

  public SerializationStatus(SerializedChunk serializedChunk) {
    this.serializedChunk = serializedChunk;
    this.usedLanguages.addAll(serializedChunk.getLanguages());
  }

  public boolean hasConsideredClassifier(String classifierId) {
    return classifiersConsidered.contains(classifierId);
  }

  public void markClassifierAsConsidered(String classifierId) {
    classifiersConsidered.add(classifierId);
  }

  public Iterable<Property> allProperties(Classifier<?> classifier) {
    return properties.computeIfAbsent(classifier.getID(), id -> classifier.allProperties());
  }

  public Iterable<Containment> allContainments(Classifier<?> classifier) {
    return containments.computeIfAbsent(classifier.getID(), id -> classifier.allContainments());
  }

  public Iterable<Reference> allReferences(Classifier<?> classifier) {
    return references.computeIfAbsent(classifier.getID(), id -> classifier.allReferences());
  }

  public void considerLanguageDuringSerialization(Consumer<Language> consumer, Language language) {
    if (consideredLanguageIDs.contains(language.getID())) {
      return;
    }
    consumer.accept(language);
    consideredLanguageIDs.add(language.getID());
  }

  public void consider(SerializedClassifierInstance serializedClassifierInstance) {
    consider(serializedClassifierInstance.getClassifier());
    serializedClassifierInstance.getProperties().forEach(f -> consider(f.getMetaPointer()));
    serializedClassifierInstance.getContainments().forEach(f -> consider(f.getMetaPointer()));
    serializedClassifierInstance.getReferences().forEach(f -> consider(f.getMetaPointer()));
  }

  public void consider(MetaPointer metaPointer) {
    UsedLanguage ul = new UsedLanguage(metaPointer.getLanguage(), metaPointer.getVersion());
    if (!usedLanguages.contains(ul)) {
      usedLanguages.add(ul);
      serializedChunk.addLanguage(ul);
    }
  }

  /** Consider the given classifier and all of its properties to track the used languages. */
  public void consider(Classifier<?> classifier, Consumer<Language> languageConsumer) {
    Objects.requireNonNull(classifier, "A node should have a concept in order to be serialized");
    if (hasConsideredClassifier(classifier.getID())) {
      return;
    }
    Language language = classifier.getLanguage();
    if (language == null) {
      throw new IllegalStateException(
          "A Classifier should be part of a Language in order to be serialized. Classifier "
              + classifier
              + " is not");
    }
    considerLanguageDuringSerialization(languageConsumer, language);
    List<Feature<?>> features = classifier.allFeatures();
    features.forEach(
        f -> considerLanguageDuringSerialization(languageConsumer, f.getDeclaringLanguage()));
    features.stream()
        .filter(f -> f instanceof Property)
        .map(f -> (Property) f)
        .forEach(
            p -> {
              DataType<?> dt = p.getType();
              considerLanguageDuringSerialization(languageConsumer, dt.getLanguage());
              if (dt instanceof StructuredDataType) {
                StructuredDataType sdt = (StructuredDataType) dt;
                sdt.getFields()
                    .forEach(
                        f ->
                            considerLanguageDuringSerialization(
                                languageConsumer, f.getType().getLanguage()));
              }
            });
    features.stream()
        .filter(f -> f instanceof Link)
        .map(f -> (Link<?>) f)
        .forEach(
            l -> considerLanguageDuringSerialization(languageConsumer, l.getType().getLanguage()));
    markClassifierAsConsidered(classifier.getID());
  }
}
