package org.lionweb.lioncore.java;

/**
 * A type of value which has not a relevant identity in the context of a model.
 *
 * A Currency or a Date type are possible DataTypes.
 *
 * It is similar to Ecore's EDataType. It is also similar to MPS' DataTypeDeclaration.
 */
public abstract class DataType extends MetamodelElement {
    public DataType(Metamodel metamodel, String simpleName) {
        super(metamodel, simpleName);
    }
}
