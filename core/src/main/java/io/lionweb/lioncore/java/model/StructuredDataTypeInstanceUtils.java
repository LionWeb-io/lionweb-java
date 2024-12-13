package io.lionweb.lioncore.java.model;

import io.lionweb.lioncore.java.language.*;
import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Similarly to ClassifierInstanceUtils, this class offer utility methods for working with
 * StructuredDataTypeInstances.
 */
public class StructuredDataTypeInstanceUtils {

  private StructuredDataTypeInstanceUtils() {
    // Prevent instantiation
  }

  @Nullable
  public static Object getFieldValueByName(
      @Nonnull StructuredDataTypeInstance _this, @Nonnull String fieldName) {
    Objects.requireNonNull(_this, "_this should not be null");
    Objects.requireNonNull(fieldName, "fieldName should not be null");
    Field field = _this.getStructuredDataType().getFieldByName(fieldName);
    if (field == null) {
      throw new IllegalArgumentException(
          "StructuredDataType "
              + _this.getStructuredDataType().qualifiedName()
              + " does not contained a field named "
              + fieldName);
    }
    return _this.getFieldValue(field);
  }

  public static void setFieldValueByName(
      @Nonnull StructuredDataTypeInstance _this,
      @Nonnull String fieldName,
      @Nullable Object value) {
    Objects.requireNonNull(_this, "_this should not be null");
    Objects.requireNonNull(fieldName, "fieldName should not be null");
    StructuredDataType structuredDataType = _this.getStructuredDataType();
    if (structuredDataType == null) {
      throw new IllegalStateException(
          "StructuredDataType should not be null for "
              + _this
              + " (class "
              + _this.getClass().getCanonicalName()
              + ")");
    }
    Field field = structuredDataType.getFieldByName(fieldName);
    if (field == null) {
      throw new IllegalArgumentException(
          "StructuredDataType "
              + _this.getStructuredDataType().qualifiedName()
              + " does not contained a field named "
              + fieldName);
    }
    _this.setFieldValue(field, value);
  }
}
