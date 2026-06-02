package io.lionweb.serialization;

import io.lionweb.language.*;
import java.util.*;
import java.util.function.Consumer;
import javax.annotation.Nonnull;

public class SerializationStatus {
  private final IdentityHashMap<String, List<Property>> properties = new IdentityHashMap<>();
  private final IdentityHashMap<String, List<Containment>> containments = new IdentityHashMap<>();
  private final IdentityHashMap<String, List<Reference>> references = new IdentityHashMap<>();

  // This is a cache, reflecting the list of languages in serializationChunk,
  // but as a set, for faster access
  private final Set<String> consideredLanguageIDs = new HashSet<>();

  public @Nonnull Iterable<Property> allProperties(@Nonnull Classifier<?> classifier) {
    Objects.requireNonNull(classifier, "classifier cannot be null");
    return properties.computeIfAbsent(classifier.getID(), id -> classifier.allProperties());
  }

  public @Nonnull Iterable<Containment> allContainments(@Nonnull Classifier<?> classifier) {
    Objects.requireNonNull(classifier, "classifier cannot be null");
    return containments.computeIfAbsent(classifier.getID(), id -> classifier.allContainments());
  }

  public @Nonnull Iterable<Reference> allReferences(@Nonnull Classifier<?> classifier) {
    Objects.requireNonNull(classifier, "classifier cannot be null");
    return references.computeIfAbsent(classifier.getID(), id -> classifier.allReferences());
  }

  public void considerLanguageDuringSerialization(
      @Nonnull Consumer<Language> consumer, @Nonnull Language language) {
    Objects.requireNonNull(consumer, "consumer cannot be null");
    Objects.requireNonNull(language, "language cannot be null");
    if (consideredLanguageIDs.contains(language.getID())) {
      return;
    }
    consumer.accept(language);
    consideredLanguageIDs.add(language.getID());
  }
}
