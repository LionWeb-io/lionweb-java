package io.lionweb.lioncore.java.model.impl;

import io.lionweb.lioncore.java.language.Concept;
import io.lionweb.lioncore.java.language.Containment;
import io.lionweb.lioncore.java.language.Property;
import io.lionweb.lioncore.java.language.Reference;
import io.lionweb.lioncore.java.model.Node;
import io.lionweb.lioncore.java.model.ReferenceValue;
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Base class to help implements Node in the language package.
 *
 * <p>Other libraries could implement Node differently, for example based on reflection. However
 * this is outside the scope of this library. This library should provide a solid, basic dependency
 * to be used by other implementation and it should be as reusable, basic, and unopinionated as
 * possible.
 */
public abstract class M3Node<T extends M3Node> extends AbstractClassifierInstance<Concept>
    implements Node {
  private String id;
  private Node parent;

  // We use as keys of these maps the name of the features and not the IDs.
  // The reason why we do that, is to avoid a circular dependency as the classes for defining
  // language
  // elements are inheriting from this class.
  private final Map<String, Object> propertyValues = new HashMap<>();
  private final Map<String, List<Node>> containmentValues = new HashMap<>();
  private final Map<String, List<ReferenceValue>> referenceValues = new HashMap<>();

  public T setID(String id) {
    this.id = id;
    return (T) this;
  }

  public T setParent(Node parent) {
    this.parent = parent;
    return (T) this;
  }

  @Override
  public Node getRoot() {
    if (getParent() == null) {
      return this;
    }
    return getParent().getRoot();
  }

  @Override
  public Node getParent() {
    return parent;
  }

  @Override
  public Containment getContainmentFeature() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object getPropertyValue(Property property) {
    if (!getClassifier().allProperties().contains(property)) {
      throw new IllegalArgumentException("Property not belonging to this concept: " + property);
    }
    return propertyValues.get(property.getName());
  }

  /**
   * This internal method uses a property name and not a property or the property id because of a
   * circular dependency problem present for nodes representing M3 elements.
   */
  protected <V> V getPropertyValue(String propertyName, Class<V> clazz, V defaultValue) {
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
  public void setPropertyValue(Property property, Object value) {
    if (!getClassifier().allProperties().contains(property)) {
      throw new IllegalArgumentException("Property not belonging to this concept");
    }
    setPropertyValue(property.getName(), value);
  }

  protected void setPropertyValue(String propertyName, Object value) {
    propertyValues.put(propertyName, value);
  }

  @Override
  public List<Node> getChildren(Containment containment) {
    if (!getClassifier().allContainments().contains(containment)) {
      throw new IllegalArgumentException("Containment not belonging to this concept");
    }
    return containmentValues.getOrDefault(containment.getName(), Collections.emptyList());
  }

  @Override
  public void addChild(Containment containment, Node child) {
    if (containment.isMultiple()) {
      addContainmentMultipleValue(containment.getName(), child);
    } else {
      setContainmentSingleValue(containment.getName(), child);
    }
  }

  @Override
  public void removeChild(Node node) {
    throw new UnsupportedOperationException();
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
    if (getContainmentMultipleValue(linkName).contains(value)) {
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
}
