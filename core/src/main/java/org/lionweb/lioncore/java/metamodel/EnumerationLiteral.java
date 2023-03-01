package org.lionweb.lioncore.java.metamodel;

import org.lionweb.lioncore.java.model.ReferenceValue;
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
        return getReferenceSingleValue("enumeration");
    }

    public void setEnumeration(@Nullable Enumeration enumeration) {
        if (enumeration == null) {
            this.setReferenceSingleValue("enumeration", null);
        } else {
            this.setReferenceSingleValue("enumeration", new ReferenceValue(enumeration, enumeration.getSimpleName()));
        }
    }

    @Override
    public @Nullable Enumeration getContainer() {
        return this.getReferenceSingleValue("enumeration");
    }

    @Override
    public Concept getConcept() {
        return LionCore.getEnumerationLiteral();
    }
}
