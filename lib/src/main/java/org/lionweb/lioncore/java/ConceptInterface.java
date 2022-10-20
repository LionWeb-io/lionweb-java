package org.lionweb.lioncore.java;

import java.util.LinkedList;
import java.util.List;

/**
 * A ConceptInterface represents a category of entities sharing some similar characteristics.
 *
 * For example, Named would be a ConceptInterface.
 *
 * A ConceptInterface in LionWeb will be roughly equivalent to an EClass (with the isInterface flag set to true) or
 * an MPS’s ConceptInterface.
 */
public class ConceptInterface extends AbstractConcept {
    private List<ConceptInterface> extended = new LinkedList<>();

    public ConceptInterface(Metamodel metamodel, String simpleName) {
        super(metamodel, simpleName);
    }

    public List<ConceptInterface> getExtendedInterface() {
        return this.extended;
    }
}
