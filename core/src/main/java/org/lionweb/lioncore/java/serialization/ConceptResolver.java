package org.lionweb.lioncore.java.serialization;

import org.lionweb.lioncore.java.metamodel.Concept;
import org.lionweb.lioncore.java.metamodel.Metamodel;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is responsible to resolve the Concept associate with a given Concept ID.
 * <p>
 * While initially just know concepts which have been explicitly registered, in the future it could
 * adopt more advanced resolution strategies.
 */
public class ConceptResolver {
    private Map<String, Concept> registeredConcepts = new HashMap<>();

    public Concept resolveConcept(String conceptID) {
        if (registeredConcepts.containsKey(conceptID)) {
            return registeredConcepts.get(conceptID);
        } else {
            throw new RuntimeException("Unable to resolve concept with id " + conceptID);
        }
    }

    public void registerMetamodel(Metamodel metamodel) {
        metamodel.getElements().forEach(e -> {
            if (e instanceof Concept) {
                registerConcept((Concept) e);
            }
        });
    }

    private void registerConcept(Concept concept) {
        registeredConcepts.put(concept.getID(), concept);
    }
}
