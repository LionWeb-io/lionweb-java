package org.lionweb.lioncore.java;

/**
 * Represents a relation between a containing FeaturesContainer and a contained AbstractConcept.
 *
 * Between an IfStatement and its condition there is a Containment relation.
 *
 * A Containment is similar to an ECore’s EReference with the containment flag set to true.
 * Differently from an EReference there is no container flag and resolveProxies flag.
 * A Containment is similar to an MPS’s LinkDeclaration with metaClass having value aggregation.
 */
public class Containment extends Link {
    private Containment specialized;

    public Containment(String simpleName, FeaturesContainer container) {
        // TODO verify that the container is also a NamespaceProvider
        super(simpleName, container);
    }

    public Containment getSpecialized() {
        return specialized;
    }

    public void setSpecialized(Containment specialized) {
        // TODO check which limitations there are: should have the same name? Should it belong
        //      to an ancestor of the FeaturesContainer holding this Containment?
        this.specialized = specialized;
    }

}
