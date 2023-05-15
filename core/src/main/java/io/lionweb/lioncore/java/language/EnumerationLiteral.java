package io.lionweb.lioncore.java.language;

import io.lionweb.lioncore.java.model.ReferenceValue;
import io.lionweb.lioncore.java.model.impl.M3Node;
import io.lionweb.lioncore.java.self.LionCore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EnumerationLiteral extends M3Node<EnumerationLiteral>
    implements NamespacedEntity, HasKey<EnumerationLiteral> {

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
    return getReferenceSingleValue("enumeration");
  }

  public void setEnumeration(@Nullable Enumeration enumeration) {
    if (enumeration == null) {
      this.setReferenceSingleValue("enumeration", null);
    } else {
      this.setReferenceSingleValue(
          "enumeration", new ReferenceValue(enumeration, enumeration.getName()));
    }
  }

  @Override
  public @Nullable Enumeration getContainer() {
    return this.getReferenceSingleValue("enumeration");
  }

  @Override
  public Concept getConcept() {
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
