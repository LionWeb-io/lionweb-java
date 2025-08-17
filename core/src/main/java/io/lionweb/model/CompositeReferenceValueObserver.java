package io.lionweb.model;

import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CompositeReferenceValueObserver implements ReferenceValueObserver {
  private static final int INTERN_LIMIT = 3;
  private static final Map<List<ReferenceValueObserver>, ReferenceValueObserver> INTERN_CACHE =
      new WeakHashMap<>();

  private final List<ReferenceValueObserver> elements;

  private CompositeReferenceValueObserver(List<ReferenceValueObserver> elements) {
    this.elements = elements;
  }

  public static ReferenceValueObserver combine(ReferenceValueObserver a, ReferenceValueObserver b) {
    List<ReferenceValueObserver> combined = new ArrayList<>();

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

    // intern if small, but don’t intern larger ones
    if (combined.size() <= INTERN_LIMIT) {
      List<ReferenceValueObserver> key = Collections.unmodifiableList(new ArrayList<>(combined));
      return INTERN_CACHE.computeIfAbsent(
          key, k -> k.size() == 1 ? k.get(0) : new CompositeReferenceValueObserver(k));
    } else {
      return new CompositeReferenceValueObserver(Collections.unmodifiableList(combined));
    }
  }

  public void addReferenceValueObserver(ReferenceValueObserver observer) {
    elements.add(observer);
  }

  public void removeReferenceValueObserver(ReferenceValueObserver observer) {
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

  public List<ReferenceValueObserver> getElements() {
    return elements;
  }
}
