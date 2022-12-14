package org.lionweb.lioncore.java.metamodel;

import org.lionweb.lioncore.java.Experimental;

import javax.annotation.Nullable;

/**
 * This represents a relation between an {@link FeaturesContainer} and referred {@link FeaturesContainer}.
 *
 * A VariableReference may have a Reference to a VariableDeclaration.
 *
 * @see org.eclipse.emf.ecore.EReference Ecore equivalent <i>EReference</i> (with the <code>containment</code> flag set to <code>false</code>)
 * @see <a href="https://www.jetbrains.com/help/mps/structure.html#references">MPS equivalent <i>Reference</i> in documentation</a>
 * @see <a href="http://127.0.0.1:63320/node?ref=r%3A00000000-0000-4000-0000-011c89590292%28jetbrains.mps.lang.structure.structure%29%2F1071489288298">MPS equivalent <i>LinkDeclaration</i> in local MPS (with <code>metaClass</code> having value <code>reference</code>)</a>
 * @see org.jetbrains.mps.openapi.language.SReferenceLink MPS equivalent <i>SReferenceLink</i> in SModel
 */
public class Reference extends Link {

    public static Reference createOptional(@Nullable String simpleName, @Nullable FeaturesContainer container) {
        Reference reference = new Reference(simpleName, container);
        reference.setOptional(true);
        reference.setMultiple(false);
        return reference;
    }

    public static Reference createRequired(@Nullable String simpleName, @Nullable FeaturesContainer container) {
        Reference reference = new Reference(simpleName, container);
        reference.setOptional(false);
        reference.setMultiple(false);
        return reference;
    }

    public static Reference createMultiple(@Nullable String simpleName, @Nullable FeaturesContainer container) {
        Reference reference = new Reference(simpleName, container);
        reference.setOptional(true);
        reference.setMultiple(true);
        return reference;
    }

    public static Reference createMultipleAndRequired(@Nullable String simpleName, @Nullable FeaturesContainer container) {
        Reference reference = new Reference(simpleName, container);
        reference.setOptional(false);
        reference.setMultiple(true);
        return reference;
    }

    @Experimental
    private Reference specialized;

    public Reference() {
        super();
    }

    public Reference(@Nullable String simpleName, @Nullable FeaturesContainer container) {
        // TODO verify that the container is also a NamespaceProvider
        super(simpleName, container);
    }

    public @Nullable Reference getSpecialized() {
        return specialized;
    }

    public void setSpecialized(@Nullable Reference specialized) {
        // TODO check which limitations there are: should have the same name? Should it belong
        //      to an ancestor of the FeaturesContainer holding this Containment?
        this.specialized = specialized;
    }

    @Override
    public String toString() {
        return "Reference{" +
                "simpleName=" + getSimpleName() + ", " +
                "type=" + getType() +
                '}';
    }

}
