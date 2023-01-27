package org.lionweb.lioncore.java.metamodel;

import org.lionweb.lioncore.java.self.LionCore;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class Enumeration extends DataType implements NamespaceProvider {
    public Enumeration() {
        super();
    }

    public Enumeration(@Nullable Metamodel metamodel, @Nullable String simpleName) {
        super(metamodel, simpleName);
    }

    public @Nonnull List<EnumerationLiteral> getLiterals() {
        return getLinkMultipleValue("literals");
    }

    public void addLiteral(@Nonnull EnumerationLiteral literal) {
        this.addContainmentMultipleValue("literals", literal);
    }

    @Override
    public String namespaceQualifier() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Concept getConcept() {
        return LionCore.getEnumeration();
    }
}
