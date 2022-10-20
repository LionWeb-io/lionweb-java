package org.lionweb.lioncore.java;

/**
 * A type of value which has not a relevant identity in the context of a model.
 *
 * A Currency or a Date type are possible DataTypes.
 *
 * It is similar to Ecore's {@link org.eclipse.emf.ecore.EDataType EDataType}.
 * It is also similar to MPS'
 * <a href="http://127.0.0.1:63320/node?ref=r%3A00000000-0000-4000-0000-011c89590292%28jetbrains.mps.lang.structure.structure%29%2F1082978164218">DataTypeDeclaration</a> /
 * <a href="http://127.0.0.1:63320/node?ref=8865b7a8-5271-43d3-884c-6fd1d9cfdd34%2Fjava%3Aorg.jetbrains.mps.openapi.language%28MPS.OpenAPI%2F%29%2F%7ESDataType">SDataType</a>.
 */
public abstract class DataType extends MetamodelElement {
    public DataType(Metamodel metamodel, String simpleName) {
        super(metamodel, simpleName);
    }
}
