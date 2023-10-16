package io.lionweb.lioncore.java.model.impl;

import io.lionweb.lioncore.java.language.Enumeration;
import java.util.Objects;
import javax.annotation.Nonnull;

/** This represents an Enumeration Value when the actual Enum class is not available. */
public class DynamicEnumerationValue {
  private final Enumeration enumeration;
  private final String serializedValue;

  public DynamicEnumerationValue(
      @Nonnull Enumeration enumeration, @Nonnull String serializedValue) {
    Objects.requireNonNull(enumeration);
    Objects.requireNonNull(serializedValue);
    this.enumeration = enumeration;
    this.serializedValue = serializedValue;
  }

  public Enumeration getEnumeration() {
    return enumeration;
  }

  public String getSerializedValue() {
    return serializedValue;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof DynamicEnumerationValue)) return false;
    DynamicEnumerationValue that = (DynamicEnumerationValue) o;
    return Objects.equals(enumeration, that.enumeration)
        && Objects.equals(serializedValue, that.serializedValue);
  }

  @Override
  public int hashCode() {
    return Objects.hash(enumeration, serializedValue);
  }
}
