package org.lionweb.lioncore.java.metamodel;

import org.lionweb.lioncore.java.self.LionCore;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public class Enumeration extends DataType<Enumeration> implements NamespaceProvider {
    public Enumeration() {
        super();
    }

    public Enumeration(@Nullable Metamodel metamodel, @Nullable String simpleName) {
        super(metamodel, simpleName);
    }

    public @Nonnull List<EnumerationLiteral> getLiterals() {
        return getContainmentMultipleValue("literals");
    }

    public void addLiteral(@Nonnull EnumerationLiteral literal) {
        Objects.requireNonNull(literal, "literal should not be null");
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
