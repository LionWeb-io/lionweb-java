package org.lionweb.lioncore.java;

public abstract class Feature implements NamespacedEntity {
    private Multiplicity multiplicity;
    private boolean derived;
    private String simpleName;
    private FeaturesContainer container;

    public Feature(String simpleName, FeaturesContainer container) {
        this.simpleName = simpleName;
        this.container = container;
    }

    public Multiplicity getMultiplicity() {
        return multiplicity;
    }

    public void setMultiplicity(Multiplicity multiplicity) {
        this.multiplicity = multiplicity;
    }

    public boolean isDerived() {
        return derived;
    }

    public void setDerived(boolean derived) {
        this.derived = derived;
    }

    @Override
    public String getSimpleName() {
        return simpleName;
    }

}
