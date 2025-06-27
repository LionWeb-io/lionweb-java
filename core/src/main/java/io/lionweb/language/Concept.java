package io.lionweb.language;

import io.lionweb.LionWebVersion;
import io.lionweb.lioncore.LionCore;
import io.lionweb.model.ClassifierInstanceUtils;
import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A Concept represents a category of entities sharing the same structure.
 *
 * <p>For example, Invoice would be a Concept. Single entities could be Concept instances, such as
 * Invoice #1/2022.
 *
 * @see org.eclipse.emf.ecore.EClass Ecore equivalent <i>EClass</i> (with the <code>isInterface
 *     </code> flag set to <code>false</code>)
 * @see <a href="https://www.jetbrains.com/help/mps/structure.html#conceptsandconceptinterfaces">MPS
 *     equivalent <i>Concept</i> in documentation</a>
 * @see <a
 *     href="http://127.0.0.1:63320/node?ref=r%3A00000000-0000-4000-0000-011c89590292%28jetbrains.mps.lang.structure.structure%29%2F1071489090640">MPS
 *     equivalent <i>ConceptDeclaration</i> in local MPS</a>
 * @see org.jetbrains.mps.openapi.language.SConcept MPS equivalent <i>SConcept</i> in SModel
 */
public class Concept extends Classifier<Concept> {

  public Concept() {
    super();
    setAbstract(false);
    setPartition(false);
  }

  public Concept(@Nonnull LionWebVersion lionWebVersion) {
    super(lionWebVersion);
    setAbstract(false);
    setPartition(false);
  }

  public Concept(
      @Nullable Language language,
      @Nullable String name,
      @Nonnull String id,
      @Nullable String key) {
    this(language, name, id);
    setKey(key);
  }

  public Concept(@Nullable Language language, @Nullable String name, @Nonnull String id) {
    super(language, name, id);
    setAbstract(false);
    setPartition(false);
  }

  public Concept(
      @Nonnull LionWebVersion lionWebVersion, @Nullable Language language, @Nullable String name) {
    super(lionWebVersion, language, name);
    setAbstract(false);
    setPartition(false);
  }

  public Concept(@Nullable Language language, @Nullable String name) {
    super(language, name);
    setAbstract(false);
    setPartition(false);
  }

  public Concept(@Nonnull LionWebVersion lionWebVersion, @Nullable String name) {
    super(lionWebVersion, null, name);
    setAbstract(false);
    setPartition(false);
  }

  public Concept(@Nullable String name) {
    super(null, name);
    setAbstract(false);
    setPartition(false);
  }

  @Nonnull
  @Override
  public List<Classifier<?>> directAncestors() {
    List<Classifier<?>> directAncestors = new ArrayList<>();
    // TODO add base ancestor common to all Concepts
    if (this.getExtendedConcept() != null) {
      directAncestors.add(this.getExtendedConcept());
    }
    directAncestors.addAll(this.getImplemented());
    return directAncestors;
  }

  public boolean isAbstract() {
    return this.getPropertyValue("abstract", Boolean.class, false);
  }

  public void setAbstract(boolean value) {
    this.setPropertyValue("abstract", value);
  }

  public boolean isPartition() {
    return this.getPropertyValue("partition", Boolean.class, false);
  }

  public void setPartition(boolean value) {
    this.setPropertyValue("partition", value);
  }

  // TODO should this return BaseConcept when extended is equal null?
  public @Nullable Concept getExtendedConcept() {
    return this.getReferenceSingleValue("extends");
  }

  public @Nonnull List<Interface> getImplemented() {
    return this.getReferenceMultipleValue("implements");
  }

  public void addImplementedInterface(@Nonnull Interface iface) {
    Objects.requireNonNull(iface, "Interface should not be null");
    this.addReferenceMultipleValue("implements", ClassifierInstanceUtils.referenceTo(iface));
  }

  // TODO should we verify the Concept does not extend itself, even indirectly?
  public void setExtendedConcept(@Nullable Concept extended) {
    if (extended == null) {
      this.setReferenceSingleValue("extends", null);
    } else {
      this.setReferenceSingleValue("extends", ClassifierInstanceUtils.referenceTo(extended));
    }
  }

  @Nonnull
  @Override
  public List<Feature<?>> inheritedFeatures() {
    List<Feature<?>> result = new LinkedList<>();
    for (Classifier<?> ancestor : this.allAncestors()) {
      combineFeatures(result, ancestor.getFeatures());
    }
    return result;
  }

  @Override
  public Concept getClassifier() {
    return LionCore.getConcept(getLionWebVersion());
  }
}
