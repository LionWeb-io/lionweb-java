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
    String id = classifier.getID();
    List<Property> cached = properties.get(id);
    if (cached != null) return cached;
    List<Property> computed = classifier.allProperties();
    properties.put(id, computed);
    return computed;
  }

  public Iterable<Containment> allContainments(Classifier<?> classifier) {
    String id = classifier.getID();
    List<Containment> cached = containments.get(id);
    if (cached != null) return cached;
    List<Containment> computed = classifier.allContainments();
    containments.put(id, computed);
    return computed;
  }

  public Iterable<Reference> allReferences(Classifier<?> classifier) {
    String id = classifier.getID();
    List<Reference> cached = references.get(id);
    if (cached != null) return cached;
    List<Reference> computed = classifier.allReferences();
    references.put(id, computed);
    return computed;
  }

  public void considerLanguageDuringSerialization(Consumer<Language> consumer, Language language) {
    if (consideredLanguageIDs.contains(language.getID())) {
      return;
    }
    consumer.accept(language);
    consideredLanguageIDs.add(language.getID());
  }
}
