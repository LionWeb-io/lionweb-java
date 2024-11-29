package io.lionweb.lioncore.java.language;

import io.lionweb.lioncore.java.self.LionCore;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

/**
 * Represents a collection of named instances of Data Types. They are meant to support a small composite of values
 * that semantically form a unit. Instances of StructuredDataTypes have no identity, are always copied by value, and
 * SHOULD be immutable. Two instances of a StructuredDataType that hold the same values for all fields of that
 * StructuredDataType are interchangeable. This is different from the instances of Classifiers which have an identity,
 * through their id.
 */
public class StructuredDataType extends DataType<StructuredDataType> implements NamespaceProvider {

    public StructuredDataType() {
        super();
    }

    public StructuredDataType(@Nonnull String id) {
        super(id);
    }

    public StructuredDataType(@Nullable Language language, @Nullable String name) {
        super(language, name);
    }

    public StructuredDataType(@Nullable Language language, @Nullable String name, String id) {
        super(language, name);
        setID(id);
    }

    public StructuredDataType addField(@Nonnull Field field) {
        Objects.requireNonNull(field, "field should not be null");
        this.addContainmentMultipleValue("fields", field);
        field.setParent(this);
        return this;
    }

    public @Nonnull List<LanguageEntity> getFields() {
        return this.getContainmentMultipleValue("fields");
    }

    @Override
    public String namespaceQualifier() {
        return this.qualifiedName();
    }

    @Override
    public Concept getClassifier() {
        return LionCore.getStructuredDataType();
    }
}
