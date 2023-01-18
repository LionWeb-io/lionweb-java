package org.lionweb.lioncore.java.metamodel;

import org.lionweb.lioncore.java.model.impl.BaseNode;
import org.lionweb.lioncore.java.self.LionCore;

import javax.annotation.Nullable;

public class EnumerationLiteral extends BaseNode implements NamespacedEntity {
    private String simpleName;
    private Enumeration enumeration;

    public EnumerationLiteral() {
        setConcept(LionCore.getEnumerationLiteral());
    }

    public EnumerationLiteral(@Nullable String simpleName) {
        this.simpleName = simpleName;
        setConcept(LionCore.getEnumerationLiteral());
    }

    @Override
    public @Nullable String getSimpleName() {
        return simpleName;
    }

    public void setSimpleName(@Nullable String simpleName) {
        this.simpleName = simpleName;
    }

    public @Nullable Enumeration getEnumeration() {
        return enumeration;
    }

    public void setEnumeration(@Nullable Enumeration enumeration) {
        this.enumeration = enumeration;
    }

    @Override
    public @Nullable Enumeration getContainer() {
        return enumeration;
    }
}
