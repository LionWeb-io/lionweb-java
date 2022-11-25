package org.lionweb.lioncore.java;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

public class EnumDeclaration extends DataType {
    private List<EnumLiteral> literals = new LinkedList<>();

    public EnumDeclaration(Metamodel metamodel, String simpleName) {
        super(metamodel, simpleName);
    }

    public List<EnumLiteral> getLiterals() {
        return literals;
    }

    public void addLiteral(EnumLiteral literal) {
        if (literals.stream().anyMatch(enumLiteral -> enumLiteral.getName().equals(literal.getName()))) {
            throw new IllegalArgumentException("Duplicate literal name");
        }
        this.literals.add(literal);
    }
}
