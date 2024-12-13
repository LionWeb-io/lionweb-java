package io.lionweb.lioncore.java.language;

import io.lionweb.lioncore.java.LionWebVersion;
import io.lionweb.lioncore.java.model.ReferenceValue;
import io.lionweb.lioncore.java.model.impl.M3Node;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represent a connection to an {@link Classifier}.
 *
 * <p>An Invoice can be connected to its InvoiceLines and to a Customer.
 *
 * @see org.eclipse.emf.ecore.EReference Ecore equivalent <i>EReference</i>
 * @see <a
 *     href="http://127.0.0.1:63320/node?ref=r%3A00000000-0000-4000-0000-011c89590292%28jetbrains.mps.lang.structure.structure%29%2F1071489288298">MPS
 *     equivalent <i>LinkDeclaration</i> in local MPS</a>
 * @see org.jetbrains.mps.openapi.language.SAbstractLink MPS equivalent <i>SAbstractLink</i> in
 *     SModel
 */
public abstract class Link<T extends M3Node> extends Feature<T> {
  public Link() {
    super();
    setMultiple(false);
  }

  public Link(@Nonnull LionWebVersion lionWebVersion) {
    super(lionWebVersion);
    setMultiple(false);
  }

  public Link(@Nonnull LionWebVersion lionWebVersion, @Nullable String name, @Nonnull String id) {
    super(lionWebVersion, name, id);
    setMultiple(false);
  }

  public Link(@Nullable String name, @Nonnull String id) {
    super(name, id);
    setMultiple(false);
  }

  public Link(
      @Nonnull LionWebVersion lionWebVersion,
      @Nullable String name,
      @Nullable Classifier container) {
    super(lionWebVersion, name, container);
    setMultiple(false);
  }

  public Link(@Nullable String name, @Nullable Classifier container) {
    super(name, container);
    setMultiple(false);
  }

  public boolean isMultiple() {
    return getPropertyValue("multiple", Boolean.class, false);
  }

  public boolean isSingle() {
    return !isMultiple();
  }

  public T setMultiple(boolean multiple) {
    this.setPropertyValue("multiple", multiple);
    return (T) this;
  }

  public @Nullable Classifier getType() {
    return getReferenceSingleValue("type");
  }

  public T setType(@Nullable Classifier type) {
    if (type == null) {
      this.setReferenceSingleValue("type", null);
    } else {
      this.setReferenceSingleValue("type", new ReferenceValue(type, type.getName()));
    }
    return (T) this;
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
}
