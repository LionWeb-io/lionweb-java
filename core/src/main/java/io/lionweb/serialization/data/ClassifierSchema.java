package io.lionweb.serialization.data;

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;

/**
 * Describes the structural layout — the ordered sequence of properties, containments, and
 * references — shared by all {@link SerializedClassifierInstance} objects of the same classifier
 * and the same observed feature set.
 *
 * <h3>Motivation</h3>
 *
 * <p>In a typical model the vast majority of nodes that share a classifier also share exactly the
 * same set of features. The naive representation stores one {@link MetaPointer} per feature <em>per
 * node</em>, inside wrapper objects like {@link SerializedPropertyValue}. For a model with 100 K
 * nodes of the same type, those MetaPointer references are stored 100 K times each. This class
 * factors them out into a single, shared descriptor: instances then hold only the <em>values</em>,
 * indexed positionally against the schema.
 *
 * <p>The analogy is Java's {@code Class} object: it describes field layout once, while every object
 * instance holds field values without re-stating the field names or types.
 *
 * <h3>Interning</h3>
 *
 * <p>Schemas are interned in a global registry keyed on the exact {@code (classifier,
 * propertyKeys[], containmentKeys[], referenceKeys[])} tuple. Because {@link MetaPointer} objects
 * are themselves interned and use identity equality and identity hash codes, array comparison
 * reduces to element-identity comparison — cheap and correct.
 *
 * <h3>Handling nodes that deviate from the schema</h3>
 *
 * <p>Nodes whose features differ from their assigned schema are supported via an <em>overflow
 * map</em> on the instance side (see {@link SerializedClassifierInstance}). The schema itself is
 * always immutable once registered.
 */
public final class ClassifierSchema {

  // ---------------------------------------------------------------------------
  // Global registry
  // ---------------------------------------------------------------------------

  /**
   * One canonical ClassifierSchema per (classifier + feature-set) combination. ConcurrentHashMap
   * because schemas may be created by multiple threads during parallel deserialization.
   */
  private static final ConcurrentHashMap<SchemaKey, ClassifierSchema> REGISTRY =
      new ConcurrentHashMap<>();

  /**
   * Returns the canonical schema for the given classifier and ordered feature-key arrays, creating
   * and registering a new one if none exists yet.
   *
   * <p>The arrays are used <em>as-is</em> (not defensively copied); callers must not modify them
   * after passing them here. This avoids an extra allocation on the hot deserialization path.
   *
   * @param classifier the MetaPointer identifying the classifier; must not be null
   * @param propertyKeys ordered property MetaPointers for this node's feature set (may be empty)
   * @param containmentKeys ordered containment MetaPointers (may be empty)
   * @param referenceKeys ordered reference MetaPointers (may be empty)
   */
  public static ClassifierSchema get(
      @Nonnull MetaPointer classifier,
      @Nonnull MetaPointer[] propertyKeys,
      @Nonnull MetaPointer[] containmentKeys,
      @Nonnull MetaPointer[] referenceKeys) {
    SchemaKey key = new SchemaKey(classifier, propertyKeys, containmentKeys, referenceKeys);
    return REGISTRY.computeIfAbsent(key, ClassifierSchema::new);
  }

  // ---------------------------------------------------------------------------
  // Immutable state
  // ---------------------------------------------------------------------------

  /** The classifier this schema belongs to. */
  public final MetaPointer classifier;

  /**
   * Ordered property MetaPointers. Index {@code i} corresponds to {@code
   * SerializedClassifierInstance.propertyValues[i]}.
   */
  public final MetaPointer[] propertyKeys;

  /**
   * Ordered containment MetaPointers. Index {@code i} corresponds to {@code
   * SerializedClassifierInstance.containmentValues[i]}.
   */
  public final MetaPointer[] containmentKeys;

  /**
   * Ordered reference MetaPointers. Index {@code i} corresponds to {@code
   * SerializedClassifierInstance.referenceValues[i]}.
   */
  public final MetaPointer[] referenceKeys;

  private ClassifierSchema(@Nonnull SchemaKey key) {
    this.classifier = key.classifier;
    // The arrays were not defensively copied on the way in, so we take ownership here.
    this.propertyKeys = key.propKeys;
    this.containmentKeys = key.contKeys;
    this.referenceKeys = key.refKeys;
  }

  // ---------------------------------------------------------------------------
  // Convenience accessors
  // ---------------------------------------------------------------------------

  public int propertyCount() {
    return propertyKeys.length;
  }

  public int containmentCount() {
    return containmentKeys.length;
  }

  public int referenceCount() {
    return referenceKeys.length;
  }

  /**
   * Returns the index of {@code mp} in {@link #propertyKeys}, or -1 if not present.
   *
   * <p>Uses identity comparison ({@code ==}) because {@link MetaPointer} is interned; the linear
   * scan is deliberate — classifiers rarely have more than ~20 properties, so a HashMap's overhead
   * would cost more than it saves.
   */
  public int indexOfProperty(@Nonnull MetaPointer mp) {
    for (int i = 0; i < propertyKeys.length; i++) {
      if (propertyKeys[i] == mp) return i; // identity OK: MetaPointer is interned
    }
    return -1;
  }

  /**
   * Returns the index of {@code mp} in {@link #containmentKeys}, or -1 if not present.
   *
   * @see #indexOfProperty(MetaPointer)
   */
  public int indexOfContainment(@Nonnull MetaPointer mp) {
    for (int i = 0; i < containmentKeys.length; i++) {
      if (containmentKeys[i] == mp) return i;
    }
    return -1;
  }

  /**
   * Returns the index of {@code mp} in {@link #referenceKeys}, or -1 if not present.
   *
   * @see #indexOfProperty(MetaPointer)
   */
  public int indexOfReference(@Nonnull MetaPointer mp) {
    for (int i = 0; i < referenceKeys.length; i++) {
      if (referenceKeys[i] == mp) return i;
    }
    return -1;
  }

  // ---------------------------------------------------------------------------
  // Registry key
  // ---------------------------------------------------------------------------

  /**
   * Composite key for the schema registry.
   *
   * <p>Equality and hashing delegate to {@link Arrays#equals} / {@link Arrays#hashCode}, which in
   * turn call each element's own {@code equals}/{@code hashCode}. For {@link MetaPointer} those
   * resolve to {@code this == other} and {@code System.identityHashCode(this)} respectively — so
   * the standard array utilities give us element-identity semantics without any custom logic.
   */
  private static final class SchemaKey {

    final MetaPointer classifier;
    final MetaPointer[] propKeys;
    final MetaPointer[] contKeys;
    final MetaPointer[] refKeys;

    // Pre-computed to speed up concurrent registry lookups during large deserializations.
    private final int hashCode;

    SchemaKey(
        MetaPointer classifier,
        MetaPointer[] propKeys,
        MetaPointer[] contKeys,
        MetaPointer[] refKeys) {
      this.classifier = classifier;
      this.propKeys = propKeys;
      this.contKeys = contKeys;
      this.refKeys = refKeys;
      // Each MetaPointer.hashCode() is System.identityHashCode(), so this composite hash is
      // stable for the lifetime of the JVM instance (no address-based randomisation after intern).
      int h = System.identityHashCode(classifier);
      h = h * 31 + Arrays.hashCode(propKeys);
      h = h * 31 + Arrays.hashCode(contKeys);
      h = h * 31 + Arrays.hashCode(refKeys);
      this.hashCode = h;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof SchemaKey)) return false;
      SchemaKey other = (SchemaKey) o;
      // classifier uses MetaPointer identity (== is correct here)
      return classifier == other.classifier
          && Arrays.equals(propKeys, other.propKeys) // element-identity via MetaPointer.equals
          && Arrays.equals(contKeys, other.contKeys)
          && Arrays.equals(refKeys, other.refKeys);
    }

    @Override
    public int hashCode() {
      return hashCode;
    }
  }
}
