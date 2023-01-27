package org.lionweb.lioncore.java.metamodel;

import org.lionweb.lioncore.java.model.impl.M3Node;
import org.lionweb.lioncore.java.self.LionCore;
import org.lionweb.lioncore.java.utils.Naming;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
public class Metamodel extends M3Node implements NamespaceProvider {
    public Metamodel() {
    }

    public Metamodel(String qualifiedName) {
        this.setQualifiedName(qualifiedName);
    }

    private void setQualifiedName(String qualifiedName) {
        setPropertyValue("qualifiedName", qualifiedName);
    }

    @Override
    public String namespaceQualifier() {
        return getQualifiedName();
    }

    public @Nonnull List<Metamodel> dependsOn() {
        return this.getLinkMultipleValue("dependsOn");
    }
    public @Nonnull List<MetamodelElement> getElements() {
        return this.getLinkMultipleValue("elements");
    }

    public <T extends MetamodelElement> T addElement(@Nonnull T element) {
        this.addLinkMultipleValue("elements", element, true);
        element.setMetamodel(this);
        return element;
    }

    public @Nullable Concept getConceptByName(String name) {
        return getElements().stream().filter(element -> element instanceof Concept)
                .map(element -> (Concept)element)
                .filter(element -> element.getSimpleName().equals(name)).findFirst()
                .orElse(null);
    }

    public Concept requireConceptByName(String name) {
        Concept concept = getConceptByName(name);
        if (concept == null) {
            throw new IllegalArgumentException("Concept named " + name + " was not found");
        } else {
            return concept;
        }
    }

    public @Nullable ConceptInterface getConceptInterfaceByName(String name) {
        return getElements().stream().filter(element -> element instanceof ConceptInterface)
                .map(element -> (ConceptInterface)element)
                .filter(element -> element.getSimpleName().equals(name)).findFirst()
                .orElse(null);
    }

    public String getQualifiedName() {
        return (String) this.getPropertyValue("qualifiedName", String.class, null);
    }

    public @Nullable MetamodelElement getElementByName(String name) {
        return getElements().stream().filter(element -> element.getSimpleName().equals(name)).findFirst()
                .orElse(null);
    }

    public @Nullable PrimitiveType getPrimitiveTypeByName(String name) {
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

    @Override
    public Concept getConcept() {
        return LionCore.getMetamodel();
    }

}
