package io.lionweb.language;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Something with a name and contained in a Namespace.
 *
 * <p>A Concept Invoice, contained in a Language com.foo.Accounting. Therefore, Invoice will have
 * the qualifiedName com.foo.Accounting.Invoice.
 */
public interface NamespacedEntity extends INamed {
  @Nullable
  String getName();

  default @Nonnull String qualifiedName() {
    if (this.getContainer() == null) {
      throw new IllegalStateException("No container for " + this);
    }
    return this.getContainer().namespaceQualifier() + "." + this.getName();
  }

  @Nullable
  NamespaceProvider getContainer();
}
