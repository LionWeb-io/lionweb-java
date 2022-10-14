package org.lionweb.lioncore.java;

import java.util.LinkedList;
import java.util.List;

public class Concept extends AbstractConcept {
    private boolean isAbstract;
    private Concept extended;
    private List<ConceptInterface> implemented = new LinkedList<>();

    public Concept(Metamodel metamodel, String simpleName) {
        super(metamodel, simpleName);
    }

    public boolean isAbstract() {
        return this.isAbstract;
    }

    public void setAbstract(boolean anAbstract) {
        isAbstract = anAbstract;
    }

    public Concept getExtendedConcept() {
        return this.extended;
    }

    public List<ConceptInterface> getImplemented() {
        return this.implemented;
    }

    public void setExtendedConcept(Concept extended) {
        this.extended = extended;
    }


}
