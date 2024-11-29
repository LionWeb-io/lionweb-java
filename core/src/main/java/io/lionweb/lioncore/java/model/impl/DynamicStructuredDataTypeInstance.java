package io.lionweb.lioncore.java.model.impl;

import io.lionweb.lioncore.java.language.Field;
import io.lionweb.lioncore.java.language.StructuredDataType;
import io.lionweb.lioncore.java.model.StructuredDataTypeInstance;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DynamicStructuredDataTypeInstance implements StructuredDataTypeInstance {
  private StructuredDataType structuredDataType;
  private Map<Field, Object> fieldValues = new HashMap<>();

  public DynamicStructuredDataTypeInstance(@Nonnull StructuredDataType structuredDataType) {
    Objects.requireNonNull(structuredDataType, "structuredDataType should not be null");
    this.structuredDataType = structuredDataType;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof StructuredDataTypeInstance)) {
      return false;
    }
    StructuredDataTypeInstance other = (StructuredDataTypeInstance) obj;
    StructuredDataType structuredDataType = getStructuredDataType();
    if (structuredDataType == null || !(structuredDataType.equals(other.getStructuredDataType()))) {
      return false;
    }

    for (Field field : structuredDataType.getFields()) {
      if (!Objects.equals(getFieldValue(field), other.getFieldValue(field))) {
        return false;
      }
    }
    return true;
  }

  @Override
  public int hashCode() {
    int hashCode = 1;
    StructuredDataType structuredDataType = getStructuredDataType();
    if (structuredDataType != null) {
      hashCode = 7 * structuredDataType.getID().hashCode();
    }
    for (Field field : structuredDataType.getFields()) {
      hashCode = hashCode + 3 * Objects.hashCode(getFieldValue(field));
    }
    return hashCode;
  }

  @Override
  public @Nonnull StructuredDataType getStructuredDataType() {
    return structuredDataType;
  }

  @Override
  public @Nullable Object getFieldValue(@Nonnull Field field) {
    Objects.requireNonNull(field, "field should not be null");
    if (field.getID() == null) {
      throw new IllegalStateException("Field with no ID specified should not be used");
    }
    if (!getStructuredDataType().getFields().contains(field)) {
      throw new IllegalArgumentException(
          "Invalid field for StructuredDataType " + getStructuredDataType());
    }
    return fieldValues.get(field);
  }

  @Override
  public void setFieldValue(@Nonnull Field field, @Nullable Object value) {
    Objects.requireNonNull(field, "field should not be null");
    if (field.getID() == null) {
      throw new IllegalStateException("Field with no ID specified should not be used");
    }
    if (!getStructuredDataType().getFields().contains(field)) {
      throw new IllegalArgumentException(
          "Invalid field for StructuredDataType " + getStructuredDataType());
    }
    fieldValues.put(field, value);
  }

  @Override
  public String toString() {
    return "DynamicStructuredDataTypeInstance{"
        + "structuredDataType="
        + structuredDataType
        + ", fieldValues="
        + fieldValues
        + '}';
  }
}
