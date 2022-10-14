package org.lionweb.lioncore.java;

public class Reference extends Link {
    private Containment specialized;

    public Reference(String simpleName, FeaturesContainer container) {
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
}
