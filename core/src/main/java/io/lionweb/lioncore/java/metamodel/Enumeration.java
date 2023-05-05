package io.lionweb.lioncore.java.metamodel;

import io.lionweb.lioncore.java.self.LionCore;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Enumeration extends DataType<Enumeration> implements NamespaceProvider {
  public Enumeration() {
    super();
  }

  public Enumeration(@Nullable Metamodel metamodel, @Nullable String name) {
    super(metamodel, name);
  }

  public @Nonnull List<EnumerationLiteral> getLiterals() {
    return getContainmentMultipleValue("literals");
  }

  public void addLiteral(@Nonnull EnumerationLiteral literal) {
    Objects.requireNonNull(literal, "literal should not be null");
    this.addContainmentMultipleValue("literals", literal);
  }

  @Override
  public String namespaceQualifier() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Concept getConcept() {
    return LionCore.getEnumeration();
  }
}
