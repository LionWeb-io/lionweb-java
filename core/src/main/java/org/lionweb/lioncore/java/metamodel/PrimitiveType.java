package org.lionweb.lioncore.java.metamodel;

import javax.annotation.Nullable;

/**
 * This represents an arbitrary primitive value, which is not an enumeration.
 *
 * BooleanType, NumberType, and StringType are common PrimitiveTypes.
 *
 * @see org.eclipse.emf.ecore.EDataType Ecore equivalent <i>EDataType</i>
 * @see <a href="http://127.0.0.1:63320/node?ref=r%3A00000000-0000-4000-0000-011c89590292%28jetbrains.mps.lang.structure.structure%29%2F1083243159079">MPS equivalent <i>PrimitiveDataTypeDeclaration</i> in local MPS</a>
 * @see org.jetbrains.mps.openapi.language.SPrimitiveDataType MPS equivalent <i>SPrimitiveDataType</i> in SModel
 *
 * All PrimitiveTypes in LionCore are builtin.
 */
public class PrimitiveType extends DataType {
    public PrimitiveType() {
        super();
    }

    public PrimitiveType(@Nullable Metamodel metamodel, String simpleName) {
        super(metamodel, simpleName);
    }

    @Override
    public String toString() {
        return "PrimitiveType(" + getSimpleName() + ")";
    }
}
