package io.lionweb.model.impl;

import static io.lionweb.model.ClassifierInstanceUtils.*;

import io.lionweb.LionWebVersion;
import io.lionweb.language.*;
import io.lionweb.model.*;
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Base class to help implements Node in the language package.
 *
 * <p>Other libraries could implement Node differently, for example based on reflection. However,
 * this is outside the scope of this library. This library should provide a solid, basic dependency
 * to be used by other implementation and it should be as reusable, basic, and unopinionated as
 * possible.
 *
 * <p>Each M3Node is connected to a specific version of lionWebVersion, as these elements may behave
 * differently depending on the version of LionWeb they are representing.
 */
public abstract class M3Node<T extends M3Node> extends AbstractClassifierInstance<Concept>
    implements Node, HasSettableParent, HasSettableID {
  private final @Nonnull LionWebVersion lionWebVersion;
  private @Nullable String id;
  private @Nullable ClassifierInstance<?> parent;

  // We use as keys of these maps the name of the features and not the IDs.
  // The reason why we do that, is to avoid a circular dependency as the classes for defining
  // language
  // elements are inheriting from this class.
  private final Map<String, Object> propertyValues = new HashMap<>();
  private final Map<String, List<Node>> containmentValues = new HashMap<>();
  private final Map<String, List<ReferenceValue>> referenceValues = new HashMap<>();

  protected M3Node() {
    this.lionWebVersion = LionWebVersion.currentVersion;
  }

  protected M3Node(@Nonnull LionWebVersion lionWebVersion) {
    Objects.requireNonNull(lionWebVersion, "lionWebVersion should not be null");
    this.lionWebVersion = lionWebVersion;
  }

  /**
   * The ID can be _temporarily_ left to null, but _eventually_ it should be not null, so we are
   * preventing assigning null to it (see https://github.com/LionWeb-io/lionweb-java/pull/234).
   */
  public @Nonnull T setID(@Nonnull String id) {
    Objects.requireNonNull(id);
    this.id = id;
    return (T) this;
  }

  public T setParent(ClassifierInstance<?> parent) {
    this.parent = parent;
    return (T) this;
  }

  @Override
  @Nullable
  public ClassifierInstance<?> getParent() {
    return parent;
  }

  @Override
  public @Nullable Object getPropertyValue(@Nonnull Property property) {
    Objects.requireNonNull(property, "property should not be null");
    if (!getClassifier().allProperties().contains(property)) {
      throw new IllegalArgumentException("Property not belonging to this concept: " + property);
    }
    return propertyValues.get(property.getName());
  }

  /**
   * This internal method uses a property name and not a property or the property id because of a
   * circular dependency problem present for nodes representing M3 elements.
   */
  protected <V> @Nullable V getPropertyValue(
      @Nonnull String propertyName, @Nonnull Class<V> clazz, @Nullable V defaultValue) {
    Object value = propertyValues.get(propertyName);
    if (value == null) {
      return defaultValue;
    } else {
      return clazz.cast(value);
    }
  }

  protected <V> V getPropertyValue(String propertyName, Class<V> clazz) {
    return getPropertyValue(propertyName, clazz, null);
  }

  @Override
  public void setPropertyValue(@Nonnull Property property, @Nullable Object value) {
    Objects.requireNonNull(property, "property should not be null");
    if (!getClassifier().allProperties().contains(property)) {
      throw new IllegalArgumentException("Property not belonging to this concept");
    }
    setPropertyValue(property.getName(), value);
  }

  protected void setPropertyValue(String propertyName, Object value) {
    propertyValues.put(propertyName, value);
  }

  @Override
  public @Nonnull List<Node> getChildren(@Nonnull Containment containment) {
    Objects.requireNonNull(containment, "containment should not be null");
    if (!getClassifier().allContainments().contains(containment)) {
      throw new IllegalArgumentException("Containment not belonging to this concept");
    }
    return containmentValues.getOrDefault(containment.getName(), Collections.emptyList());
  }

  @Override
  public void addChild(@Nonnull Containment containment, @Nonnull Node child) {
    Objects.requireNonNull(containment);
    Objects.requireNonNull(child);
    if (containment.isMultiple()) {
      addContainmentMultipleValue(containment.getName(), child);
    } else {
      setContainmentSingleValue(containment.getName(), child);
    }
  }

  @Override
  public void removeChild(@Nonnull Node child) {
    Objects.requireNonNull(child, "child should not be null");
    if (child.getParent() != this) {
      throw new IllegalArgumentException(
          "Cannot remove the given child, as this node it is not its parent");
    }
    /*
     * Note that, if the parent of the child is equal to this, it means the parent
     * is not null, and therefore child.getContainmentFeature should not be null.
     * Most implementation of the method (including the default one in Node) would
     * either return a proper value or throw an exception to signal the inconsistency,
     * so the extra check for feature not to be null is out of caution and to report
     * the inconsistency, if somehow this has not been done by getContainmentFeature
     */
    Feature<?> feature = child.getContainmentFeature();
    if (feature == null) {
      throw new IllegalStateException(
          "If the parent is not null, the containment feature should not be null");
    }

    List<Node> children = containmentValues.get(feature.getName());
    children.remove(child);
    if (child instanceof HasSettableParent) {
      ((HasSettableParent) child).setParent(null);
    }
  }

  @Nonnull
  @Override
  public List<ReferenceValue> getReferenceValues(@Nonnull Reference reference) {
    Objects.requireNonNull(reference, "reference should not be null");
    if (!getClassifier().allReferences().contains(reference)) {
      throw new IllegalArgumentException("Reference not belonging to this concept");
    }
    return referenceValues.getOrDefault(reference.getName(), Collections.emptyList());
  }

  @Override
  public void addReferenceValue(
      @Nonnull Reference reference, @Nullable ReferenceValue referenceValue) {
    Objects.requireNonNull(reference, "reference should not be null");
    if (!getClassifier().allReferences().contains(reference)) {
      throw new IllegalArgumentException("Reference not belonging to this concept: " + reference);
    }
    if (reference.isMultiple()) {
      addReferenceMultipleValue(reference.getName(), referenceValue);
    } else {
      setReferenceSingleValue(reference.getName(), referenceValue);
    }
  }

  @Override
  public void setReferenceValues(
      @Nonnull Reference reference, @Nonnull List<? extends ReferenceValue> values) {
    Objects.requireNonNull(reference, "reference should not be null");
    if (!getClassifier().allReferences().contains(reference)) {
      throw new IllegalArgumentException("Reference not belonging to this concept");
    }
    referenceValues.put(reference.getName(), (List<ReferenceValue>) values);
  }

  @Nullable
  @Override
  public String getID() {
    return id;
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName() + "[" + this.getID() + "]";
  }

  protected <V extends Node> V getContainmentSingleValue(String linkName) {
    if (containmentValues.containsKey(linkName)) {
      List<Node> values = containmentValues.get(linkName);
      if (values.isEmpty()) {
        return null;
      } else if (values.size() == 1) {
        return (V) (values.get(0));
      } else {
        throw new IllegalStateException();
      }
    } else {
      return null;
    }
  }

  protected <V extends Node> V getReferenceSingleValue(String linkName) {
    if (referenceValues.containsKey(linkName)) {
      List<ReferenceValue> values = referenceValues.get(linkName);
      if (values.isEmpty()) {
        return null;
      } else if (values.size() == 1) {
        return (V) (values.get(0).getReferred());
      } else {
        throw new IllegalStateException();
      }
    } else {
      return null;
    }
  }

  protected <V extends Node> List<V> getContainmentMultipleValue(String linkName) {
    if (containmentValues.containsKey(linkName)) {
      List<V> values = (List<V>) containmentValues.get(linkName);
      return values;
    } else {
      return Collections.emptyList();
    }
  }

  protected <V extends Node> List<V> getReferenceMultipleValue(String linkName) {
    if (referenceValues.containsKey(linkName)) {
      List<V> values =
          (List<V>)
              referenceValues.get(linkName).stream()
                  .map(rv -> rv.getReferred())
                  .collect(Collectors.toList());
      return values;
    } else {
      return Collections.emptyList();
    }
  }

  /*
   * This method could be invoked by the language elements classes before the LionCore language
   * has been built, therefore we cannot look for the definition of the features to verify they
   * exist. We instead just trust a link with that name to exist.
   */
  private void setContainmentSingleValue(String linkName, Node value) {
    List<Node> prevValue = containmentValues.get(linkName);
    if (prevValue != null) {
      List<Node> copy = new LinkedList<>(prevValue);
      copy.forEach(c -> this.removeChild(c));
    }
    if (value == null) {
      containmentValues.remove(linkName);
    } else {
      ((M3Node) value).setParent(this);
      containmentValues.put(linkName, new ArrayList(Arrays.asList(value)));
    }
  }

  /*
   * This method could be invoked by the language elements classes before the LionCore language
   * has been built, therefore we cannot look for the definition of the features to verify they
   * exist. We instead just trust a link with that name to exist.
   */
  protected void setReferenceSingleValue(@Nonnull String linkName, @Nullable ReferenceValue value) {
    if (value == null) {
      referenceValues.remove(linkName);
    } else {
      referenceValues.put(linkName, new ArrayList(Arrays.asList(value)));
    }
  }

  /**
   * Adding a null value or a value already contained, do not produce any change.
   *
   * @return return true if the addition produced a change
   */
  protected boolean addContainmentMultipleValue(@Nonnull String linkName, Node value) {
    if (value == null) {
      return false;
    }
    if (getContainmentMultipleValue(linkName).stream().anyMatch(e -> e == value)) {
      return false;
    }
    ((M3Node) value).setParent(this);
    if (containmentValues.containsKey(linkName)) {
      containmentValues.get(linkName).add(value);
    } else {
      containmentValues.put(linkName, new ArrayList(Arrays.asList(value)));
    }
    return true;
  }

  protected void addReferenceMultipleValue(String linkName, ReferenceValue value) {
    if (value == null) {
      return;
    }
    if (referenceValues.containsKey(linkName)) {
      referenceValues.get(linkName).add(value);
    } else {
      referenceValues.put(linkName, new ArrayList(Arrays.asList(value)));
    }
  }

  @Nonnull
  public LionWebVersion getLionWebVersion() {
    return lionWebVersion;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Node)) return false;
    Node other = (Node) o;

    return Objects.equals(getID(), other.getID())
        && shallowClassifierInstanceEquality(parent, other.getParent())
        && shallowClassifierInstanceEquality(getClassifier(), other.getClassifier())
        && getClassifier().allProperties().stream()
            .allMatch(p -> Objects.equals(getPropertyValue(p), other.getPropertyValue(p)))
        && getClassifier().allContainments().stream()
            .allMatch(c -> shallowContainmentEquality(getChildren(c), other.getChildren(c)))
        && getClassifier().allReferences().stream()
            .allMatch(
                r -> shallowReferenceEquality(getReferenceValues(r), other.getReferenceValues(r)))
        && shallowAnnotationsEquality(annotations, other.getAnnotations());
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
