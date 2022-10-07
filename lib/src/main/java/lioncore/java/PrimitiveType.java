package lioncore.java;

public class PrimitiveType extends Datatype {
    public PrimitiveType(Metamodel metamodel, String simpleName) {
        super(metamodel, simpleName);
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
