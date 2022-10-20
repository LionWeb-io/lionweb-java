package org.lionweb.lioncore.java;

/**
 * This indicates a simple value associated to an entity.
 *
 * For example, an Invoice could have a date or an amount.
 *
 * A Property is similar to Ecore’s EAttribute.
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
