package org.lionweb.lioncore.java.metamodel;

import org.lionweb.lioncore.java.Experimental;
import org.lionweb.lioncore.java.utils.Naming;
import org.lionweb.lioncore.java.utils.Validatable;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

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
public class Annotation extends FeaturesContainer {
    private @Nullable String platformSpecific;
    private FeaturesContainer target;

    public Annotation() {
        super();
    }

    public Annotation(Metamodel metamodel, String simpleName) {
        super(metamodel, simpleName);
    }

    @Override
    public List<Feature> allFeatures() {
        throw new UnsupportedOperationException();
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

    public FeaturesContainer getTarget() {
        return target;
    }

    public void setTarget(FeaturesContainer target) {
        // TODO prevent annotations to be used as target
        this.target = target;
    }

}
