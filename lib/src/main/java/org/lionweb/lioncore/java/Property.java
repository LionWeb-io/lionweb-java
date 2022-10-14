package org.lionweb.lioncore.java;

public class Property extends Feature {
    private Datatype type;
    private String simpleName;

    public Property(String simpleName, FeaturesContainer container) {
        super(simpleName, container);
    }

    @Override
    public String getSimpleName() {
        return simpleName;
    }

    @Override
    public String qualifiedName() {
        return getContainer().namespaceQualifier() + "." + getSimpleName();
    }

    @Override
    public NamespaceProvider getContainer() {
        throw new UnsupportedOperationException();
    }
}
