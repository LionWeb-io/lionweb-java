package org.lionweb.lioncore.java.metamodel;

import org.lionweb.lioncore.java.model.AnnotationInstance;
import org.lionweb.lioncore.java.model.Model;
import org.lionweb.lioncore.java.model.Node;
import org.lionweb.lioncore.java.model.impl.BaseNode;
import org.lionweb.lioncore.java.self.LionCore;
import org.lionweb.lioncore.java.utils.Naming;
import org.lionweb.lioncore.java.utils.Validatable;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A MetamodelElement is an element with an identity within a {@link Metamodel}.
 *
 * For example, Invoice, Currency, Named, or String could be MetamodelElements.
 *
 * @see org.eclipse.emf.ecore.EClassifier Ecore equivalent <i>EClassifier</i>
 * @see <a href="http://127.0.0.1:63320/node?ref=r%3A00000000-0000-4000-0000-011c89590292%28jetbrains.mps.lang.structure.structure%29%2F1588368162880706270">MPS equivalent <i>IStructureElement</i> in local MPS</a>
 * @see org.jetbrains.mps.openapi.language.SElement MPS equivalent <i>SElement</i> in SModel
 */

public abstract class MetamodelElement extends BaseNode implements NamespacedEntity, Validatable {
    private Metamodel metamodel;
    private String simpleName;

    public MetamodelElement() {

    }

    public MetamodelElement(Metamodel metamodel, String simpleName) {
        // TODO enforce uniqueness of the name within the Metamodel
        Naming.validateSimpleName(simpleName);
        this.metamodel = metamodel;
        this.simpleName = simpleName;
    }

    public Metamodel getMetamodel() {
        return this.metamodel;
    }

    @Override
    public String getSimpleName() {
        return this.simpleName;
    }

    public void setSimpleName(String simpleName) {
        this.simpleName = simpleName;
    }

    @Override
    public String qualifiedName() {
        return this.getContainer().namespaceQualifier() + "." + this.getSimpleName();
    }

    @Override
    public NamespaceProvider getContainer() {
        return this.metamodel;
    }

    @Override
    public Validatable.ValidationResult validate() {
        return new Validatable.ValidationResult()
                .checkForError(() -> getSimpleName() == null, "Simple name not set")
                .checkForError(() -> getMetamodel() == null, "Metamodel not set");
    }

    @Override
    public Object getPropertyValue(Property property) {
        if (property == LionCore.getFeaturesContainer().getPropertyByName("simpleName")) {
            return this.getSimpleName();
        }
        if (property == LionCore.getAnnotation().getPropertyByName("qualifiedName")) {
            return this.qualifiedName();
        }
        return super.getPropertyValue(property);
    }

    @Override
    public void setPropertyValue(Property property, Object value) {
        if (property == LionCore.getAnnotation().getPropertyByName("simpleName")) {
            setSimpleName((String)value);
            return;
        }
        super.setPropertyValue(property, value);
    }

}