package io.lionweb.language;

import io.lionweb.LionWebVersion;
import io.lionweb.lioncore.LionCore;
import io.lionweb.model.ClassifierInstanceUtils;
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
public class Property extends Feature<Property> {

  public static Property createOptional(
      @Nonnull LionWebVersion lionWebVersion, @Nullable String name, @Nullable DataType<?> type) {
    Objects.requireNonNull(lionWebVersion, "lionWebVersion should not be null");
    Property property = new Property(lionWebVersion, name);
    property.setOptional(true);
    property.setType(type);
    return property;
  }

  public static Property createOptional(@Nullable String name, @Nullable DataType<?> type) {
    Property property = new Property();
    property.setName(name);
    property.setType(type);
    property.setOptional(true);
    property.setType(type);
    return property;
  }

  public static Property createRequired(
      @Nonnull LionWebVersion lionWebVersion, @Nullable String name, @Nullable DataType<?> type) {
    Objects.requireNonNull(lionWebVersion, "lionWebVersion should not be null");
    Property property = new Property(lionWebVersion, name);
    property.setOptional(false);
    property.setType(type);
    return property;
  }

  public static Property createRequired(@Nullable String name, @Nullable DataType<?> type) {
    Property property = new Property();
    property.setName(name);
    property.setOptional(false);
    property.setType(type);
    return property;
  }

  public static Property createOptional(
      @Nullable String name, @Nullable DataType<?> type, @Nonnull String id) {
    Objects.requireNonNull(id, "id should not be null");
    Property property = new Property(name, null, id);
    property.setOptional(true);
    property.setType(type);
    return property;
  }

  public static Property createRequired(
      @Nonnull LionWebVersion lionWebVersion,
      @Nullable String name,
      @Nullable DataType type,
      @Nonnull String id) {
    Objects.requireNonNull(lionWebVersion, "lionWebVersion should not be null");
    Objects.requireNonNull(id, "id should not be null");
    Property property = new Property(lionWebVersion, name, null, id);
    property.setOptional(false);
    property.setType(type);
    return property;
  }

  public static Property createRequired(
      @Nullable String name, @Nullable DataType type, @Nonnull String id) {
    Objects.requireNonNull(id, "id should not be null");
    Property property = new Property(name, null, id);
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
      @Nullable Classifier<?> container,
      @Nonnull String id) {
    super(lionWebVersion, name, container, id);
  }

  public Property(@Nullable String name, @Nullable Classifier<?> container, @Nonnull String id) {
    super(name, container, id);
  }

  public Property(@Nonnull LionWebVersion lionWebVersion) {
    super(lionWebVersion);
  }

  public Property(@Nonnull LionWebVersion lionWebVersion, @Nullable String name) {
    super(lionWebVersion);
    setName(name);
  }

  public @Nullable DataType<?> getType() {
    return getReferenceSingleValue("type");
  }

  public Property setType(@Nullable DataType<?> type) {
    if (type == null) {
      setReferenceSingleValue("type", null);
    } else {
      setReferenceSingleValue("type", ClassifierInstanceUtils.referenceTo(type));
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
  public Concept getClassifier() {
    return LionCore.getProperty(getLionWebVersion());
  }
}
