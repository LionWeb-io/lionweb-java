package org.lionweb.lioncore.java;

import org.lionweb.lioncore.java.utils.Naming;

/**
 * This represents additional metadata relative to some orthogonal concern.
 *
 * A DocumentationComment could be specified as an annotation.
 *
 * This is similar to Ecore’s {@link org.eclipse.emf.ecore.EAnnotation EAnnotation}.
 * And to MPS’s
 * <a href="https://www.jetbrains.com/help/mps/structure.html#attributes">Attributes</a> /
 * <a href="http://127.0.0.1:63320/node?ref=r%3A00000000-0000-4000-0000-011c89590288%28jetbrains.mps.lang.core.structure%29%2F3364660638048049748">NodeAttribute</a>.
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
