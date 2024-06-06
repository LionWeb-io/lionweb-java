package io.lionweb.lioncore.java.language;

import io.lionweb.lioncore.java.model.ReferenceValue;
import io.lionweb.lioncore.java.self.LionCore;
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
public class Interface extends Classifier<Interface> {
  public Interface() {
    super();
  }

  public Interface(@Nullable Language language, @Nullable String name, @Nonnull String id) {
    super(language, name, id);
  }

  public Interface(
      @Nullable Language language,
      @Nullable String name,
      @Nonnull String id,
      @Nullable String key) {
    this(language, name, id);
    setKey(key);
  }

  public Interface(@Nullable Language language, @Nullable String name) {
    super(language, name);
  }

  public Interface(@Nullable String name) {
    super(null, name);
  }

  public Interface(@Nullable String name, @Nonnull String id) {
    super(null, name, id);
  }

  public @Nonnull List<Interface> getExtendedInterfaces() {
    return getReferenceMultipleValue("extends");
  }

  public void addExtendedInterface(@Nonnull Interface extendedInterface) {
    Objects.requireNonNull(extendedInterface, "extendedInterface should not be null");
    this.addReferenceMultipleValue(
        "extends", new ReferenceValue(extendedInterface, extendedInterface.getName()));
  }

  @Nonnull
  @Override
  public List<Feature<?>> inheritedFeatures() {
    List<Feature<?>> result = new LinkedList<>();
    for (Classifier<?> superInterface : allAncestors()) {
      combineFeatures(result, superInterface.allFeatures());
    }
    return result;
  }

  @Override
  public Concept getClassifier() {
    return LionCore.getInterface();
  }

  @Nonnull
  @Override
  public List<Classifier<?>> directAncestors() {
    return (List<Classifier<?>>) (Object) this.getExtendedInterfaces();
  }

  public Set<Interface> allExtendedInterfaces() {
    Set<Interface> toAvoid = new HashSet<>();
    toAvoid.add(this);
    return allExtendedInterfacesHelper(toAvoid);
  }

  private Set<Interface> allExtendedInterfacesHelper(Set<Interface> toAvoid) {
    Set<Interface> interfaces = new HashSet<>();
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
