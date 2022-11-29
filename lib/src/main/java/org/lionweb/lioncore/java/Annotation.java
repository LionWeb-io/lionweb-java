package org.lionweb.lioncore.java;

import org.lionweb.lioncore.java.utils.Naming;

import javax.annotation.Nullable;

/**
 * This represents additional metadata relative to some orthogonal concern.
 *
 * A DocumentationComment could be specified as an annotation.
 *
 * @see org.eclipse.emf.ecore.EAnnotation Ecore equivalent <i>EAnnotation</i>
 * @see <a href="https://www.jetbrains.com/help/mps/structure.html#attributes">MPS equivalent <i>Attribute</i> in documentation</a>
 * @see <a href="http://127.0.0.1:63320/node?ref=r%3A00000000-0000-4000-0000-011c89590288%28jetbrains.mps.lang.core.structure%29%2F3364660638048049748">MPS equivalent <i>NodeAttribute</i> in local MPS</a>
 */
@Experimental
public class Annotation extends AbstractConcept {
    private @Nullable String platformSpecific;
    private AbstractConcept target;

    public Annotation(Metamodel metamodel, String simpleName) {
        super(metamodel, simpleName);
    }

    public @Nullable String getPlatformSpecific() {
        return platformSpecific;
    }

    public void setPlatformSpecific(@Nullable String platformSpecific) {
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
