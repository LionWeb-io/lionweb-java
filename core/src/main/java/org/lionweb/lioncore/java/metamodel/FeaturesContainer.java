package org.lionweb.lioncore.java.metamodel;

import org.lionweb.lioncore.java.model.Node;
import org.lionweb.lioncore.java.self.LionCore;
import org.lionweb.lioncore.java.utils.Validatable;

import java.sql.Ref;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This represents a group of elements that shares some characteristics.
 *
 * For example, Dated and Invoice could be both AbstractConcepts, while having different levels of tightness in the
 * groups.
 *
 * @see org.eclipse.emf.ecore.EClass Ecore equivalent <i>EClass</i> (which is used both for classes and interfaces)
 * @see <a href="http://127.0.0.1:63320/node?ref=r%3A00000000-0000-4000-0000-011c89590292%28jetbrains.mps.lang.structure.structure%29%2F1169125787135">MPS equivalent <i>AbstractConceptDeclaration</i> in local MPS</a>
 * @see org.jetbrains.mps.openapi.language.SAbstractConcept MPS equivalent <i>SAbstractConcept</i> in SModel
 */
public abstract class FeaturesContainer extends MetamodelElement implements NamespaceProvider, Validatable {
    private List<Feature> features = new LinkedList<>();

    public FeaturesContainer() {
        super();
    }

    public FeaturesContainer(Metamodel metamodel, String simpleName) {
        super(metamodel, simpleName);
    }

    public Feature getFeatureByName(String simpleName) {
        return allFeatures().stream().filter(feature -> feature.getSimpleName().equals(simpleName)).findFirst()
                .orElse(null);
    }

    public Property getPropertyByName(String simpleName) {
        return allFeatures().stream().filter(feature -> feature instanceof Property)
                .map(feature -> (Property)feature)
                .filter(feature -> feature.getSimpleName().equals(simpleName)).findFirst()
                .orElse(null);
    }

    public Containment getContainmentByName(String simpleName) {
        return allFeatures().stream().filter(feature -> feature instanceof Containment)
                .map(feature -> (Containment)feature)
                .filter(feature -> feature.getSimpleName().equals(simpleName)).findFirst()
                .orElse(null);
    }

    public Reference getReferenceByName(String simpleName) {
        return allFeatures().stream().filter(feature -> feature instanceof Reference)
                .map(feature -> (Reference)feature)
                .filter(feature -> feature.getSimpleName().equals(simpleName)).findFirst()
                .orElse(null);
    }

    public abstract List<Feature> allFeatures();

    public List<Property> allProperties() {
        return allFeatures().stream().filter(f -> f instanceof Property).map(f -> (Property)f).collect(Collectors.toList());
    }

    public List<Containment> allContainments() {
        return allFeatures().stream().filter(f -> f instanceof Containment).map(f -> (Containment)f).collect(Collectors.toList());
    }

    public List<Reference> allReferences() {
        return allFeatures().stream().filter(f -> f instanceof Reference).map(f -> (Reference)f).collect(Collectors.toList());
    }

    // TODO should this expose an immutable list to force users to use methods on this class
    //      to modify the collection?
    public List<Feature> getFeatures() {
        return this.features;
    }

    public void addFeature(Feature feature) {
        if (feature.getContainer() != this) {
            feature.setContainer(this);
        }
        this.features.add(feature);
    }

    @Override
    public String namespaceQualifier() {
        return this.qualifiedName();
    }

    public void addProperty(String simpleName, DataType dataType, boolean optional, boolean derived) {
        Property property = new Property(simpleName, this);
        property.setType(dataType);
        property.setOptional(optional);
        property.setDerived(derived);
        addFeature(property);
    }

    public void addOptionalProperty(String simpleName, DataType dataType) {
        addProperty(simpleName, dataType, true, false);
    }

    public void addRequiredProperty(String simpleName, DataType dataType) {
        addProperty(simpleName, dataType, false, false);
    }

    public void addReference(String simpleName, FeaturesContainer type, boolean optional, boolean multiple) {
        Reference reference = new Reference(simpleName, this);
        reference.setType(type);
        reference.setDerived(false);
        reference.setOptional(optional);
        reference.setMultiple(multiple);
        addFeature(reference);
    }

    public void addOptionalReference(String simpleName, FeaturesContainer type) {
        addReference(simpleName, type, true, false);
    }

    public void addRequiredReference(String simpleName, FeaturesContainer type) {
        addReference(simpleName, type, false, false);
    }

    public void addMultipleReference(String simpleName, FeaturesContainer type) {
        addReference(simpleName, type, true, true);
    }

    public void addMultipleAndRequiredReference(String simpleName, FeaturesContainer type) {
        addReference(simpleName, type, false, true);
    }

    public void addContainment(String simpleName, FeaturesContainer type, boolean optional, boolean multiple) {
        Containment containment = new Containment(simpleName, this);
        containment.setType(type);
        containment.setDerived(false);
        containment.setOptional(optional);
        containment.setMultiple(multiple);
        addFeature(containment);
    }

    public void addOptionalContainment(String simpleName, FeaturesContainer type) {
        addContainment(simpleName, type, true, false);
    }

    public void addRequiredContainment(String simpleName, FeaturesContainer type) {
        addContainment(simpleName, type, false, false);
    }

    public void addMultipleContainment(String simpleName, FeaturesContainer type) {
        addContainment(simpleName, type, true, true);
    }

    public void addMultipleAndRequiredContainment(String simpleName, FeaturesContainer type) {
        addContainment(simpleName, type, false, true);
    }

    @Override
    public List<Node> getChildren(Containment containment) {
        if (containment == LionCore.getFeaturesContainer().getContainmentByName("features")) {
            return this.getFeatures().stream().collect(Collectors.toList());
        }
        return super.getChildren(containment);
    }

    @Override
    public Object getPropertyValue(Property property) {
        if (property == LionCore.getFeaturesContainer().getPropertyByName("namespaceQualifier")) {
            return this.namespaceQualifier();
        }
        return super.getPropertyValue(property);
    }
}
