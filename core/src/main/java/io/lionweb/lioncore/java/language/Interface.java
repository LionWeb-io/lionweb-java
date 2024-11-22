package io.lionweb.lioncore.java.language;

import io.lionweb.lioncore.java.versions.LionWebVersion;
import io.lionweb.lioncore.java.model.ReferenceValue;
import io.lionweb.lioncore.java.self.LionCore;
import io.lionweb.lioncore.java.versions.LionWebVersionToken;

import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * An Interface represents a category of entities sharing some similar characteristics.
 *
 * <p>For example, Named would be an Interface.
 *
 * @see org.eclipse.emf.ecore.EClass Ecore equivalent <i>EClass</i> (with the <code>isInterface
 *     </code> flag set to <code>true</code>)
 * @see <a href="https://www.jetbrains.com/help/mps/structure.html#conceptsandconceptinterfaces">MPS
 *     equivalent <i>Concept Interface</i> in documentation</a>
 * @see <a
 *     href="http://127.0.0.1:63320/node?ref=r%3A00000000-0000-4000-0000-011c89590292%28jetbrains.mps.lang.structure.structure%29%2F1169125989551">MPS
 *     equivalent <i>InterfaceConceptDeclaration</i> in local MPS</a>
 * @see org.jetbrains.mps.openapi.language.SInterfaceConcept MPS equivalent <i>SInterfaceConcept</i>
 *     in SModel
 */
public class Interface<V extends LionWebVersionToken> extends Classifier<Interface<V>, V> {
  public Interface() {
    super();
  }

  public Interface(@Nonnull LionWebVersion lionWebVersion) {
    super(lionWebVersion);
  }

  public Interface(@Nullable Language<V> language, @Nullable String name, @Nonnull String id) {
    super(language, name, id);
  }

  public Interface(
      @Nullable Language<V> language,
      @Nullable String name,
      @Nonnull String id,
      @Nullable String key) {
    this(language, name, id);
    setKey(key);
  }

  public Interface(@Nullable Language<V> language, @Nullable String name) {
    super(language, name);
  }

  public Interface(
      @Nonnull LionWebVersion lionWebVersion, @Nullable Language<V> language, @Nullable String name) {
    super(lionWebVersion, language, name);
  }

  public Interface(@Nonnull LionWebVersion lionWebVersion, @Nullable String name) {
    super(lionWebVersion, null, name);
  }

  public Interface(@Nullable String name) {
    super(null, name);
  }

  public Interface(@Nullable String name, @Nonnull String id) {
    super(null, name, id);
  }

  public @Nonnull List<Interface<V>> getExtendedInterfaces() {
    return getReferenceMultipleValue("extends");
  }

  public void addExtendedInterface(@Nonnull Interface<V> extendedInterface) {
    Objects.requireNonNull(extendedInterface, "extendedInterface should not be null");
    this.addReferenceMultipleValue(
        "extends", new ReferenceValue(extendedInterface, extendedInterface.getName()));
  }

  @Nonnull
  @Override
  public List<Feature<?, V>> inheritedFeatures() {
    List<Feature<?, V>> result = new LinkedList<>();
    for (Classifier<?, V> superInterface : allAncestors()) {
      combineFeatures(result, superInterface.allFeatures());
    }
    return result;
  }

  @Override
  public Concept<V> getClassifier() {
    return LionCore.getInterface(getLionWebVersionToken());
  }

  @Nonnull
  @Override
  public List<Classifier<?, V>> directAncestors() {
    return (List<Classifier<?, V>>) (Object) this.getExtendedInterfaces();
  }

  public Set<Interface<V>> allExtendedInterfaces() {
    Set<Interface<V>> toAvoid = new HashSet<>();
    toAvoid.add(this);
    return allExtendedInterfacesHelper(toAvoid);
  }

  private Set<Interface<V>> allExtendedInterfacesHelper(Set<Interface<V>> toAvoid) {
    Set<Interface<V>> interfaces = new HashSet<>();
    toAvoid.add(this);
    this.getExtendedInterfaces()
        .forEach(
            ei -> {
              boolean toConsider = !toAvoid.contains(ei) && !interfaces.contains(ei);
              interfaces.add(ei);
              if (toConsider) {
                interfaces.addAll(ei.allExtendedInterfacesHelper(toAvoid));
              }
            });
    return interfaces;
  }
}
