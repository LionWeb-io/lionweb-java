package io.lionweb.model;

import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CompositeReferenceValueObserver implements ReferenceValueObserver {
  private static final int INTERN_LIMIT = 3;
  private static final Map<Set<ReferenceValueObserver>, ReferenceValueObserver> INTERN_CACHE =
      new WeakHashMap<>();

  private final Set<ReferenceValueObserver> elements;

  private CompositeReferenceValueObserver(Set<ReferenceValueObserver> elements) {
    this.elements = elements;
  }

  public static ReferenceValueObserver combine(ReferenceValueObserver a, ReferenceValueObserver b) {
    Set<ReferenceValueObserver> combined = new HashSet<>();

    // flatten left
    if (a instanceof CompositeReferenceValueObserver) {
      combined.addAll(((CompositeReferenceValueObserver) a).elements);
    } else {
      combined.add(a);
    }

    // flatten right
    if (b instanceof CompositeReferenceValueObserver) {
      combined.addAll(((CompositeReferenceValueObserver) b).elements);
    } else {
      combined.add(b);
    }

    return getInstance(combined);
  }

  private static ReferenceValueObserver getInstance(Set<ReferenceValueObserver> elements) {
    // intern if small, but don’t intern larger ones
    if (elements.size() <= INTERN_LIMIT) {
      Set<ReferenceValueObserver> key = Collections.unmodifiableSet(new HashSet<>(elements));
      return INTERN_CACHE.computeIfAbsent(
          key, k -> k.size() == 1 ? k.iterator().next() : new CompositeReferenceValueObserver(k));
    } else {
      return new CompositeReferenceValueObserver(Collections.unmodifiableSet(elements));
    }
  }

  public void addElement(ReferenceValueObserver observer) {
    elements.add(observer);
  }

  public void removeElement(ReferenceValueObserver observer) {
    elements.remove(observer);
  }

  @Override
  public void resolveInfoChanged(
      @Nonnull ReferenceValue referenceValue,
      @Nullable String oldValue,
      @Nullable String newValue) {
    this.elements.forEach(
        element -> element.resolveInfoChanged(referenceValue, oldValue, newValue));
  }

  @Override
  public void referredIDChanged(
      @Nonnull ReferenceValue referenceValue,
      @Nullable String oldValue,
      @Nullable String newValue) {
    this.elements.forEach(element -> element.referredIDChanged(referenceValue, oldValue, newValue));
  }

  public Set<ReferenceValueObserver> getElements() {
    return elements;
  }

  public ReferenceValueObserver remove(ReferenceValueObserver observer) {
    if (!this.elements.contains(observer)) {
      throw new IllegalArgumentException("Cannot remove unknown observer: " + observer);
    }
    Set<ReferenceValueObserver> newElements = new HashSet<>(this.elements);
    newElements.remove(observer);
    return getInstance(newElements);
  }
}
