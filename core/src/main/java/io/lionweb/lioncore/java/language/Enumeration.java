package io.lionweb.lioncore.java.language;

import io.lionweb.lioncore.java.versions.LionWebVersion;
import io.lionweb.lioncore.java.self.LionCore;
import io.lionweb.lioncore.java.versions.LionWebVersionToken;

import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Enumeration<V extends LionWebVersionToken> extends DataType<Enumeration<V>, V> implements NamespaceProvider {
  public Enumeration(@Nonnull LionWebVersion lionWebVersion) {
    super(lionWebVersion);
  }

  public Enumeration() {
    super();
  }

  public Enumeration(@Nullable Language<V> language, @Nullable String name) {
    super(language, name);
  }

  public @Nonnull List<EnumerationLiteral<V>> getLiterals() {
    return getContainmentMultipleValue("literals");
  }

  public void addLiteral(@Nonnull EnumerationLiteral<V> literal) {
    Objects.requireNonNull(literal, "literal should not be null");
    this.addContainmentMultipleValue("literals", literal);
  }

  @Override
  public String namespaceQualifier() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Concept<V> getClassifier() {
    return LionCore.getEnumeration(getLionWebVersionToken());
  }
}
