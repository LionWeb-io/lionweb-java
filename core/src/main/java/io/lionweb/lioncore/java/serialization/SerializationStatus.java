package io.lionweb.lioncore.java.serialization;

import io.lionweb.lioncore.java.language.Classifier;
import io.lionweb.lioncore.java.language.Containment;
import io.lionweb.lioncore.java.language.Property;
import io.lionweb.lioncore.java.language.Reference;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

public class SerializationStatus {
  private final Set<String> classifiersConsidered = new HashSet<>();
  private final IdentityHashMap<String, List<Property>> properties = new IdentityHashMap<>();
  private final IdentityHashMap<String, List<Containment>> containments = new IdentityHashMap<>();
  private final IdentityHashMap<String, List<Reference>> references = new IdentityHashMap<>();

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
    if (!containments.containsKey(classifier.getID())) {
      containments.put(classifier.getID(), classifier.allContainments());
    }
    return containments.get(classifier.getID());
  }

  public Iterable<Reference> allReferences(Classifier<?> classifier) {
    if (!references.containsKey(classifier.getID())) {
      references.put(classifier.getID(), classifier.allReferences());
    }
    return references.get(classifier.getID());
  }
}
