package org.lionweb.lioncore.java;

import org.lionweb.lioncore.java.utils.Naming;

/**
 * This represents additional metadata relative to some orthogonal concern.
 *
 * A DocumentationComment could be specified as an annotation.
 *
 * This is similar to Ecore’s EAnnotation. And to MPS’s NodeAttribute.
 */
public class Annotation extends AbstractConcept {
    private String platformSpecific;
    private AbstractConcept target;

    public Annotation(Metamodel metamodel, String simpleName) {
        super(metamodel, simpleName);
    }

    public String getPlatformSpecific() {
        return platformSpecific;
    }

    public void setPlatformSpecific(String platformSpecific) {
        if (platformSpecific != null) {
            Naming.validateSimpleName(platformSpecific);
        }
        this.platformSpecific = platformSpecific;
    }

    public AbstractConcept getTarget() {
        return target;
    }

    public void setTarget(AbstractConcept target) {
        // TODO prevent annotations to be used as target
        this.target = target;
    }
}
