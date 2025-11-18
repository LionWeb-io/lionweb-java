package io.lionweb.language;

import io.lionweb.LionWebVersion;
import io.lionweb.lioncore.LionCore;
import io.lionweb.model.ReferenceValue;
import io.lionweb.model.impl.M3Node;
import io.lionweb.utils.LanguageValidator;
import io.lionweb.utils.ValidationResult;
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
public class Language extends M3Node<Language> implements NamespaceProvider, IKeyed<Language> {
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

  public @Nonnull List<LanguageEntity> getElements() {
    return this.getContainmentMultipleValue("entities");
  }

  public Language addDependency(@Nonnull Language dependency) {
    Objects.requireNonNull(dependency, "dependency should not be null");
    this.addReferenceMultipleValue(
        "dependsOn", new ReferenceValue(dependency, dependency.getName()));
    return dependency;
  }

  public <T extends LanguageEntity> T addElement(@Nonnull T element) {
    Objects.requireNonNull(element, "element should not be null");
    this.addContainmentMultipleValue("entities", element);
    element.setParent(this);
    return element;
  }

  /**
   * Finds and returns a {@link Concept} by its name from the list of elements in the current
   * language.
   *
   * @param name the name of the desired {@link Concept}; may be null or empty, in which case null
   *     is returned
   * @return the {@link Concept} with the specified name if found, or null if no {@link Concept}
   *     with that name exists
   */
  public @Nullable Concept getConceptByName(@Nonnull String name) {
    Objects.requireNonNull(name, "name should not be null");
    return getElements().stream()
        .filter(element -> element instanceof Concept)
        .map(element -> (Concept) element)
        .filter(element -> element.getName().equals(name))
        .findFirst()
        .orElse(null);
  }

  /**
   * Retrieves a {@link Classifier} object from the list of elements by its name.
   *
   * @param name the name of the desired {@link Classifier}; must not be null
   * @return the {@link Classifier} with the specified name if found, or null if no {@link
   *     Classifier} with that name exists
   */
  public @Nullable Classifier<?> getClassifierByName(@Nonnull String name) {
    Objects.requireNonNull(name, "name should not be null");
    return getElements().stream()
        .filter(element -> element instanceof Classifier<?>)
        .map(element -> (Classifier<?>) element)
        .filter(element -> element.getName().equals(name))
        .findFirst()
        .orElse(null);
  }

  public @Nullable Enumeration getEnumerationByName(String name) {
    return getElements().stream()
        .filter(element -> element instanceof Enumeration)
        .map(element -> (Enumeration) element)
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

  /**
   * Ensures the retrieval of a {@link Classifier} by its name. If no classifier with the specified
   * name exists, an {@link IllegalArgumentException} is thrown.
   *
   * @param name the name of the desired {@link Classifier}; must not be null
   * @return the {@link Classifier} with the specified name
   * @throws NullPointerException if the name is null
   * @throws IllegalArgumentException if no classifier with the specified name is found
   */
  public Classifier<?> requireClassifierByName(@Nonnull String name) {
    Objects.requireNonNull(name, "name should not be null");
    Classifier<?> classifier = getClassifierByName(name);
    if (classifier == null) {
      throw new IllegalArgumentException("Classifier named " + name + " was not found");
    } else {
      return classifier;
    }
  }

  /**
   * Ensures the retrieval of an {@link Interface} by its name. If no interface with the specified
   * name exists, an {@link IllegalArgumentException} is thrown.
   *
   * @param name the name of the desired {@link Interface}; must not be null
   * @return the {@link Interface} with the specified name
   * @throws NullPointerException if the name is null
   * @throws IllegalArgumentException if no interface with the specified name is found
   */
  public Interface requireInterfaceByName(@Nonnull String name) {
    Objects.requireNonNull(name, "name should not be null");
    Interface interf = getInterfaceByName(name);
    if (interf == null) {
      throw new IllegalArgumentException("Interface named " + name + " was not found");
    } else {
      return interf;
    }
  }

  /**
   * Ensures the retrieval of an {@link Annotation} by its name. If no annotation with the specified
   * name exists, an {@link IllegalArgumentException} is thrown.
   *
   * @param name the name of the desired {@link Annotation}; must not be null
   * @return the {@link Annotation} with the specified name
   * @throws NullPointerException if the name is null
   * @throws IllegalArgumentException if no annotation with the specified name is found
   */
  public Annotation requireAnnotationByName(@Nonnull String name) {
    Objects.requireNonNull(name, "name should not be null");
    Annotation annotation = getAnnotationByName(name);
    if (annotation == null) {
      throw new IllegalArgumentException("Annotation named " + name + " was not found");
    } else {
      return annotation;
    }
  }

  /**
   * Ensures the retrieval of a {@link PrimitiveType} by its name. If no primitive type with the
   * specified name exists, an {@link IllegalArgumentException} is thrown.
   *
   * @param name the name of the desired {@link PrimitiveType}; must not be null
   * @return the {@link PrimitiveType} with the specified name
   * @throws NullPointerException if the name is null
   * @throws IllegalArgumentException if no
   */
  public PrimitiveType requirePrimitiveTypeByName(@Nonnull String name) {
    Objects.requireNonNull(name, "name should not be null");
    PrimitiveType primitiveType = getPrimitiveTypeByName(name);
    if (primitiveType == null) {
      throw new IllegalArgumentException("PrimitiveType named " + name + " was not found");
    } else {
      return primitiveType;
    }
  }

  public DataType<?> requireDataTypeByName(@Nonnull String name) {
    Objects.requireNonNull(name, "name should not be null");
    DataType<?> dataType = getDataTypeByName(name);
    if (dataType == null) {
      throw new IllegalArgumentException("DataType named " + name + " was not found");
    } else {
      return dataType;
    }
  }

  public @Nullable Interface getInterfaceByName(String name) {
    return getElements().stream()
        .filter(element -> element instanceof Interface)
        .map(element -> (Interface) element)
        .filter(element -> element.getName().equals(name))
        .findFirst()
        .orElse(null);
  }

  /**
   * Retrieves an {@link Annotation} object from the list of elements by its name.
   *
   * @param name the name of the desired {@link Annotation}; must not be null
   * @return the {@link Annotation} with the specified name if found, or null if no {@link
   *     Annotation} with that name exists
   */
  public @Nullable Annotation getAnnotationByName(@Nonnull String name) {
    Objects.requireNonNull(name, "name should not be null");
    return getElements().stream()
        .filter(element -> element instanceof Annotation)
        .map(element -> (Annotation) element)
        .filter(element -> element.getName().equals(name))
        .findFirst()
        .orElse(null);
  }

  public @Nullable String getName() {
    return this.getPropertyValue("name", String.class);
  }

  @Override
  public @Nullable String getKey() {
    return this.getPropertyValue("key", String.class);
  }

  public @Nullable String getVersion() {
    return this.getPropertyValue("version", String.class);
  }

  public @Nullable LanguageEntity<?> getElementByName(@Nonnull String name) {
    Objects.requireNonNull(name);
    return getElements().stream()
        .filter(element -> Objects.equals(element.getName(), name))
        .findFirst()
        .orElse(null);
  }

  /**
   * Retrieves a {@link PrimitiveType} by its name from the list of elements.
   *
   * <p>If an element with the specified name exists and is an instance of {@link PrimitiveType}, it
   * is returned. Otherwise, a {@link RuntimeException} is thrown if the element exists but is not a
   * {@link PrimitiveType}. If no element with the specified name exists, null is returned.
   *
   * @param name the name of the desired {@link PrimitiveType}; must not be null
   * @return the {@link PrimitiveType} with the specified name if found and valid, or null if no
   *     such element exists
   * @throws NullPointerException if the name is null
   * @throws RuntimeException if an element with the specified name exists but is not a {@link
   *     PrimitiveType}
   */
  public @Nullable PrimitiveType getPrimitiveTypeByName(@Nonnull String name) {
    Objects.requireNonNull(name);
    LanguageEntity<?> element = this.getElementByName(name);
    if (element == null) {
      return null;
    }
    if (element instanceof PrimitiveType) {
      return (PrimitiveType) element;
    } else {
      throw new RuntimeException("Element " + name + " is not a PrimitiveType");
    }
  }

  public @Nullable DataType<?> getDataTypeByName(@Nonnull String name) {
    Objects.requireNonNull(name);
    LanguageEntity<?> element = this.getElementByName(name);
    if (element == null) {
      return null;
    }
    if (element instanceof DataType) {
      return (DataType) element;
    } else {
      throw new RuntimeException("Element " + name + " is not a DataType");
    }
  }

  @Override
  public Concept getClassifier() {
    return LionCore.getLanguage(getLionWebVersion());
  }

  @Override
  public String toString() {
    return super.toString() + "{" + "name=" + getName() + "}";
  }

  public List<PrimitiveType> getPrimitiveTypes() {
    return this.getElements().stream()
        .filter(e -> e instanceof PrimitiveType)
        .map(e -> (PrimitiveType) e)
        .collect(Collectors.toList());
  }

  public boolean isValid() {
    return new LanguageValidator().isValid(this);
  }

  public @Nonnull ValidationResult validate() {
    return new LanguageValidator().validate(this);
  }

  public @Nonnull List<StructuredDataType> getStructuredDataTypes() {
    return getElements().stream()
        .filter(e -> e instanceof StructuredDataType)
        .map(e -> (StructuredDataType) e)
        .collect(Collectors.toList());
  }

  public @Nonnull List<Concept> getConcepts() {
    return getElements().stream()
        .filter(e -> e instanceof Concept)
        .map(e -> (Concept) e)
        .collect(Collectors.toList());
  }

  public @Nonnull List<Interface> getInterfaces() {
    return getElements().stream()
        .filter(e -> e instanceof Interface)
        .map(e -> (Interface) e)
        .collect(Collectors.toList());
  }

  public @Nonnull List<Annotation> getAnnotationDefinitions() {
    return getElements().stream()
        .filter(e -> e instanceof Annotation)
        .map(e -> (Annotation) e)
        .collect(Collectors.toList());
  }
}
