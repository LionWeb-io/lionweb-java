package io.lionweb.lioncore.java.serialization;

import io.lionweb.lioncore.java.language.*;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class SerializationStatus {
  private final Set<String> classifiersConsidered = new HashSet<>();
  private final IdentityHashMap<String, List<Property>> properties = new IdentityHashMap<>();
  private final IdentityHashMap<String, List<Containment>> containments = new IdentityHashMap<>();
  private final IdentityHashMap<String, List<Reference>> references = new IdentityHashMap<>();
  private final Set<String> consideredLanguageIDs = new HashSet<>();

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
}
