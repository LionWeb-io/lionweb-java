package org.lionweb.lioncore.java;

/**
 * This indicates a simple value associated to an entity.
 *
 * For example, an Invoice could have a date or an amount.
 *
 * A Property is similar to Ecore’s {@link org.eclipse.emf.ecore.EAttribute EAttribute} or MPS'
 * <a href="https://www.jetbrains.com/help/mps/structure.html#properties">Properties</a> /
 * <a href="http://127.0.0.1:63320/node?ref=r%3A00000000-0000-4000-0000-011c89590292%28jetbrains.mps.lang.structure.structure%29%2F1071489288299">PropertyDeclaration</a> /
 * <a href="http://127.0.0.1:63320/node?ref=8865b7a8-5271-43d3-884c-6fd1d9cfdd34%2Fjava%3Aorg.jetbrains.mps.openapi.language%28MPS.OpenAPI%2F%29%2F%7ESProperty">SProperty</a>.
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
