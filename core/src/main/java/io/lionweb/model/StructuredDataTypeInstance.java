package io.lionweb.model;

import io.lionweb.language.Field;
import io.lionweb.language.StructuredDataType;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** This represents an instance of Structured Data Type. */
public interface StructuredDataTypeInstance {

  /** The StructuredDataType of which this StructuredDataTypeInstance is an instance. */
  @Nonnull
  StructuredDataType getStructuredDataType();

  /** Get the field value associated with the specified field. */
  @Nullable
  Object getFieldValue(@Nonnull Field field);

  /**
   * @throws IllegalArgumentException If the value is not compatible with the type of the property
   */
  void setFieldValue(@Nonnull Field field, @Nullable Object value) throws IllegalArgumentException;
}
