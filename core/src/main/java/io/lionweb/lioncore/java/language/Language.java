package io.lionweb.lioncore.java.language;

import io.lionweb.lioncore.java.versions.LionWebVersion;
import io.lionweb.lioncore.java.model.ReferenceValue;
import io.lionweb.lioncore.java.model.impl.M3Node;
import io.lionweb.lioncore.java.self.LionCore;
import io.lionweb.lioncore.java.utils.LanguageValidator;
import io.lionweb.lioncore.java.utils.ValidationResult;
import io.lionweb.lioncore.java.versions.LionWebVersionToken;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A Language will provide the {@link Concept}s necessary to describe data in a particular domain
 * together with supporting elements necessary for the definition of those Concepts.
 *
 * <p>It also represents the namespace within which Concepts and other supporting elements are
 * organized. For example, a Language for accounting could collect several Concepts such as Invoice,
 * Customer, InvoiceLine, Product. It could also contain related elements necessary for the
 * definitions of the concepts. For example, a {@link DataType} named Currency.
 *
 * @see org.eclipse.emf.ecore.EPackage Ecore equivalent <i>EPackage</i>
 * @see <a href="https://www.jetbrains.com/help/mps/structure.html">MPS equivalent <i>Language's
 *     structure aspect</i> in documentation</a>
 */
public class Language<V extends LionWebVersionToken> extends M3Node<Language<V>, V> implements NamespaceProvider, IKeyed<Language<V>> {
  public Language(@Nonnull LionWebVersion lionWebVersion) {
    super(lionWebVersion);
  }

  public Language() {
    this(LionWebVersion.currentVersion);
  }

  public Language(@Nonnull LionWebVersion lionWebVersion, @Nonnull String name) {
    this(lionWebVersion);
    Objects.requireNonNull(name, "name should not be null");
    this.setName(name);
  }

  public Language(@Nonnull String name) {
    this(LionWebVersion.currentVersion);
    Objects.requireNonNull(name, "name should not be null");
    this.setName(name);
  }

  public Language(@Nonnull String name, @Nullable String id, @Nullable String key) {
    this(name);
    this.setID(id);
    this.setKey(key);
  }

  public Language(
      @Nonnull String name, @Nullable String id, @Nullable String key, @Nullable String version) {
    this(name, id, key);
    setVersion(version);
  }

  public Language(@Nonnull String name, @Nullable String id) {
    this(name);
    this.setID(id);
  }

  public Language<V> setName(String name) {
    setPropertyValue("name", name);
    return this;
  }

  public Language<V> setVersion(@Nullable String version) {
    setPropertyValue("version", version);
    return this;
  }

  @Override
  public Language<V> setKey(String key) {
    setPropertyValue("key", key);
    return this;
  }

  @Override
  public String namespaceQualifier() {
    return getName();
  }

  public @Nonnull List<Language<V>> dependsOn() {
    return this.getReferenceMultipleValue("dependsOn");
  }

  public @Nonnull List<LanguageEntity<?, V>> getElements() {
    return this.getContainmentMultipleValue("entities");
  }

  public Language<V> addDependency(@Nonnull Language<V> dependency) {
    Objects.requireNonNull(dependency, "dependency should not be null");
    this.addReferenceMultipleValue(
        "dependsOn", new ReferenceValue(dependency, dependency.getName()));
    return dependency;
  }

  public <T extends LanguageEntity<?, V>> T addElement(@Nonnull T element) {
    Objects.requireNonNull(element, "element should not be null");
    this.addContainmentMultipleValue("entities", element);
    element.setParent(this);
    return element;
  }

  public @Nullable Concept<V> getConceptByName(String name) {
    return getElements().stream()
        .filter(element -> element instanceof Concept)
        .map(element -> (Concept) element)
        .filter(element -> element.getName().equals(name))
        .findFirst()
        .orElse(null);
  }

  public @Nullable Enumeration<V> getEnumerationByName(String name) {
    return getElements().stream()
        .filter(element -> element instanceof Enumeration)
        .map(element -> (Enumeration<V>) element)
        .filter(element -> element.getName().equals(name))
        .findFirst()
        .orElse(null);
  }

  public Concept<V> requireConceptByName(String name) {
    Concept<V> concept = getConceptByName(name);
    if (concept == null) {
      throw new IllegalArgumentException("Concept named " + name + " was not found");
    } else {
      return concept;
    }
  }

  public @Nullable Interface<V> getInterfaceByName(String name) {
    return getElements().stream()
        .filter(element -> element instanceof Interface)
        .map(element -> (Interface<V>) element)
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

  public @Nullable LanguageEntity<?, V> getElementByName(String name) {
    return getElements().stream()
        .filter(element -> element.getName().equals(name))
        .findFirst()
        .orElse(null);
  }

  public @Nullable PrimitiveType<V> getPrimitiveTypeByName(String name) {
    LanguageEntity element = this.getElementByName(name);
    if (element == null) {
      return null;
    }
    if (element instanceof PrimitiveType) {
      return (PrimitiveType<V>) element;
    } else {
      throw new RuntimeException("Element " + name + " is not a PrimitiveType");
    }
  }

  @Override
  public Concept<V> getClassifier() {
    return LionCore.getLanguage(getLionWebVersion());
  }

  @Override
  public String toString() {
    return super.toString() + "{" + "name=" + getName() + "}";
  }

  public List<PrimitiveType<V>> getPrimitiveTypes() {
    return this.getElements().stream()
        .filter(e -> e instanceof PrimitiveType)
        .map(e -> (PrimitiveType<V>) e)
        .collect(Collectors.toList());
  }

  public boolean isValid() {
    return new LanguageValidator().isValid(this);
  }

  public ValidationResult validate() {
    return new LanguageValidator().validate(this);
  }
}
