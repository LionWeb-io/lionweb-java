package lioncore.java;

import java.util.List;

public class Concept extends AbstractConcept {
    private boolean isAbstract;

    public Concept(Metamodel metamodel, String simpleName) {
        super(metamodel, simpleName);
    }

    public boolean isAbstract() {
        return this.isAbstract;
    }

    public Concept getExtendedConcept() {
        throw new UnsupportedOperationException();
    }

    public List<ConceptInterface> getImplemented() {
        throw new UnsupportedOperationException();
    }
}
