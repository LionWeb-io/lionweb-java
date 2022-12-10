package org.lionweb.lioncore.java.metamodel;

import org.lionweb.lioncore.java.self.LionCore;

import java.util.LinkedList;
import java.util.List;

public class Enumeration extends DataType implements NamespaceProvider {
    private List<EnumerationLiteral> literals = new LinkedList<>();

    public Enumeration() {
        super();
        setConcept(LionCore.getEnumeration());
    }

    public Enumeration(Metamodel metamodel, String simpleName) {
        super(metamodel, simpleName);
        setConcept(LionCore.getEnumeration());
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
