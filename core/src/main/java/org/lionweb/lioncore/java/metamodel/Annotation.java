package org.lionweb.lioncore.java.metamodel;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.lionweb.lioncore.java.Experimental;
import org.lionweb.lioncore.java.utils.Naming;

/**
 * This represents additional metadata relative to some orthogonal concern.
 *
 * <p>A DocumentationComment could be specified as an annotation.
 *
 * @see org.eclipse.emf.ecore.EAnnotation Ecore equivalent <i>EAnnotation</i>
 * @see <a href="https://www.jetbrains.com/help/mps/structure.html#attributes">MPS equivalent
 *     <i>Attribute</i> in documentation</a>
 * @see <a
 *     href="http://127.0.0.1:63320/node?ref=r%3A00000000-0000-4000-0000-011c89590288%28jetbrains.mps.lang.core.structure%29%2F3364660638048049748">MPS
 *     equivalent <i>NodeAttribute</i> in local MPS</a>
 */
@Experimental
public class Annotation extends FeaturesContainer<Annotation> {
  private @Nullable String platformSpecific;
  private FeaturesContainer target;

  public Annotation() {
    super();
  }

  public Annotation(Metamodel metamodel, String simpleName) {
    super(metamodel, simpleName);
  }

  @Nonnull
  @Override
  public List<FeaturesContainer<?>> directAncestors() {
    throw new UnsupportedOperationException();
  }

  @Override
  public @Nonnull List<Feature> allFeatures() {
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

  public @Nullable FeaturesContainer getTarget() {
    return target;
  }

  public void setTarget(@Nullable FeaturesContainer target) {
    // TODO prevent annotations to be used as target
    this.target = target;
  }

  @Override
  public Concept getConcept() {
    throw new UnsupportedOperationException(
        "Annotation is currently not yet approved, so there is no concept defined for it");
  }
}
