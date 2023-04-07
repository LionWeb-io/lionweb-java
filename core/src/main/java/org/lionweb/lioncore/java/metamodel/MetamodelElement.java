package org.lionweb.lioncore.java.metamodel;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.lionweb.lioncore.java.model.impl.M3Node;

/**
 * A MetamodelElement is an element with an identity within a {@link Metamodel}.
 *
 * <p>For example, Invoice, Currency, Named, or String could be MetamodelElements.
 *
 * @see org.eclipse.emf.ecore.EClassifier Ecore equivalent <i>EClassifier</i>
 * @see <a
 *     href="http://127.0.0.1:63320/node?ref=r%3A00000000-0000-4000-0000-011c89590292%28jetbrains.mps.lang.structure.structure%29%2F1588368162880706270">MPS
 *     equivalent <i>IStructureElement</i> in local MPS</a>
 * @see org.jetbrains.mps.openapi.language.SElement MPS equivalent <i>SElement</i> in SModel
 */
public abstract class MetamodelElement<T extends M3Node> extends M3Node<T>
    implements NamespacedEntity, HasKey<T> {

  public MetamodelElement() {}

  public MetamodelElement(
      @Nullable Metamodel metamodel, @Nullable String simpleName, @Nonnull String id) {
    this(metamodel, simpleName);
    this.setID(id);
  }

  public MetamodelElement(@Nullable Metamodel metamodel, @Nullable String simpleName) {
    // TODO enforce uniqueness of the name within the Metamodel
    this.setParent(metamodel);
    this.setSimpleName(simpleName);
  }

  /**
   * This method returns the Metamodel containing this element. It is the parent, casted to
   * Metamodel.
   *
   * @return
   */
  public @Nullable Metamodel getMetamodel() {
    if (getParent() == null) {
      return null;
    } else if (getParent() instanceof Metamodel) {
      return (Metamodel) getParent();
    } else {
      throw new IllegalStateException("The parent of this MetamodelElement is not a Metamodel");
    }
  }

  @Override
  public @Nullable String getSimpleName() {
    return this.getPropertyValue("simpleName", String.class);
  }

  public T setSimpleName(String simpleName) {
    this.setPropertyValue("simpleName", simpleName);
    return (T) this;
  }

  @Override
  public @Nullable NamespaceProvider getContainer() {
    if (this.getParent() == null) {
      return null;
    }
    if (this.getParent() instanceof NamespaceProvider) {
      return (NamespaceProvider) this.getParent();
    } else {
      throw new IllegalStateException("The parent is not a NamespaceProvider");
    }
  }

  @Override
  public String getKey() {
    return this.getPropertyValue("key", String.class);
  }

  @Override
  public T setKey(String key) {
    setPropertyValue("key", key);
    return (T) this;
  }

  protected Object getDerivedValue(Property property) {
    if (property.getKey().equals(this.getConcept().getPropertyByName("qualifiedName").getKey())) {
      return qualifiedName();
    }
    return null;
  }

  @Override
  public String toString() {
    String qualifier = "<no metamodel>";
    if (this.getContainer() != null) {
      if (this.getContainer().namespaceQualifier() != null) {
        qualifier = this.getContainer().namespaceQualifier();
      } else {
        qualifier = "<unnamed metamodel>";
      }
    }
    ;
    String qualified = "<unnamed>";
    if (this.getSimpleName() != null) {
      qualified = this.getSimpleName();
    }
    ;
    String qn = qualifier + "." + qualified;
    return this.getClass().getSimpleName() + "(" + qn + ")";
  }
}
