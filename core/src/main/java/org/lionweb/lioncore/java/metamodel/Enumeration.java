package org.lionweb.lioncore.java.metamodel;

import java.util.LinkedList;
import java.util.List;

public class Enumeration extends DataType implements NamespaceProvider {
    private List<EnumerationLiteral> literals = new LinkedList<>();

    public Enumeration() {
        super();
    }

    public Enumeration(Metamodel metamodel, String simpleName) {
        super(metamodel, simpleName);
    }

    public List<EnumerationLiteral> getLiterals() {
        return literals;
    }

    public void addLiteral(EnumerationLiteral literal) {
        if (literals.stream().anyMatch(enumLiteral -> enumLiteral.getSimpleName().equals(literal.getSimpleName()))) {
            throw new IllegalArgumentException("Duplicate literal name");
        }
        this.literals.add(literal);
    }

    @Override
    public String namespaceQualifier() {
        throw new UnsupportedOperationException();
    }
}
