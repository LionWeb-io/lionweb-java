package io.lionweb.serialization;

import io.lionweb.language.*;
import java.util.*;
import java.util.function.Consumer;

public class SerializationStatus {
  private final IdentityHashMap<String, List<Property>> properties = new IdentityHashMap<>();
  private final IdentityHashMap<String, List<Containment>> containments = new IdentityHashMap<>();
  private final IdentityHashMap<String, List<Reference>> references = new IdentityHashMap<>();

  // This is a cache, reflecting the list of languages in serializationChunk,
  // but as a set, for faster access
  private final Set<String> consideredLanguageIDs = new HashSet<>();

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
