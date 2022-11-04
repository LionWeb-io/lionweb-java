package org.lionweb.lioncore.java.metamodel;

/**
 * @see <a href="https://www.jetbrains.com/help/mps/structure.html#constraineddatatypes">MPS equivalent <i>Constrained Data Type</i> in documentation</a>
 * @see <a href="http://127.0.0.1:63320/node?ref=r%3A00000000-0000-4000-0000-011c89590292%28jetbrains.mps.lang.structure.structure%29%2F1082978499127">MPS equivalent <i>ConstrainedDataTypeDeclaration</i> in local MPS</a>
 */
public class Typedef extends DataType {
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