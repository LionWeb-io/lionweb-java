package io.lionweb.lioncore.java.language;

import io.lionweb.lioncore.java.self.LionCore;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents a collection of named instances of Data Types. They are meant to support a small
 * composite of values that semantically form a unit. Instances of StructuredDataTypes have no
 * identity, are always copied by value, and SHOULD be immutable. Two instances of a
 * StructuredDataType that hold the same values for all fields of that StructuredDataType are
 * interchangeable. This is different from the instances of Classifiers which have an identity,
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

  public StructuredDataType(
      @Nullable Language language, @Nullable String name, String id, String key) {
    super(language, name);
    setID(id);
    setKey(key);
  }

  public @Nonnull StructuredDataType addField(@Nonnull Field field) {
    Objects.requireNonNull(field, "field should not be null");
    this.addContainmentMultipleValue("fields", field);
    field.setParent(this);
    return this;
  }

  public @Nonnull List<Field> getFields() {
    return this.getContainmentMultipleValue("fields");
  }

  @Override
  public @Nonnull String namespaceQualifier() {
    return this.qualifiedName();
  }

  @Override
  public @Nonnull Concept getClassifier() {
    return LionCore.getStructuredDataType(getLionWebVersion());
  }

  public @Nullable Field getFieldByName(@Nonnull String fieldName) {
    Objects.requireNonNull(fieldName, "fieldName should not be null");
    return getFields().stream()
        .filter(p -> Objects.equals(p.getName(), fieldName))
        .findFirst()
        .orElse(null);
  }
}
