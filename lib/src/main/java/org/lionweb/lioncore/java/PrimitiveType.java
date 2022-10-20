package org.lionweb.lioncore.java;

/**
 * This represents an arbitrary primitive value, which is not an enumeration.
 *
 * BooleanType, NumberType, and StringType are common PrimitiveTypes.
 *
 * A PrimitiveType is similar to Ecore’s EDataType and to MPS’ PrimitiveDataTypeDeclaration.
 *
 * All PrimitiveTypes in LionCore are builtin.
 */
public class PrimitiveType extends DataType {
    public PrimitiveType(Metamodel metamodel, String simpleName) {
        super(metamodel, simpleName);
    }
}
