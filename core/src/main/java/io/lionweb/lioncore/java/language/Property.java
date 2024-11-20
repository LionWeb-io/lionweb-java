package io.lionweb.lioncore.java.language;

import io.lionweb.lioncore.java.versions.LionWebVersion;
import io.lionweb.lioncore.java.model.ReferenceValue;
import io.lionweb.lioncore.java.self.LionCore;
import io.lionweb.lioncore.java.versions.LionWebVersionToken;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This indicates a simple value associated to an entity.
 *
 * <p>For example, an Invoice could have a date or an amount.
 *
 * @see org.eclipse.emf.ecore.EAttribute Ecore equivalent <i>EAttribute</i>
 * @see <a href="https://www.jetbrains.com/help/mps/structure.html#properties">MPS equivalent
 *     <i>Property</i> in documentation</a>
 * @see <a
 *     href="http://127.0.0.1:63320/node?ref=r%3A00000000-0000-4000-0000-011c89590292%28jetbrains.mps.lang.structure.structure%29%2F1071489288299">MPS
 *     equivalent <i>PropertyDeclaration</i> in local MPS</a>
 * @see org.jetbrains.mps.openapi.language.SProperty MPS equivalent <i>SProperty</i> in SModel
 */
public class Property<V extends LionWebVersionToken> extends Feature<Property<V>, V> {

  public static <V extends LionWebVersionToken> Property<V> createOptional(
      @Nonnull LionWebVersion lionWebVersion, @Nullable String name, @Nullable DataType<?, V> type) {
    Objects.requireNonNull(lionWebVersion, "lionWebVersion should not be null");
    Property<V> property = new Property<V>(lionWebVersion, name, null);
    property.setOptional(true);
    property.setType(type);
    return property;
  }

  public static <V extends LionWebVersionToken> Property<V> createOptional(@Nullable String name, @Nullable DataType<?, V> type) {
    Property<V> property = new Property(name, null);
    property.setOptional(true);
    property.setType(type);
    return property;
  }

  public static <V extends LionWebVersionToken> Property<V> createRequired(
      @Nonnull LionWebVersion lionWebVersion, @Nullable String name, @Nullable DataType<?, V> type) {
    Objects.requireNonNull(lionWebVersion, "lionWebVersion should not be null");
    Property<V> property = new Property<V>(lionWebVersion, name, null);
    property.setOptional(false);
    property.setType(type);
    return property;
  }

  public static <V extends LionWebVersionToken> Property<V> createRequired(@Nullable String name, @Nullable DataType<?, V> type) {
    Property<V> property = new Property<V>(name, null);
    property.setOptional(false);
    property.setType(type);
    return property;
  }

  public static <V extends LionWebVersionToken> Property<V> createOptional(
      @Nullable String name, @Nullable DataType<?, V> type, @Nonnull String id) {
    Objects.requireNonNull(id, "id should not be null");
    Property<V> property = new Property(name, null, id);
    property.setOptional(true);
    property.setType(type);
    return property;
  }

  public static <V extends LionWebVersionToken> Property<V> createRequired(
      @Nonnull LionWebVersion lionWebVersion,
      @Nullable String name,
      @Nullable DataType<?, V> type,
      @Nonnull String id) {
    Objects.requireNonNull(lionWebVersion, "lionWebVersion should not be null");
    Objects.requireNonNull(id, "id should not be null");
    Property<V> property = new Property<V>(lionWebVersion, name, null, id);
    property.setOptional(false);
    property.setType(type);
    return property;
  }

  public static <V extends LionWebVersionToken> Property<V> createRequired(
      @Nullable String name, @Nullable DataType<?, V> type, @Nonnull String id) {
    Objects.requireNonNull(id, "id should not be null");
    Property<V> property = new Property<V>(name, null, id);
    property.setOptional(false);
    property.setType(type);
    return property;
  }

  public Property() {
    super();
  }

  public Property(
      @Nonnull LionWebVersion lionWebVersion,
      @Nullable String name,
      @Nullable Classifier<?, V> container,
      @Nonnull String id) {
    super(lionWebVersion, name, container, id);
  }

  public Property(@Nullable String name, @Nullable Classifier<?, V> container, @Nonnull String id) {
    super(name, container, id);
  }

  public Property(
      @Nonnull LionWebVersion lionWebVersion,
      @Nullable String name,
      @Nullable Classifier<?, V> container) {
    super(lionWebVersion, name, container);
  }

  public Property(@Nullable String name, @Nullable Classifier<?, V> container) {
    super(name, container);
  }

  public @Nullable DataType<?, V> getType() {
    return getReferenceSingleValue("type");
  }

  public Property<V> setType(@Nullable DataType<?, V> type) {
    if (type == null) {
      setReferenceSingleValue("type", null);
    } else {
      setReferenceSingleValue("type", new ReferenceValue(type, type.getName()));
    }
    return this;
  }

  @Override
  public String toString() {
    return super.toString()
        + "{"
        + "qualifiedName="
        + DebugUtils.qualifiedName(this)
        + ", "
        + "type="
        + getType()
        + '}';
  }

  @Override
  public Concept<V> getClassifier() {
    return LionCore.getProperty(getLionWebVersion());
  }
}
