package io.lionweb.lioncore.java.language;

import io.lionweb.lioncore.java.model.Node;
import io.lionweb.lioncore.java.model.impl.M3Node;
import io.lionweb.lioncore.java.self.LionCore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EnumerationLiteral extends M3Node<EnumerationLiteral>
    implements NamespacedEntity, IKeyed<EnumerationLiteral> {

  public EnumerationLiteral() {}

  public EnumerationLiteral(@Nullable String name) {
    setName(name);
  }

  public EnumerationLiteral(@Nonnull Enumeration enumeration, @Nullable String name) {
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

  public @Nullable Enumeration getEnumeration() {
    Node parent = getParent();
    if (parent == null) {
      return null;
    } else if (parent instanceof Enumeration) {
      return (Enumeration) parent;
    } else {
      throw new IllegalStateException(
          "The parent of this EnumerationLiteral is not an Enumeration");
    }
  }

  public void setEnumeration(@Nullable Enumeration enumeration) {
    this.setParent(enumeration);
  }

  @Override
  public @Nullable Enumeration getContainer() {
    return getEnumeration();
  }

  @Override
  public Concept getClassifier() {
    return LionCore.getEnumerationLiteral();
  }

  @Override
  public String getKey() {
    return this.getPropertyValue("key", String.class);
  }

  @Override
  public EnumerationLiteral setKey(String key) {
    setPropertyValue("key", key);
    return this;
  }
}
