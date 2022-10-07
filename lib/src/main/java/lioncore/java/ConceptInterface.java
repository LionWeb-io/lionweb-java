package lioncore.java;

import java.util.List;

public class ConceptInterface extends AbstractConcept {
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

    public List<ConceptInterface> getExtended() {
        throw new UnsupportedOperationException();
    }
}
