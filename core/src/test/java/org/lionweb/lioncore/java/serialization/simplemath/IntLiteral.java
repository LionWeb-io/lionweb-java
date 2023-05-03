package org.lionweb.lioncore.java.serialization.simplemath;

import java.util.Objects;
import org.lionweb.lioncore.java.metamodel.Concept;
import org.lionweb.lioncore.java.metamodel.Property;
import org.lionweb.lioncore.java.serialization.SimpleNode;

public class IntLiteral extends SimpleNode {
  private int value;

  public IntLiteral(int value) {
    assignRandomID();
    this.value = value;
  }

  public IntLiteral(int value, String id) {
    setId(id);
    this.value = value;
  }

  @Override
  public Concept getConcept() {
    return SimpleMathMetamodel.INT_LITERAL;
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
