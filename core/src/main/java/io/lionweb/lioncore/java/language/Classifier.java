package io.lionweb.lioncore.java.language;

import io.lionweb.lioncore.java.versions.LionWebVersion;
import io.lionweb.lioncore.java.model.impl.M3Node;
import io.lionweb.lioncore.java.serialization.data.MetaPointer;
import io.lionweb.lioncore.java.versions.LionWebVersionToken;

import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This represents a group of elements that shares some characteristics.
 *
 * <p>For example, Dated and Invoice could be both AbstractConcepts, while having different levels
 * of tightness in the groups.
 *
 * @see org.eclipse.emf.ecore.EClass Ecore equivalent <i>EClass</i> (which is used both for classes
 *     and interfaces)
 * @see <a
 *     href="http://127.0.0.1:63320/node?ref=r%3A00000000-0000-4000-0000-011c89590292%28jetbrains.mps.lang.structure.structure%29%2F1169125787135">MPS
 *     equivalent <i>AbstractConceptDeclaration</i> in local MPS</a>
 * @see org.jetbrains.mps.openapi.language.SAbstractConcept MPS equivalent <i>SAbstractConcept</i>
 *     in SModel
 */
public abstract class Classifier<T extends M3Node, V extends LionWebVersionToken> extends LanguageEntity<T, V>
    implements NamespaceProvider {
  public Classifier() {
    super();
  }

  public Classifier(@Nonnull LionWebVersionToken lionWebVersionToken) {
    super(lionWebVersionToken);
  }

  public Classifier(@Nullable Language<V> language, @Nullable String name, @Nonnull String id) {
    super(language, name, id);
  }

  public Classifier(
      @Nonnull LionWebVersionToken lionWebVersionToken, @Nullable Language<V> language, @Nullable String name) {
    super(lionWebVersionToken, language, name);
  }

  public Classifier(@Nullable Language<V> language, @Nullable String name) {
    super(language, name);
  }

  public @Nullable Feature<?, V> getFeatureByName(@Nonnull String name) {
    Objects.requireNonNull(name, "name should not be null");
    return allFeatures().stream()
        .filter(feature -> feature.getName().equals(name))
        .findFirst()
        .orElse(null);
  }

  public abstract @Nonnull List<Classifier<?, V>> directAncestors();

  /**
   * Finds all ancestors. Works even for invalid inheritance hierarchies (i.e. containing loops).
   *
   * @return All direct or indirect/transitive ancestors.
   */
  public @Nonnull Set<Classifier<?, V>> allAncestors() {
    Set<Classifier<?, V>> result = new LinkedHashSet<>();
    Set<Classifier<?, V>> ancestors = new HashSet<>(directAncestors());

    while (!ancestors.isEmpty()) {
      for (Classifier<?, V> a : new HashSet<>(ancestors)) {
        ancestors.remove(a);
        if (result.add(a)) {
          ancestors.addAll(a.directAncestors());
        }
      }
    }

    return result;
  }

  public @Nonnull List<Feature<?, V>> allFeatures() {
    // TODO Should this return features which are overriden?
    // TODO Should features be returned in a particular order?
    List<Feature<?, V>> result = new LinkedList<>();
    result.addAll(this.getFeatures());
    combineFeatures(result, this.inheritedFeatures());

    return result;
  }

  public abstract @Nonnull List<Feature<?, V>> inheritedFeatures();

  public @Nonnull List<Property<V>> allProperties() {
    return allFeatures().stream()
        .filter(f -> f instanceof Property)
        .map(f -> (Property) f)
        .collect(Collectors.toList());
  }

  public @Nonnull List<Containment<V>> allContainments() {
    return allFeatures().stream()
        .filter(f -> f instanceof Containment)
        .map(f -> (Containment) f)
        .collect(Collectors.toList());
  }

  public @Nonnull List<Reference<V>> allReferences() {
    return allFeatures().stream()
        .filter(f -> f instanceof Reference)
        .map(f -> (Reference) f)
        .collect(Collectors.toList());
  }

  public @Nonnull List<Link<?, V>> allLinks() {
    return allFeatures().stream()
        .filter(f -> f instanceof Link)
        .map(f -> (Link<?, V>) f)
        .collect(Collectors.toList());
  }

  // TODO should this expose an immutable list to force users to use methods on this class
  //      to modify the collection?
  public @Nonnull List<Feature<?, V>> getFeatures() {
    return this.getContainmentMultipleValue("features");
  }

  public T addFeature(@Nonnull Feature<?, V> feature) {
    Objects.requireNonNull(feature, "feature should not be null");
    this.addContainmentMultipleValue("features", feature);
    feature.setParent(this);
    return (T) this;
  }

  @Override
  public String namespaceQualifier() {
    return this.qualifiedName();
  }

  public @Nullable Property<V> getPropertyByID(@Nonnull String propertyId) {
    Objects.requireNonNull(propertyId, "propertyId should not be null");
    return allFeatures().stream()
        .filter(f -> f instanceof Property)
        .map(f -> (Property<V>) f)
        .filter(p -> Objects.equals(p.getID(), propertyId))
        .findFirst()
        .orElse(null);
  }

  public @Nullable Property<V> getPropertyByName(@Nonnull String propertyName) {
    Objects.requireNonNull(propertyName, "propertyName should not be null");
    return allFeatures().stream()
        .filter(f -> f instanceof Property)
        .map(f -> (Property) f)
        .filter(p -> Objects.equals(p.getName(), propertyName))
        .findFirst()
        .orElse(null);
  }

  public @Nullable Containment<V> getContainmentByID(@Nonnull String containmentID) {
    Objects.requireNonNull(containmentID, "containmentID should not be null");
    return allFeatures().stream()
        .filter(f -> f instanceof Containment)
        .map(f -> (Containment) f)
        .filter(c -> Objects.equals(c.getID(), containmentID))
        .findFirst()
        .orElse(null);
  }

  public @Nullable Containment<V> getContainmentByName(@Nonnull String containmentName) {
    Objects.requireNonNull(containmentName, "containmentName should not be null");
    return allFeatures().stream()
        .filter(f -> f instanceof Containment)
        .map(f -> (Containment) f)
        .filter(c -> Objects.equals(c.getName(), containmentName))
        .findFirst()
        .orElse(null);
  }

  public @Nullable Reference<V> getReferenceByID(@Nonnull String referenceID) {
    Objects.requireNonNull(referenceID, "referenceID should not be null");
    return allFeatures().stream()
        .filter(f -> f instanceof Reference)
        .map(f -> (Reference) f)
        .filter(c -> Objects.equals(c.getID(), referenceID))
        .findFirst()
        .orElse(null);
  }

  public @Nullable Reference<V> getReferenceByName(@Nonnull String referenceName) {
    Objects.requireNonNull(referenceName, "referenceName should not be null");
    return allFeatures().stream()
        .filter(f -> f instanceof Reference)
        .map(f -> (Reference) f)
        .filter(c -> Objects.equals(c.getName(), referenceName))
        .findFirst()
        .orElse(null);
  }

  public @Nonnull Containment<V> requireContainmentByName(@Nonnull String containmentName) {
    Containment<V> containment = getContainmentByName(containmentName);
    if (containment == null) {
      throw new IllegalArgumentException(
          "Containment " + containmentName + " not found in Classifier " + getName());
    }
    return containment;
  }

  public @Nonnull Reference<V> requireReferenceByName(@Nonnull String referenceName) {
    Reference reference = getReferenceByName(referenceName);
    if (reference == null) {
      throw new IllegalArgumentException(
          "Reference " + referenceName + " not found in Classifier " + getName());
    }
    return reference;
  }

  public @Nullable Link<?, V> getLinkByName(@Nonnull String linkName) {
    Objects.requireNonNull(linkName, "linkName should not be null");
    return allFeatures().stream()
        .filter(f -> f instanceof Link)
        .map(f -> (Link) f)
        .filter(c -> Objects.equals(c.getName(), linkName))
        .findFirst()
        .orElse(null);
  }

  public @Nullable Property<V> getPropertyByMetaPointer(MetaPointer metaPointer) {

    return this.allProperties().stream()
        .filter(p -> MetaPointer.from(p).equals(metaPointer))
        .findFirst()
        .orElse(null);
  }

  public @Nullable Containment<V> getContainmentByMetaPointer(MetaPointer metaPointer) {
    return this.allContainments().stream()
        .filter(p -> MetaPointer.from(p).equals(metaPointer))
        .findFirst()
        .orElse(null);
  }

  public @Nullable Reference<V> getReferenceByMetaPointer(MetaPointer metaPointer) {
    return this.allReferences().stream()
        .filter(p -> MetaPointer.from(p).equals(metaPointer))
        .findFirst()
        .orElse(null);
  }

  protected void combineFeatures(List<Feature<?, V>> featuresA, List<Feature<?, V>> featuresB) {
    Set<MetaPointer> existingMetapointers = new HashSet<>();
    for (Feature<?, V> f : featuresA) {
      existingMetapointers.add(MetaPointer.from(f));
    }
    for (Feature<?, V> f : featuresB) {
      MetaPointer metaPointer = MetaPointer.from(f);
      if (!existingMetapointers.contains(metaPointer)) {
        existingMetapointers.add(metaPointer);
        featuresA.add(f);
      }
    }
  }
}
