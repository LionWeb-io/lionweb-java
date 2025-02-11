package io.lionweb.lioncore.java.language;

import io.lionweb.lioncore.java.LionWebVersion;
import io.lionweb.lioncore.java.self.LionCore;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Enumeration extends DataType<Enumeration> implements NamespaceProvider {
  public Enumeration(@Nonnull LionWebVersion lionWebVersion) {
    super(lionWebVersion);
  }

  public Enumeration() {
    super();
  }

  public Enumeration(@Nullable Language language, @Nullable String name) {
    super(language, name);
  }

  public @Nonnull List<EnumerationLiteral> getLiterals() {
    return getContainmentMultipleValue("literals");
  }

  public Enumeration addLiteral(@Nonnull EnumerationLiteral literal) {
    Objects.requireNonNull(literal, "literal should not be null");
    this.addContainmentMultipleValue("literals", literal);
    return this;
  }

  @Override
  public String namespaceQualifier() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Concept getClassifier() {
    return LionCore.getEnumeration(getLionWebVersion());
  }
}
