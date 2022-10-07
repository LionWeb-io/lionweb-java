package lioncore.java;

import java.util.List;

public class Concept extends AbstractConcept {
    private boolean isAbstract;
    public boolean isAbstract() {
        return this.isAbstract;
    }

    @Override
    public List<Feature> getFeatures() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String namespaceQualifier() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getSimpleName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String qualifiedName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public NamespaceProvider getContainer() {
        throw new UnsupportedOperationException();
    }

    public Concept getExtendedConcept() {
        throw new UnsupportedOperationException();
    }

    public List<ConceptInterface> getImplemented() {
        throw new UnsupportedOperationException();
    }
}
