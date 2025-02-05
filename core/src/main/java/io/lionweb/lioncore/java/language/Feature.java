package io.lionweb.lioncore.java.language;

import io.lionweb.lioncore.java.LionWebVersion;
import io.lionweb.lioncore.java.model.impl.M3Node;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A Feature represents a characteristic or some form of data associated with a particular concept.
 *
 * <p>For example, an Invoice can have an associated date, a number, a connection with a customer,
 * and it can contain InvoiceLines. All of this information is represented by features.
 *
 * @see org.eclipse.emf.ecore.EStructuralFeature Ecore equivalent <i>EStructuralFeature</i>
 * @see <a href="https://www.jetbrains.com/help/mps/structure.html#conceptmembers">MPS equivalent
 *     <i>Concept members</i> in documentation</a>
 * @see org.jetbrains.mps.openapi.language.SConceptFeature MPS equivalent <i>SConceptFeature</i> in
 *     SModel
 */
public abstract class Feature<T extends M3Node> extends M3Node<T>
    implements NamespacedEntity, IKeyed<T> {

  public Feature() {
    super();
    setOptional(false);
  }

  public Feature(@Nonnull LionWebVersion lionWebVersion) {
    super(lionWebVersion);
    setOptional(false);
  }

  public Feature(
      @Nonnull LionWebVersion lionWebVersion, @Nullable String name, @Nonnull String id) {
    this(lionWebVersion, name, null, id);
    setOptional(false);
  }

  public Feature(@Nullable String name, @Nonnull String id) {
    this(name, null, id);
    setOptional(false);
  }

  public Feature(
      @Nonnull LionWebVersion lionWebVersion,
      @Nullable String name,
      @Nullable Classifier container,
      @Nonnull String id) {
    super(lionWebVersion);
    setOptional(false);
    Objects.requireNonNull(id, "id should not be null");
    this.setID(id);
    // TODO enforce uniqueness of the name within the FeauturesContainer
    setName(name);
    setParent(container);
  }

  public Feature(@Nullable String name, @Nullable Classifier<?> container, @Nonnull String id) {
    super(
        (container != null && container.getLionWebVersion() != null)
            ? container.getLionWebVersion()
            : LionWebVersion.currentVersion);
    setOptional(false);
    Objects.requireNonNull(id, "id should not be null");
    this.setID(id);
    // TODO enforce uniqueness of the name within the FeauturesContainer
    setName(name);
    setParent(container);
  }

  public Feature(
      @Nonnull LionWebVersion lionWebVersion,
      @Nullable String name,
      @Nullable Classifier container) {
    super(lionWebVersion);
    setOptional(false);
    // TODO enforce uniqueness of the name within the FeauturesContainer
    setName(name);
    setParent(container);
  }

  public Feature(@Nullable String name, @Nullable Classifier container) {
    super(
        (container != null && container.getLionWebVersion() != null)
            ? container.getLionWebVersion()
            : LionWebVersion.currentVersion);
    setOptional(false);
    // TODO enforce uniqueness of the name within the FeauturesContainer
    setName(name);
    setParent(container);
  }

  public boolean isOptional() {
    return this.getPropertyValue("optional", Boolean.class, false);
  }

  public boolean isRequired() {
    return !isOptional();
  }

  public T setOptional(boolean optional) {
    setPropertyValue("optional", optional);
    return (T) this;
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
  public String getKey() {
    return this.getPropertyValue("key", String.class);
  }

  @Override
  public T setKey(String key) {
    setPropertyValue("key", key);
    return (T) this;
  }

  public Language getDeclaringLanguage() {
    return (Language) ((Classifier<?>) this.getContainer()).getContainer();
  }
}
