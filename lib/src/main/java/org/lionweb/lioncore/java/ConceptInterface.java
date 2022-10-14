package org.lionweb.lioncore.java;

import java.util.LinkedList;
import java.util.List;

public class ConceptInterface extends AbstractConcept {
    private List<ConceptInterface> extended = new LinkedList<>();

    public ConceptInterface(Metamodel metamodel, String simpleName) {
        super(metamodel, simpleName);
    }

    public List<ConceptInterface> getExtendedInterface() {
        return this.extended;
    }
}
