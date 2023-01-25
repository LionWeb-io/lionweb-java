package org.lionweb.lioncore.java.metamodel;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;
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
public abstract class FeaturesContainer extends MetamodelElement implements NamespaceProvider {
    private List<Feature> features = new LinkedList<>();

    public FeaturesContainer() {
        super();
    }

    public FeaturesContainer(@Nullable Metamodel metamodel, @Nullable String simpleName) {
        super(metamodel, simpleName);
    }

    public @Nullable Feature getFeatureByName(@Nonnull String simpleName) {
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
        return this.features;
    }

    public void addFeature(@Nonnull Feature feature) {
        this.features.add(feature);
        feature.setContainer(this);
    }

    @Override
    public String namespaceQualifier() {
        return this.qualifiedName();
    }

    public @Nullable Property getPropertyByID(String propertyId) {
        return allFeatures().stream().filter(f -> f instanceof Property).map(f -> (Property) f)
                .filter(p -> p.getID().equals(propertyId)).findFirst().orElse(null);
    }

    public @Nullable Property getPropertyByName(String propertyName) {
        return allFeatures().stream().filter(f -> f instanceof Property).map(f -> (Property) f)
                .filter(p -> p.getSimpleName().equals(propertyName)).findFirst().orElse(null);
    }

    public @Nullable Containment getContainmentByID(String containmentID) {
        return allFeatures().stream().filter(f -> f instanceof Containment).map(f -> (Containment) f)
                .filter(c -> c.getID().equals(containmentID)).findFirst().orElse(null);
    }

    public @Nullable Containment getContainmentByName(String containmentName) {
        return allFeatures().stream().filter(f -> f instanceof Containment).map(f -> (Containment) f)
                .filter(c -> c.getSimpleName().equals(containmentName)).findFirst().orElse(null);
    }

    public @Nullable Reference getReferenceByID(String referenceID) {
        return allFeatures().stream().filter(f -> f instanceof Reference).map(f -> (Reference) f)
                .filter(c -> c.getID().equals(referenceID)).findFirst().orElse(null);
    }
    public @Nullable Reference getReferenceByName(String referenceName) {
        return allFeatures().stream().filter(f -> f instanceof Reference).map(f -> (Reference) f)
                .filter(c -> c.getSimpleName().equals(referenceName)).findFirst().orElse(null);
    }

}
