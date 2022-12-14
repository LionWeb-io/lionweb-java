package org.lionweb.lioncore.java.metamodel;

import javax.annotation.Nullable;

/**
 * A type of value which has not a relevant identity in the context of a model.
 *
 * A Currency or a Date type are possible DataTypes.
 *
 * @see org.eclipse.emf.ecore.EDataType Ecore equivalent <code>EDataType</code>
 * @see <a href="http://127.0.0.1:63320/node?ref=r%3A00000000-0000-4000-0000-011c89590292%28jetbrains.mps.lang.structure.structure%29%2F1082978164218">MPS equivalent <i>DataTypeDeclaration</i> in local MPS</a>
 * @see org.jetbrains.mps.openapi.language.SDataType MPS equivalent <i>SDataType</i> in SModel
 */
public abstract class DataType extends MetamodelElement {
    public DataType() {
        super();
    }

    public DataType(@Nullable Metamodel metamodel, @Nullable String simpleName) {
        super(metamodel, simpleName);
    }
}
