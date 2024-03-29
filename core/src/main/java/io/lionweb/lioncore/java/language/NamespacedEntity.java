package io.lionweb.lioncore.java.language;

/**
 * Something with a name and contained in a Namespace.
 *
 * <p>A Concept Invoice, contained in a Language com.foo.Accounting. Therefore, Invoice will have
 * the qualifiedName com.foo.Accounting.Invoice.
 */
public interface NamespacedEntity extends INamed {
  String getName();

  default String qualifiedName() {
    if (this.getContainer() == null) {
      throw new IllegalStateException("No container for " + this);
    }
    return this.getContainer().namespaceQualifier() + "." + this.getName();
  }

  NamespaceProvider getContainer();
}
