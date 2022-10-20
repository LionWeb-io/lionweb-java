package org.lionweb.lioncore.java;

/**
 * This represents an arbitrary primitive value, which is not an enumeration.
 *
 * BooleanType, NumberType, and StringType are common PrimitiveTypes.
 *
 * A PrimitiveType is similar to Ecore’s {@link org.eclipse.emf.ecore.EDataType EDataType} and to MPS’
 * <a href="http://127.0.0.1:63320/node?ref=r%3A00000000-0000-4000-0000-011c89590292%28jetbrains.mps.lang.structure.structure%29%2F1083243159079">PrimitiveDataTypeDeclaration</a> /
 * <a href="http://127.0.0.1:63320/node?ref=8865b7a8-5271-43d3-884c-6fd1d9cfdd34%2Fjava%3Aorg.jetbrains.mps.openapi.language%28MPS.OpenAPI%2F%29%2F%7ESPrimitiveDataType">SPrimitiveDataType</a>.
 *
 * All PrimitiveTypes in LionCore are builtin.
 */
public class PrimitiveType extends DataType {
    public PrimitiveType(Metamodel metamodel, String simpleName) {
        super(metamodel, simpleName);
    }
}
