package org.lionweb.lioncore.java.metamodel;

/**
 * Something which can act as the namespace for contained named things.
 *
 * <p>A Metamodel com.foo.Accounting can be the NamespaceProvider for a Concept Invoice, which will
 * therefore have the qualifiedName com.foo.Accounting.Invoice.
 */
public interface NamespaceProvider {
  String namespaceQualifier();
}
