package lioncore.java;

public class Reference extends Link {
    private Containment specialized;

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
