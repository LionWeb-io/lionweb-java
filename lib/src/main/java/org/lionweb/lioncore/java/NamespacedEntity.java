package org.lionweb.lioncore.java;

/**
 * Something with a name and contained in a Namespace.
 *
 * A Concept Invoice, contained in a Metamodel com.foo.Accounting.
 * Therefore, Invoice will have the qualifiedName com.foo.Accounting.Invoice.
 */
public interface NamespacedEntity {
    String getSimpleName();
    String qualifiedName();
    NamespaceProvider getContainer();
}
