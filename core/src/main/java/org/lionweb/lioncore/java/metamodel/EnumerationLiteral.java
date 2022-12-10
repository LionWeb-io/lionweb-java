package org.lionweb.lioncore.java.metamodel;

import org.lionweb.lioncore.java.utils.Validatable;

public class EnumerationLiteral implements Validatable  {
    private String name;

    public EnumerationLiteral() {

    }

    public EnumerationLiteral(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Validatable.ValidationResult validate() {
        return new Validatable.ValidationResult()
                .checkForError(() -> getName() == null, "Name not set");
    }
}
