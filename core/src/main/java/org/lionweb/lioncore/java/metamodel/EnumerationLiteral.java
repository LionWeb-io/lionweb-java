package org.lionweb.lioncore.java.metamodel;

import org.lionweb.lioncore.java.model.AnnotationInstance;
import org.lionweb.lioncore.java.model.Model;
import org.lionweb.lioncore.java.model.Node;
import org.lionweb.lioncore.java.model.impl.BaseNode;

import java.util.List;

import org.lionweb.lioncore.java.self.LionCore;
import org.lionweb.lioncore.java.utils.Validatable;

public class EnumerationLiteral extends BaseNode implements NamespacedEntity, Validatable {
    private String simpleName;

    private Enumeration enumeration;

    public EnumerationLiteral() {
        setConcept(LionCore.getEnumerationLiteral());
    }

    public EnumerationLiteral(String simpleName) {
        this.simpleName = simpleName;
        setConcept(LionCore.getEnumerationLiteral());
    }

    public Enumeration getEnumeration() {
        return enumeration;
    }

    public void setEnumeration(Enumeration enumeration) {
        this.enumeration = enumeration;
    }

    @Override
    public String getSimpleName() {
        return simpleName;
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
        return getEnumeration();
    }

    @Override
    public Validatable.ValidationResult validate() {
        return new Validatable.ValidationResult()
                .checkForError(() -> getSimpleName() == null, "Simple name not set");
    }

    @Override
    public Object getPropertyValue(Property property) {
        if (property == LionCore.getEnumerationLiteral().getPropertyByName("simpleName")) {
            return this.getSimpleName();
        }
        if (property == LionCore.getEnumerationLiteral().getPropertyByName("qualifiedName")) {
            return this.qualifiedName();
        }
        return super.getPropertyValue(property);
    }

    @Override
    public void setPropertyValue(Property property, Object value) {
        if (property == LionCore.getEnumerationLiteral().getPropertyByName("simpleName")) {
            setSimpleName((String)value);
            return;
        }
        super.setPropertyValue(property, value);
    }
}
