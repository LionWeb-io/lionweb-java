package org.lionweb.lioncore.java.metamodel;

import org.lionweb.lioncore.java.Experimental;
import org.lionweb.lioncore.java.model.AnnotationInstance;
import org.lionweb.lioncore.java.model.Model;
import org.lionweb.lioncore.java.model.Node;
import org.lionweb.lioncore.java.model.impl.BaseNode;
import org.lionweb.lioncore.java.self.LionCore;
import org.lionweb.lioncore.java.utils.Naming;
import org.lionweb.lioncore.java.utils.Validatable;

import java.util.List;

/**
 * A Feature represents a characteristic or some form of data associated with a particular concept.
 *
 * For example, an Invoice can have an associated date, a number, a connection with a customer, and it can contain
 * InvoiceLines. All of this information is represented by features.
 *
 * @see org.eclipse.emf.ecore.EStructuralFeature Ecore equivalent <i>EStructuralFeature</i>
 * @see <a href="https://www.jetbrains.com/help/mps/structure.html#conceptmembers">MPS equivalent <i>Concept members</i> in documentation</a>
 * @see org.jetbrains.mps.openapi.language.SConceptFeature MPS equivalent <i>SConceptFeature</i> in SModel
 */
public abstract class Feature extends BaseNode implements NamespacedEntity, Validatable {
    private boolean optional;
    @Experimental
    private boolean derived;

    private String simpleName;
    private FeaturesContainer container;

    public Feature() {

    }

    public Feature(String simpleName, FeaturesContainer container) {
        // TODO verify that the container is also a NamespaceProvider
        // TODO enforce uniqueness of the name within the FeauturesContainer
        Naming.validateSimpleName(simpleName);
        this.simpleName = simpleName;
        this.container = container;
    }

    public boolean isOptional() {
        return optional;
    }

    public boolean isRequired() {
        return !optional;
    }

    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    @Experimental
    public boolean isDerived() {
        return derived;
    }

    @Experimental
    public void setDerived(boolean derived) {
        this.derived = derived;
    }

    @Override
    public String getSimpleName() {
        return simpleName;
    }

    @Override
    public String qualifiedName() {
        return this.getContainer().namespaceQualifier() + "." + this.getSimpleName();
    }

    @Override
    public NamespaceProvider getContainer() {
        return (NamespaceProvider) container;
    }

    public void setContainer(FeaturesContainer container) {
        this.container = container;
    }

    @Override
    public Validatable.ValidationResult validate() {
        return new Validatable.ValidationResult()
                .checkForError(() -> getSimpleName() == null, "Simple name not set")
                .checkForError(() -> getContainer() == null, "Container not set");
    }

    @Override
    public Object getPropertyValue(Property property) {
        if (property == LionCore.getFeature().getPropertyByName("optional")) {
            return this.isOptional();
        }
        return super.getPropertyValue(property);
    }

    @Override
    public void setPropertyValue(Property property, Object value) {
        if (property == LionCore.getFeature().getPropertyByName("optional")) {
            setOptional((Boolean) value);
            return;
        }
        super.setPropertyValue(property, value);
    }
}
