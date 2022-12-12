package org.lionweb.lioncore.java.metamodel;

public class EnumerationLiteral implements NamespacedEntity {
    private String simpleName;
    private Enumeration enumeration;

    public EnumerationLiteral() {

    }

    public EnumerationLiteral(String simpleName) {
        this.simpleName = simpleName;
    }

    @Override
    public String getSimpleName() {
        return simpleName;
    }

    public void setSimpleName(String simpleName) {
        this.simpleName = simpleName;
    }

    public Enumeration getEnumeration() {
        return enumeration;
    }

    public void setEnumeration(Enumeration enumeration) {
        this.enumeration = enumeration;
    }

    @Override
    public Enumeration getContainer() {
        return enumeration;
    }
}
