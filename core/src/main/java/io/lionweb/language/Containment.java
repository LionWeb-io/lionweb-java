package io.lionweb.language;

import io.lionweb.LionWebVersion;
import io.lionweb.lioncore.LionCore;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents a relation between a containing {@link Classifier} and a contained {@link Classifier}.
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

  public static Containment createOptional(@Nullable String name, @Nullable Classifier type) {
    Containment containment = new Containment(name);
    containment.setOptional(true);
    containment.setMultiple(false);
    containment.setType(type);
    return containment;
  }

  public static Containment createOptional(
      @Nullable String name, @Nullable Classifier type, @Nullable String id, @Nullable String key) {
    Containment containment = new Containment(name);
    containment.setOptional(true);
    containment.setMultiple(false);
    containment.setType(type);
    containment.setID(id);
    containment.setKey(key);
    return containment;
  }

  public static Containment createOptional(
      @Nullable String name, @Nullable Classifier type, @Nullable String id) {
    Containment containment = new Containment(name);
    containment.setOptional(true);
    containment.setMultiple(false);
    containment.setType(type);
    containment.setID(id);
    return containment;
  }

  public static Containment createRequired(@Nullable String name, @Nullable Classifier type) {
    Containment containment = new Containment(name);
    containment.setOptional(false);
    containment.setMultiple(false);
    containment.setType(type);
    return containment;
  }

  public static Containment createMultiple(
      @Nonnull LionWebVersion lionWebVersion, @Nullable String name, @Nullable Classifier type) {
    Objects.requireNonNull(lionWebVersion, "lionWebVersion should not be null");
    Containment containment = new Containment(lionWebVersion, name);
    containment.setOptional(true);
    containment.setMultiple(true);
    containment.setType(type);
    return containment;
  }

  public static Containment createMultiple(@Nullable String name, @Nullable Classifier type) {
    Containment containment = new Containment(name);
    containment.setOptional(true);
    containment.setMultiple(true);
    containment.setType(type);
    return containment;
  }

  public static Containment createMultiple(
      @Nonnull LionWebVersion lionWebVersion,
      @Nullable String name,
      @Nullable Classifier type,
      @Nonnull String id) {
    Objects.requireNonNull(lionWebVersion, "lionWebVersion should not be null");
    Objects.requireNonNull(id, "id should not be null");
    Containment containment = new Containment(lionWebVersion, name, id);
    containment.setOptional(true);
    containment.setMultiple(true);
    containment.setType(type);
    return containment;
  }

  public static Containment createMultiple(
      @Nullable String name, @Nullable Classifier type, @Nonnull String id) {
    Objects.requireNonNull(id, "id should not be null");
    Containment containment = new Containment(name, id);
    containment.setOptional(true);
    containment.setMultiple(true);
    containment.setType(type);
    return containment;
  }

  public static Containment createMultipleAndRequired(
      @Nullable String name, @Nullable Classifier type) {
    Containment containment = new Containment(name);
    containment.setOptional(false);
    containment.setMultiple(true);
    containment.setType(type);
    return containment;
  }

  public Containment() {
    super();
  }

  public Containment(@Nonnull LionWebVersion lionWebVersion) {
    super(lionWebVersion);
  }

  public Containment(String name, @Nullable Classifier container) {
    super(name, container);
  }

  public Containment(@Nonnull LionWebVersion lionWebVersion, String name) {
    super(lionWebVersion, name, (Classifier) null);
  }

  public Containment(String name) {
    super(name, (Classifier) null);
  }

  public Containment(@Nonnull LionWebVersion lionWebVersion, String name, @Nonnull String id) {
    super(lionWebVersion, name, id);
  }

  public Containment(String name, @Nonnull String id) {
    super(name, id);
  }

  @Override
  public Concept getClassifier() {
    return LionCore.getContainment(getLionWebVersion());
  }
}
