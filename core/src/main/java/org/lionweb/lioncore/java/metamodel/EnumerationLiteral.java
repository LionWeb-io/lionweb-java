package org.lionweb.lioncore.java.metamodel;

import org.lionweb.lioncore.java.model.impl.M3Node;
import org.lionweb.lioncore.java.self.LionCore;

import javax.annotation.Nullable;

public class EnumerationLiteral extends M3Node<EnumerationLiteral> implements NamespacedEntity {

    public EnumerationLiteral() {
    }

    public EnumerationLiteral(@Nullable String simpleName) {
        setSimpleName(simpleName);
    }

    @Override
    public @Nullable String getSimpleName() {
        return getPropertyValue("simpleName", String.class);
    }

    public void setSimpleName(@Nullable String simpleName) {
        this.setPropertyValue("simpleName", simpleName);
    }

    public @Nullable Enumeration getEnumeration() {
        return getLinkSingleValue("enumeration");
    }

    public void setEnumeration(@Nullable Enumeration enumeration) {
        this.setReferenceSingleValue("enumeration", enumeration);
    }

    @Override
    public @Nullable Enumeration getContainer() {
        return this.getLinkSingleValue("enumeration");
    }

    @Override
    public Concept getConcept() {
        return LionCore.getEnumerationLiteral();
    }
}
