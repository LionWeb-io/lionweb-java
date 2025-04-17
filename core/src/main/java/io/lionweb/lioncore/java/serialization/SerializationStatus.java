package io.lionweb.lioncore.java.serialization;

import java.util.HashSet;
import java.util.Set;

public class SerializationStatus {
    Set<String> classifiersConsidered = new HashSet<>();

    public boolean hasConsideredClassifier(String classifierId) {
        return classifiersConsidered.contains(classifierId);
    }

    public void markClassifierAsConsidered(String classifierId) {
        classifiersConsidered.add(classifierId);
    }
}
