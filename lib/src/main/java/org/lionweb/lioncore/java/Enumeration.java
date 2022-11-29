package org.lionweb.lioncore.java;

import java.util.LinkedList;
import java.util.List;

public class Enumeration extends DataType {
    private List<EnumerationLiteral> literals = new LinkedList<>();

    public Enumeration(Metamodel metamodel, String simpleName) {
        super(metamodel, simpleName);
    }

    public List<EnumerationLiteral> getLiterals() {
        return literals;
    }

    public void addLiteral(EnumerationLiteral literal) {
        if (literals.stream().anyMatch(enumLiteral -> enumLiteral.getName().equals(literal.getName()))) {
            throw new IllegalArgumentException("Duplicate literal name");
        }
        this.literals.add(literal);
    }
}
