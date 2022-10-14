package org.lionweb.lioncore.java;

public class Containment extends Link {
    private Containment specialized;

    public Containment(String simpleName, FeaturesContainer container) {
        super(simpleName, container);
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

    public Containment getSpecialized() {
        return specialized;
    }

    public void setSpecialized(Containment specialized) {
        this.specialized = specialized;
    }
}
