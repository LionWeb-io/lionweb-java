package io.lionweb.lioncore.java.metamodel;

import io.lionweb.lioncore.java.Experimental;
import io.lionweb.lioncore.java.self.LionCore;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents a relation between a containing {@link FeaturesContainer} and a contained {@link
 * FeaturesContainer}.
 *
 * <p>Between an IfStatement and its condition there is a Containment relation.
 *
 * <p>Differently from an EReference there is no container flag and resolveProxies flag.
 *
 * @see org.eclipse.emf.ecore.EReference Ecore equivalent <i>EReference</i> (with the <code>
 *     containment</code> flag set to <code>true</code>)
 * @see <a href="https://www.jetbrains.com/help/mps/structure.html#children">MPS equivalent
 *     <i>Child</i> in documentation</a>
 * @see <a
 *     href="http://127.0.0.1:63320/node?ref=r%3A00000000-0000-4000-0000-011c89590292%28jetbrains.mps.lang.structure.structure%29%2F1071489288298">MPS
 *     equivalent <i>LinkDeclaration</i> in local MPS (with <code>metaClass</code> having value
 *     <code>aggregation</code>)</a>
 * @see org.jetbrains.mps.openapi.language.SContainmentLink MPS equivalent <i>SContainmentLink</i>
 *     in SModel
 */
public class Containment extends Link<Containment> {

  public static Containment createOptional(
      @Nullable String name, @Nullable FeaturesContainer type) {
    Containment containment = new Containment(name);
    containment.setOptional(true);
    containment.setMultiple(false);
    containment.setType(type);
    return containment;
  }

  public static Containment createRequired(
      @Nullable String name, @Nullable FeaturesContainer type) {
    Containment containment = new Containment(name);
    containment.setOptional(false);
    containment.setMultiple(false);
    containment.setType(type);
    return containment;
  }

  public static Containment createMultiple(
      @Nullable String name, @Nullable FeaturesContainer type) {
    Containment containment = new Containment(name);
    containment.setOptional(true);
    containment.setMultiple(true);
    containment.setType(type);
    return containment;
  }

  public static Containment createMultiple(
      @Nullable String name, @Nullable FeaturesContainer type, @Nonnull String id) {
    Objects.requireNonNull(id, "id should not be null");
    Containment containment = new Containment(name, id);
    containment.setOptional(true);
    containment.setMultiple(true);
    containment.setType(type);
    return containment;
  }

  public static Containment createMultipleAndRequired(
      @Nullable String name, @Nullable FeaturesContainer type) {
    Containment containment = new Containment(name);
    containment.setOptional(false);
    containment.setMultiple(true);
    containment.setType(type);
    return containment;
  }

  @Experimental private Containment specialized;

  public Containment() {
    super();
  }

  public Containment(String name, @Nullable FeaturesContainer container) {
    // TODO verify that the container is also a NamespaceProvider
    super(name, container);
  }

  public Containment(String name) {
    super(name, (FeaturesContainer) null);
  }

  public Containment(String name, @Nonnull String id) {
    super(name, id);
  }

  public @Nullable Containment getSpecialized() {
    return specialized;
  }

  public void setSpecialized(@Nullable Containment specialized) {
    // TODO check which limitations there are: should have the same name? Should it belong
    //      to an ancestor of the FeaturesContainer holding this Containment?
    this.specialized = specialized;
  }

  @Override
  public String toString() {
    return "Containment{" + "name=" + getName() + ", " + "type=" + getType() + '}';
  }

  @Override
  public Concept getConcept() {
    return LionCore.getContainment();
  }
}