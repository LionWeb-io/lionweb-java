package org.lionweb.lioncore.java.metamodel;

import org.lionweb.lioncore.java.model.impl.M3Node;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * This represents a group of elements that shares some characteristics.
 * <p>
 * For example, Dated and Invoice could be both AbstractConcepts, while having different levels of tightness in the
 * groups.
 *
 * @see org.eclipse.emf.ecore.EClass Ecore equivalent <i>EClass</i> (which is used both for classes and interfaces)
 * @see <a href="http://127.0.0.1:63320/node?ref=r%3A00000000-0000-4000-0000-011c89590292%28jetbrains.mps.lang.structure.structure%29%2F1169125787135">MPS equivalent <i>AbstractConceptDeclaration</i> in local MPS</a>
 * @see org.jetbrains.mps.openapi.language.SAbstractConcept MPS equivalent <i>SAbstractConcept</i> in SModel
 */
public abstract class FeaturesContainer<T extends M3Node> extends MetamodelElement<T> implements NamespaceProvider {
    public FeaturesContainer() {
        super();
    }

    public FeaturesContainer(@Nullable Metamodel metamodel, @Nullable String simpleName, @Nonnull String id) {
        super(metamodel, simpleName, id);
    }

    public FeaturesContainer(@Nullable Metamodel metamodel, @Nullable String simpleName) {
        super(metamodel, simpleName);
    }

    public @Nullable Feature getFeatureByName(@Nonnull String simpleName) {
        Objects.requireNonNull(simpleName, "simpleName should not be null");
        return allFeatures().stream().filter(feature -> feature.getSimpleName().equals(simpleName)).findFirst()
                .orElse(null);
    }

    public abstract @Nonnull List<Feature> allFeatures();

    public @Nonnull List<Property> allProperties() {
        return allFeatures().stream().filter(f -> f instanceof Property).map(f -> (Property) f).collect(Collectors.toList());
    }

    public @Nonnull List<Containment> allContainments() {
        return allFeatures().stream().filter(f -> f instanceof Containment).map(f -> (Containment) f).collect(Collectors.toList());
    }

    public @Nonnull List<Reference> allReferences() {
        return allFeatures().stream().filter(f -> f instanceof Reference).map(f -> (Reference) f).collect(Collectors.toList());
    }

    // TODO should this expose an immutable list to force users to use methods on this class
    //      to modify the collection?
    public @Nonnull List<Feature> getFeatures() {
        return this.getContainmentMultipleValue("features");
    }

    public T addFeature(@Nonnull Feature feature) {
        Objects.requireNonNull(feature, "feature should not be null");
        this.addContainmentMultipleValue("features", feature);
        feature.setContainer(this);
        return (T)this;
    }

    @Override
    public String namespaceQualifier() {
        return this.qualifiedName();
    }

    public @Nullable Property getPropertyByID(@Nonnull String propertyId) {
        Objects.requireNonNull(propertyId, "propertyId should not be null");
        return allFeatures().stream().filter(f -> f instanceof Property).map(f -> (Property) f)
                .filter(p -> Objects.equals(p.getID(), propertyId)).findFirst().orElse(null);
    }

    public @Nullable Property getPropertyByName(@Nonnull String propertyName) {
        Objects.requireNonNull(propertyName, "propertyName should not be null");
        return allFeatures().stream().filter(f -> f instanceof Property).map(f -> (Property) f)
                .filter(p -> Objects.equals(p.getSimpleName(), propertyName)).findFirst().orElse(null);
    }

    public @Nullable Containment getContainmentByID(@Nonnull String containmentID) {
        Objects.requireNonNull(containmentID, "containmentID should not be null");
        return allFeatures().stream().filter(f -> f instanceof Containment).map(f -> (Containment) f)
                .filter(c -> Objects.equals(c.getID(), containmentID)).findFirst().orElse(null);
    }

    public @Nullable Containment getContainmentByName(@Nonnull String containmentName) {
        Objects.requireNonNull(containmentName, "containmentName should not be null");
        return allFeatures().stream().filter(f -> f instanceof Containment).map(f -> (Containment) f)
                .filter(c -> Objects.equals(c.getSimpleName(), containmentName)).findFirst().orElse(null);
    }

    public @Nullable Reference getReferenceByID(@Nonnull String referenceID) {
        Objects.requireNonNull(referenceID, "referenceID should not be null");
        return allFeatures().stream().filter(f -> f instanceof Reference).map(f -> (Reference) f)
                .filter(c -> Objects.equals(c.getID(), referenceID)).findFirst().orElse(null);
    }
    public @Nullable Reference getReferenceByName(@Nonnull String referenceName) {
        Objects.requireNonNull(referenceName, "referenceName should not be null");
        return allFeatures().stream().filter(f -> f instanceof Reference).map(f -> (Reference) f)
                .filter(c -> Objects.equals(c.getSimpleName(), referenceName)).findFirst().orElse(null);
    }

}
