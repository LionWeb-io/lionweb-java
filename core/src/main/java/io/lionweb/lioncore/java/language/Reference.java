package io.lionweb.lioncore.java.language;

import io.lionweb.lioncore.java.self.LionCore;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This represents a relation between an {@link Classifier} and referred {@link Classifier}.
 *
 * <p>A VariableReference may have a Reference to a VariableDeclaration.
 *
 * @see org.eclipse.emf.ecore.EReference Ecore equivalent <i>EReference</i> (with the <code>
 *     containment</code> flag set to <code>false</code>)
 * @see <a href="https://www.jetbrains.com/help/mps/structure.html#references">MPS equivalent
 *     <i>Reference</i> in documentation</a>
 * @see <a
 *     href="http://127.0.0.1:63320/node?ref=r%3A00000000-0000-4000-0000-011c89590292%28jetbrains.mps.lang.structure.structure%29%2F1071489288298">MPS
 *     equivalent <i>LinkDeclaration</i> in local MPS (with <code>metaClass</code> having value
 *     <code>reference</code>)</a>
 * @see org.jetbrains.mps.openapi.language.SReferenceLink MPS equivalent <i>SReferenceLink</i> in
 *     SModel
 */
public class Reference extends Link<Reference> {

  public static Reference createOptional(@Nullable String name, @Nullable Classifier type) {
    Reference reference = new Reference(name);
    reference.setOptional(true);
    reference.setMultiple(false);
    reference.setType(type);
    return reference;
  }

  public static Reference createOptional(
      @Nullable String name, @Nullable Classifier type, @Nonnull String id) {
    Objects.requireNonNull(id, "id should not be null");
    Reference reference = new Reference(name, id);
    reference.setOptional(true);
    reference.setMultiple(false);
    reference.setType(type);
    return reference;
  }

  public static Reference createRequired(@Nullable String name, @Nullable Classifier type) {
    Reference reference = new Reference(name);
    reference.setOptional(false);
    reference.setMultiple(false);
    reference.setType(type);
    return reference;
  }

  public static Reference createRequired(
      @Nullable String name, @Nullable Classifier type, @Nonnull String id) {
    Objects.requireNonNull(id, "id should not be null");
    Reference reference = new Reference(name, id);
    reference.setOptional(false);
    reference.setMultiple(false);
    reference.setType(type);
    return reference;
  }

  public static Reference createMultiple(@Nullable String name, @Nullable Classifier type) {
    Reference reference = new Reference(name);
    reference.setOptional(true);
    reference.setMultiple(true);
    reference.setType(type);
    return reference;
  }

  public static Reference createMultiple(
      @Nullable String name, @Nullable Classifier type, @Nonnull String id) {
    Objects.requireNonNull(id, "id should not be null");
    Reference reference = new Reference(name, id);
    reference.setOptional(true);
    reference.setMultiple(true);
    reference.setType(type);
    return reference;
  }

  public static Reference createMultipleAndRequired(
      @Nullable String name, @Nullable Classifier type) {
    Reference reference = new Reference(name);
    reference.setOptional(false);
    reference.setMultiple(true);
    reference.setType(type);
    return reference;
  }

  public Reference() {
    super();
  }

  public Reference(@Nullable String name, @Nullable Classifier container) {
    // TODO verify that the container is also a NamespaceProvider
    super(name, container);
  }

  public Reference(@Nullable String name) {
    super(name, (Classifier) null);
  }

  public Reference(@Nullable String name, @Nonnull String id) {
    super(name, id);
  }

  @Override
  public Concept getClassifier() {
    return LionCore.getReference();
  }
}
