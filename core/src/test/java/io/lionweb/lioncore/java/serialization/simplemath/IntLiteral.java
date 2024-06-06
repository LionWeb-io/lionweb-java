package io.lionweb.lioncore.java.serialization.simplemath;

import io.lionweb.lioncore.java.language.Concept;
import io.lionweb.lioncore.java.language.Property;
import io.lionweb.lioncore.java.serialization.SimpleNode;
import java.util.Objects;

public class IntLiteral extends SimpleNode {
  private final int value;

  public IntLiteral(int value) {
    assignRandomID();
    this.value = value;
  }

  public IntLiteral(int value, String id) {
    setId(id);
    this.value = value;
  }

  @Override
  public Concept getClassifier() {
    return SimpleMathLanguage.INT_LITERAL;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof IntLiteral)) return false;
    IntLiteral that = (IntLiteral) o;
    return Objects.equals(getID(), that.getID()) && value == that.value;
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }

  @Override
  public String toString() {
    return "IntLiteral{" + "value=" + value + '}';
  }

  @Override
  protected Object concreteGetPropertyValue(Property property) {
    if (property.getName().equals("value")) {
      return value;
    }
    return super.concreteGetPropertyValue(property);
  }
}
