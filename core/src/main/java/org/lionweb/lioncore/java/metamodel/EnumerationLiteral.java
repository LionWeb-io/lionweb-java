package org.lionweb.lioncore.java.metamodel;

import org.lionweb.lioncore.java.model.AnnotationInstance;
import org.lionweb.lioncore.java.model.Model;
import org.lionweb.lioncore.java.model.Node;
import org.lionweb.lioncore.java.model.impl.BaseNode;

import java.util.List;

public class EnumerationLiteral extends BaseNode implements NamespacedEntity {
    private String simpleName;

    public Enumeration getEnumeration() {
        return enumeration;
    }

    public void setEnumeration(Enumeration enumeration) {
        this.enumeration = enumeration;
    }

    private Enumeration enumeration;

    public EnumerationLiteral(String simpleName) {
        this.simpleName = simpleName;
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
        return getEnumeration();
    }

}
