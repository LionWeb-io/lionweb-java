package lioncore.java;

public class Property extends Feature {
    private Datatype type;
    private String simpleName;

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
