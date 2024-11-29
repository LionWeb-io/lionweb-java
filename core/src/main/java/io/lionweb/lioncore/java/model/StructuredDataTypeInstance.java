package io.lionweb.lioncore.java.model;

import io.lionweb.lioncore.java.language.Field;
import io.lionweb.lioncore.java.language.StructuredDataType;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** This a represent in instance of Structured Data Type. */
public interface StructuredDataTypeInstance {

  /** The StructuredDataType of which this StructuredDataTypeInstance is an instance. */
  @Nonnull
  StructuredDataType getStructuredDataType();

  /** Get the field value associated with the specified field. */
  @Nullable
  Object getFieldValue(@Nonnull Field field);

  /**
   * If the value is not compatible with the type of the property, the exception
   * IllegalArgumentValue should be thrown
   */
  void setFieldValue(@Nonnull Field field, @Nullable Object value);
}
