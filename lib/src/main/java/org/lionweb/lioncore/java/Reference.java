package org.lionweb.lioncore.java;

/**
 * This represents a relation between an FeaturesContainer and referred AbstractConcept.
 *
 * A VariableReference may have a Reference to a VariableDeclaration.
 *
 * A Containment is similar to an ECore’s EReference with the containment flag set to false.
 */
public class Reference extends Link {
    private Reference specialized;

    public Reference(String simpleName, FeaturesContainer container) {
        // TODO verify that the container is also a NamespaceProvider
        super(simpleName, container);
    }

    public Reference getSpecialized() {
        return specialized;
    }

    public void setSpecialized(Reference specialized) {
        // TODO check which limitations there are: should have the same name? Should it belong
        //      to an ancestor of the FeaturesContainer holding this Containment?
        this.specialized = specialized;
    }

    @Override
    public void setMultiplicity(Multiplicity multiplicity) {
        // TODO check constraint on multiplicity
        super.setMultiplicity(multiplicity);
    }
}
