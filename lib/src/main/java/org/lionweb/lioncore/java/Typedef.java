package org.lionweb.lioncore.java;

public class Typedef extends Datatype {
    private PrimitiveType primitiveType;
    public Typedef(Metamodel metamodel, String simpleName) {
        super(metamodel, simpleName);
    }

    public PrimitiveType getPrimitiveType() {
        return primitiveType;
    }

    public void setPrimitiveType(PrimitiveType primitiveType) {
        this.primitiveType = primitiveType;
    }
}
