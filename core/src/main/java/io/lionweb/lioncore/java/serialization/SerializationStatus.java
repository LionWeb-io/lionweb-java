package io.lionweb.lioncore.java.serialization;

import io.lionweb.lioncore.java.language.*;
import io.lionweb.lioncore.java.serialization.data.MetaPointer;
import io.lionweb.lioncore.java.serialization.data.SerializedChunk;
import io.lionweb.lioncore.java.serialization.data.SerializedClassifierInstance;
import io.lionweb.lioncore.java.serialization.data.UsedLanguage;

import java.util.*;
import java.util.function.Consumer;

public class SerializationStatus {
  private final Set<String> classifiersConsidered = new HashSet<>();
  private final IdentityHashMap<String, List<Property>> properties = new IdentityHashMap<>();
  private final IdentityHashMap<String, List<Containment>> containments = new IdentityHashMap<>();
  private final IdentityHashMap<String, List<Reference>> references = new IdentityHashMap<>();
  private final Set<String> consideredLanguageIDs = new HashSet<>();
  private final SerializedChunk serializedChunk;
  private final Set<UsedLanguage> usedLanguages = new HashSet<>();

  public SerializationStatus(SerializedChunk serializedChunk) {
    this.serializedChunk = serializedChunk;
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

  public void consider(Classifier<?> classifier, Consumer<Language> languageConsumer) {
    if (!hasConsideredClassifier(classifier.getID())) {
      Objects.requireNonNull(classifier, "A node should have a concept in order to be serialized");
      Language language = classifier.getLanguage();
      if (language == null) {
        throw new NullPointerException(
            "A Concept should be part of a Language in order to be serialized. Concept "
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
              p ->
                  considerLanguageDuringSerialization(languageConsumer, p.getType().getLanguage()));
      features.stream()
          .filter(f -> f instanceof Link)
          .map(f -> (Link<?>) f)
          .forEach(
              l ->
                  considerLanguageDuringSerialization(languageConsumer, l.getType().getLanguage()));
      markClassifierAsConsidered(classifier.getID());
    }
  }
}
