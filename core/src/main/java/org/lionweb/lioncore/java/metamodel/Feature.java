package org.lionweb.lioncore.java.metamodel;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.lionweb.lioncore.java.Experimental;
import org.lionweb.lioncore.java.model.impl.M3Node;

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
    implements NamespacedEntity, HasKey<T> {

  public Feature() {
    setDerived(false);
  }

  public Feature(@Nullable String name, @Nonnull String id) {
    this(name, null, id);
    setDerived(false);
  }

  public Feature(
      @Nullable String name, @Nullable FeaturesContainer container, @Nonnull String id) {
    setDerived(false);
    Objects.requireNonNull(id, "id should not be null");
    this.setID(id);
    // TODO verify that the container is also a NamespaceProvider
    // TODO enforce uniqueness of the name within the FeauturesContainer
    setname(name);
    setParent(container);
  }

  public Feature(@Nullable String name, @Nullable FeaturesContainer container) {
    setDerived(false);
    // TODO verify that the container is also a NamespaceProvider
    // TODO enforce uniqueness of the name within the FeauturesContainer
    setname(name);
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

  @Experimental
  public boolean isDerived() {
    return getPropertyValue("derived", Boolean.class);
  }

  @Experimental
  public T setDerived(boolean derived) {
    setPropertyValue("derived", derived);
    return (T) this;
  }

  @Override
  public @Nullable String getName() {
    return getPropertyValue("name", String.class);
  }

  public void setname(@Nullable String name) {
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

  protected Object getDerivedValue(Property property) {
    if (property.getKey().equals(this.getConcept().getPropertyByName("qualifiedName").getKey())) {
      return qualifiedName();
    }
    return null;
  }
}
