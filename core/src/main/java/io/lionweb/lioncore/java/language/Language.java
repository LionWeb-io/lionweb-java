package io.lionweb.lioncore.java.language;

import io.lionweb.lioncore.java.model.ReferenceValue;
import io.lionweb.lioncore.java.model.impl.M3Node;
import io.lionweb.lioncore.java.self.LionCore;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A Metamodel will provide the {@link Concept}s necessary to describe data in a particular domain
 * together with supporting elements necessary for the definition of those Concepts.
 *
 * <p>It also represents the namespace within which Concepts and other supporting elements are
 * organized. For example, a Metamodel for accounting could collect several Concepts such as
 * Invoice, Customer, InvoiceLine, Product. It could also contain related elements necessary for the
 * definitions of the concepts. For example, a {@link DataType} named Currency.
 *
 * @see org.eclipse.emf.ecore.EPackage Ecore equivalent <i>EPackage</i>
 * @see <a href="https://www.jetbrains.com/help/mps/structure.html">MPS equivalent <i>Language's
 *     structure aspect</i> in documentation</a>
 */
public class Language extends M3Node<Language> implements NamespaceProvider, HasKey<Language> {
  public Language() {}

  public Language(@Nonnull String name) {
    Objects.requireNonNull(name, "name should not be null");
    this.setName(name);
  }

  public Language setName(String name) {
    setPropertyValue("name", name);
    return this;
  }

  public Language setVersion(@Nullable String version) {
    setPropertyValue("version", version);
    return this;
  }

  @Override
  public Language setKey(String key) {
    setPropertyValue("key", key);
    return this;
  }

  @Override
  public String namespaceQualifier() {
    return getName();
  }

  public @Nonnull List<Language> dependsOn() {
    return this.getReferenceMultipleValue("dependsOn");
  }

  public @Nonnull List<LanguageElement> getElements() {
    return this.getContainmentMultipleValue("elements");
  }

  public Language addDependency(@Nonnull Language dependency) {
    Objects.requireNonNull(dependency, "dependency should not be null");
    this.addReferenceMultipleValue(
        "dependsOn", new ReferenceValue(dependency, dependency.getName()));
    return dependency;
  }

  public <T extends LanguageElement> T addElement(@Nonnull T element) {
    Objects.requireNonNull(element, "element should not be null");
    this.addContainmentMultipleValue("elements", element);
    element.setParent(this);
    return element;
  }

  public @Nullable Concept getConceptByName(String name) {
    return getElements().stream()
        .filter(element -> element instanceof Concept)
        .map(element -> (Concept) element)
        .filter(element -> element.getName().equals(name))
        .findFirst()
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
    return getElements().stream()
        .filter(element -> element instanceof ConceptInterface)
        .map(element -> (ConceptInterface) element)
        .filter(element -> element.getName().equals(name))
        .findFirst()
        .orElse(null);
  }

  public String getName() {
    return this.getPropertyValue("name", String.class);
  }

  @Override
  public String getKey() {
    return this.getPropertyValue("key", String.class);
  }

  @Nullable
  public String getVersion() {
    return this.getPropertyValue("version", String.class);
  }

  public @Nullable LanguageElement getElementByName(String name) {
    return getElements().stream()
        .filter(element -> element.getName().equals(name))
        .findFirst()
        .orElse(null);
  }

  public @Nullable PrimitiveType getPrimitiveTypeByName(String name) {
    LanguageElement element = this.getElementByName(name);
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
    return LionCore.getLanguage();
  }

  @Override
  public String toString() {
    return "Metamodel(" + this.getName() + ")";
  }
}
