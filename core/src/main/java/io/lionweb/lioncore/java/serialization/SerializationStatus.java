package io.lionweb.lioncore.java.serialization;

import io.lionweb.lioncore.java.language.Classifier;
import io.lionweb.lioncore.java.language.Containment;
import io.lionweb.lioncore.java.language.Property;
import io.lionweb.lioncore.java.language.Reference;

import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.List;

public class SerializationStatus {
    private Set<String> classifiersConsidered = new HashSet<>();
    private IdentityHashMap<String, List<Property>> properties = new IdentityHashMap<>();
    private IdentityHashMap<String, List<Containment>> containments = new IdentityHashMap<>();
    private IdentityHashMap<String, List<Reference>> references = new IdentityHashMap<>();

    public boolean hasConsideredClassifier(String classifierId) {
        return classifiersConsidered.contains(classifierId);
    }

    public void markClassifierAsConsidered(String classifierId) {
        classifiersConsidered.add(classifierId);
    }

    public Iterable<Property> allProperties(Classifier<?> classifier) {
        if (!properties.containsKey(classifier.getID())) {
            properties.put(classifier.getID(), classifier.allProperties());
        }
        return properties.get(classifier.getID());
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
