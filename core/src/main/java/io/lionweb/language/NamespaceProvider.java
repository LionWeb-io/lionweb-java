package io.lionweb.language;

/**
 * Something which can act as the namespace for contained named things.
 *
 * <p>A Language com.foo.Accounting can be the NamespaceProvider for a Concept Invoice, which will
 * therefore have the qualifiedName com.foo.Accounting.Invoice.
 */
public interface NamespaceProvider {
  String namespaceQualifier();
}
