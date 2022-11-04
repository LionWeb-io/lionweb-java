package org.lionweb.lioncore.java.metamodel;

import org.lionweb.lioncore.java.utils.Naming;

import java.util.LinkedList;
import java.util.List;

/**
 * A Metamodel will provide the {@link Concept}s necessary to describe data in a particular domain together with supporting
 * elements necessary for the definition of those Concepts.
 *
 * It also represents the namespace within which Concepts and other supporting elements are organized.
 * For example, a Metamodel for accounting could collect several Concepts such as Invoice, Customer, InvoiceLine,
 * Product. It could also contain related elements necessary for the definitions of the concepts. For example, a
 * {@link DataType} named Currency.
 *
 * @see org.eclipse.emf.ecore.EPackage Ecore equivalent <i>EPackage</i>
 * @see <a href="https://www.jetbrains.com/help/mps/structure.html">MPS equivalent <i>Language's structure aspect</i> in documentation</a>
 */
public class Metamodel implements NamespaceProvider {
    private String qualifiedName;
    private List<Metamodel> dependsOn = new LinkedList<>();
    private List<MetamodelElement> elements = new LinkedList<>();

    public Metamodel(String qualifiedName) {
        Naming.validateQualifiedName(qualifiedName);
        this.qualifiedName = qualifiedName;
    }

    @Override
    public String namespaceQualifier() {
        return qualifiedName;
    }

    public List<Metamodel> dependsOn() {
        return this.dependsOn;
    }
    public List<MetamodelElement> getElements() {
        return this.elements;
    }

    public String getQualifiedName() {
        return this.qualifiedName;
    }

    public MetamodelElement getElementByName(String name) {
        return getElements().stream().filter(element -> element.getSimpleName().equals(name)).findFirst()
                .orElse(null);
    }

    public PrimitiveType getPrimitiveTypeByName(String name) {
        MetamodelElement element = this.getElementByName(name);
        if (element == null) {
            return null;
        }
        if (element instanceof PrimitiveType) {
            return (PrimitiveType) element;
        } else {
            throw new RuntimeException("Element " + name + " is not a PrimitiveType");
        }
    }
}
