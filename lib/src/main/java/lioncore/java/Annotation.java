package lioncore.java;

public class Annotation extends MetamodelElement {
    private String platformSpecific;

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
