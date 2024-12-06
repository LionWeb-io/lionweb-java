package io.lionweb.lioncore.java.language;

import io.lionweb.lioncore.java.model.ReferenceValue;
import io.lionweb.lioncore.java.model.impl.M3Node;
import io.lionweb.lioncore.java.self.LionCore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Field of a StructuredDataType. */
public class Field extends M3Node<Field> implements NamespacedEntity, IKeyed<Field> {

  public Field() {}

  public Field(@Nullable String name) {
    setName(name);
  }

  public Field(@Nullable String name, @Nullable DataType<?> type) {
    setName(name);
    setType(type);
  }

  public Field(@Nullable String name, @Nullable DataType<?> type, @Nullable String id) {
    setName(name);
    setType(type);
    setID(id);
  }

  public Field(@Nonnull StructuredDataType structuredDataType, @Nullable String name) {
    structuredDataType.addField(this);
    setParent(structuredDataType);
    setName(name);
  }

  @Override
  public @Nullable String getName() {
    return getPropertyValue("name", String.class);
  }

  public void setName(@Nullable String name) {
    this.setPropertyValue("name", name);
  }

  /** The container is always the parent. It is just casted for convenience. */
  @Override
  public @Nullable NamespaceProvider getContainer() {
    if (this.getParent() == null) {
      return null;
    }
    if (this.getParent() instanceof NamespaceProvider) {
      return (NamespaceProvider) this.getParent();
    } else {
      throw new IllegalStateException("The parent is not a NamespaceProvider");
    }
  }

  @Override
  public @Nonnull Concept getClassifier() {
    return LionCore.getField(getLionWebVersion());
  }

  public @Nullable DataType<?> getType() {
    return this.getReferenceSingleValue("type");
  }

  public void setType(@Nullable DataType<?> type) {
    if (type == null) {
      this.setReferenceSingleValue("type", null);
    } else {
      this.setReferenceSingleValue("type", new ReferenceValue(type, type.getName()));
    }
  }

  @Override
  public @Nullable String getKey() {
    return this.getPropertyValue("key", String.class);
  }

  @Override
  public @Nonnull Field setKey(@Nullable String key) {
    setPropertyValue("key", key);
    return this;
  }
}
