package io.lionweb.lioncore.java.model.impl;

import io.lionweb.lioncore.java.language.Enumeration;
import io.lionweb.lioncore.java.language.EnumerationLiteral;
import java.util.Objects;
import javax.annotation.Nonnull;

/** This represents an Enumeration Value when the actual Enum class is not available. */
public class EnumerationValueImpl implements EnumerationValue {
  private final EnumerationLiteral enumerationLiteral;

  public EnumerationValueImpl(@Nonnull EnumerationLiteral enumerationLiteral) {
    this.enumerationLiteral = enumerationLiteral;
  }

  public Enumeration getEnumeration() {
    return enumerationLiteral.getEnumeration();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof EnumerationValueImpl)) return false;
    EnumerationValueImpl that = (EnumerationValueImpl) o;
    return Objects.equals(enumerationLiteral, that.enumerationLiteral);
  }

  @Override
  public int hashCode() {
    return Objects.hash(enumerationLiteral);
  }

  @Override
  public EnumerationLiteral getEnumerationLiteral() {
    return enumerationLiteral;
  }

  @Override
  public String toString() {
    return "EnumerationValue("
        + enumerationLiteral.getEnumeration().getName()
        + "."
        + enumerationLiteral.getName()
        + ")";
  }
}
