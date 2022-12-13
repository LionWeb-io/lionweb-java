package org.lionweb.lioncore.java.metamodel;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;

public class Enumeration extends DataType implements NamespaceProvider {
    private List<EnumerationLiteral> literals = new LinkedList<>();

    public Enumeration() {
        super();
    }

    public Enumeration(@Nullable Metamodel metamodel, @Nullable String simpleName) {
        super(metamodel, simpleName);
    }

    public @Nonnull List<EnumerationLiteral> getLiterals() {
        return literals;
    }

    public void addLiteral(@Nonnull EnumerationLiteral literal) {
        this.literals.add(literal);
    }

    @Override
    public String namespaceQualifier() {
        throw new UnsupportedOperationException();
    }
}
