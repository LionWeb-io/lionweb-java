package io.lionweb.lioncore.java.language;

import io.lionweb.lioncore.java.versions.LionWebVersion;
import io.lionweb.lioncore.java.model.Node;
import io.lionweb.lioncore.java.model.impl.M3Node;
import io.lionweb.lioncore.java.self.LionCore;
import io.lionweb.lioncore.java.versions.LionWebVersionToken;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EnumerationLiteral<V extends LionWebVersionToken> extends M3Node<EnumerationLiteral<V>, V>
    implements NamespacedEntity, IKeyed<EnumerationLiteral<V>> {

  public EnumerationLiteral(@Nonnull LionWebVersion lionWebVersion) {
    super(lionWebVersion);
  }

  public EnumerationLiteral() {}

  public EnumerationLiteral(@Nullable String name) {
    setName(name);
  }

  public EnumerationLiteral(@Nonnull Enumeration<V> enumeration, @Nullable String name) {
    enumeration.addLiteral(this);
    setParent(enumeration);
    setName(name);
  }

  @Override
  public @Nullable String getName() {
    return getPropertyValue("name", String.class);
  }

  public void setName(@Nullable String name) {
    this.setPropertyValue("name", name);
  }

  public @Nullable Enumeration<V> getEnumeration() {
    Node parent = getParent();
    if (parent == null) {
      return null;
    } else if (parent instanceof Enumeration<?>) {
      return (Enumeration<V>) parent;
    } else {
      throw new IllegalStateException(
          "The parent of this EnumerationLiteral is not an Enumeration");
    }
  }

  public void setEnumeration(@Nullable Enumeration<V> enumeration) {
    this.setParent(enumeration);
  }

  @Override
  public @Nullable Enumeration<V> getContainer() {
    return getEnumeration();
  }

  @Override
  public Concept<V> getClassifier() {
    return LionCore.getEnumerationLiteral(getLionWebVersion());
  }

  @Override
  public String getKey() {
    return this.getPropertyValue("key", String.class);
  }

  @Override
  public EnumerationLiteral<V> setKey(String key) {
    setPropertyValue("key", key);
    return this;
  }
}
