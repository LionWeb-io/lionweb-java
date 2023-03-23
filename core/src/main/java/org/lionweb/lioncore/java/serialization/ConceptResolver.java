package org.lionweb.lioncore.java.serialization;

import org.lionweb.lioncore.java.metamodel.Concept;
import org.lionweb.lioncore.java.metamodel.Metamodel;
import org.lionweb.lioncore.java.serialization.data.MetaPointer;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is responsible to resolve the Concept associate with a given Concept ID.
 * <p>
 * While initially just know concepts which have been explicitly registered, in the future it could
 * adopt more advanced resolution strategies.
 */
public class ConceptResolver {
    private Map<MetaPointer, Concept> registeredConcepts = new HashMap<>();

    public Concept resolveConcept(MetaPointer conceptMetaPointer) {
        if (registeredConcepts.containsKey(conceptMetaPointer)) {
            return registeredConcepts.get(conceptMetaPointer);
        } else {
            throw new RuntimeException("Unable to resolve concept with metaPointer " + conceptMetaPointer);
        }
    }

    public ConceptResolver registerMetamodel(Metamodel metamodel) {
        metamodel.getElements().forEach(e -> {
            if (e instanceof Concept) {
                registerConcept((Concept) e);
            }
        });
        return this;
    }

    private void registerConcept(Concept concept) {
        registeredConcepts.put(MetaPointer.from(concept), concept);
    }
}
