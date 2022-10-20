package org.lionweb.lioncore.java;

/**
 * This indicates a simple value associated to an entity.
 *
 * For example, an Invoice could have a date or an amount.
 *
 * @see org.eclipse.emf.ecore.EAttribute Ecore equivalent <i>EAttribute</i>
 * @see <a href="https://www.jetbrains.com/help/mps/structure.html#properties">MPS equivalent <i>Property</i> in documentation</a>
 * @see <a href="http://127.0.0.1:63320/node?ref=r%3A00000000-0000-4000-0000-011c89590292%28jetbrains.mps.lang.structure.structure%29%2F1071489288299">MPS equivalent <i>PropertyDeclaration</i> in local MPS</a>
 * @see org.jetbrains.mps.openapi.language.SProperty MPS equivalent <i>SProperty</i> in SModel
 */
public class Property extends Feature {
    private DataType type;

    public Property(String simpleName, FeaturesContainer container) {
        // TODO verify that the container is also a NamespaceProvider
        super(simpleName, container);
    }

    @Override
    public void setMultiplicity(Multiplicity multiplicity) {
        // TODO check constraint on multiplicity
        super.setMultiplicity(multiplicity);
    }

    public DataType getType() {
        return type;
    }

    public void setType(DataType type) {
        this.type = type;
    }
}
