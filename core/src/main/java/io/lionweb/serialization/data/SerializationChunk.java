package io.lionweb.serialization.data;

import io.lionweb.LionWebVersion;
import java.util.*;
import javax.annotation.Nonnull;

/**
 * This represents a chunk of nodes which have been serialized. The serialization could be
 * inconsistent. This is a low-level representation, intended to represent broken chunks or as an
 * intermediate step during serialization or deserialization.
 */
public class SerializationChunk {

  private final Map<String, SerializedClassifierInstance> classifierInstancesByID;

  private String serializationFormatVersion;
  private final List<LanguageVersion> languages = new ArrayList<>();

  /**
   * Shadow set for O(1) duplicate detection. Uses identity equality since LanguageVersion is
   * interned.
   */
  private final Set<LanguageVersion> languagesSet =
      Collections.newSetFromMap(new IdentityHashMap<>());

  private final List<SerializedClassifierInstance> classifierInstances;

  // Stable unmodifiable views — created once, always reflect current list contents.
  private final List<SerializedClassifierInstance> classifierInstancesView;
  private final List<LanguageVersion> languagesView;

  public SerializationChunk() {
    this(16);
  }

  public SerializationChunk(int initialCapacity) {
    this.classifierInstancesByID = new HashMap<>(initialCapacity);
    this.classifierInstances = new ArrayList<>(initialCapacity);
    this.classifierInstancesView = Collections.unmodifiableList(classifierInstances);
    this.languagesView = Collections.unmodifiableList(languages);
  }

  public static SerializationChunk fromNodes(
      @Nonnull LionWebVersion lionWebVersion, @Nonnull List<SerializedClassifierInstance> nodes) {
    Objects.requireNonNull(lionWebVersion);
    Objects.requireNonNull(nodes);
    if (nodes.isEmpty()) {
      throw new IllegalArgumentException();
    }
    SerializationChunk instance = new SerializationChunk();
    instance.setSerializationFormatVersion(lionWebVersion.getVersionString());
    nodes.forEach(n -> instance.addClassifierInstance(n));
    // populateUsedLanguages() is no needed here: addClassifierInstance() registers
    // each instance's languages incrementally as it is added.
    return instance;
  }

  public void setSerializationFormatVersion(String value) {
    this.serializationFormatVersion = value;
  }

  public String getSerializationFormatVersion() {
    return serializationFormatVersion;
  }

  public List<SerializedClassifierInstance> getClassifierInstances() {
    return classifierInstancesView;
  }

  /**
   * Adds a single {@link SerializedClassifierInstance} to the current SerializationChunk.
   *
   * @param instance the {@code SerializedClassifierInstance} to add; must not be null
   * @throws NullPointerException if {@code instance} is null
   */
  public void addClassifierInstance(@Nonnull SerializedClassifierInstance instance) {
    Objects.requireNonNull(instance, "instance should not be null");
    this.classifierInstancesByID.put(instance.getID(), instance);
    classifierInstances.add(instance);
    considerMetaPointers(instance);
  }

  /**
   * Adds multiple classifier instances to the current SerializationChunk.
   *
   * @param instances an array of SerializedClassifierInstance objects to be added
   */
  public void addClassifierInstances(@Nonnull SerializedClassifierInstance... instances) {
    Arrays.stream(instances).forEach(this::addClassifierInstance);
  }

  @Nonnull
  public SerializedClassifierInstance getInstanceByID(String instanceID) {
    SerializedClassifierInstance instance = this.classifierInstancesByID.get(instanceID);
    if (instance == null) {
      throw new IllegalArgumentException("Cannot find instance with ID " + instanceID);
    }
    return instance;
  }

  /**
   * Adds a language to the current SerializationChunk.
   *
   * @param language the {@code UsedLanguage} instance to add; must not be null
   * @throws NullPointerException if {@code language} is null
   */
  public void addLanguage(@Nonnull LanguageVersion language) {
    Objects.requireNonNull(language, "language should not be null");
    if (languagesSet.add(language)) {
      this.languages.add(language);
    }
  }

  /**
   * Adds multiple {@link LanguageVersion} instances to the current SerializationChunk.
   *
   * @param languages an array of {@code UsedLanguage} instances to be added; must not be null
   * @throws NullPointerException if any element in {@code languages} is null
   */
  public void addLanguages(@Nonnull LanguageVersion... languages) {
    for (LanguageVersion language : languages) {
      addLanguage(language);
    }
  }

  public Map<String, SerializedClassifierInstance> getClassifierInstancesByID() {
    return Collections.unmodifiableMap(classifierInstancesByID);
  }

  public List<LanguageVersion> getLanguages() {
    return languagesView;
  }

  public void concat(List<SerializedClassifierInstance> instances) {
    this.classifierInstances.addAll(instances);
  }

  /**
   * Traverse the SerializationChunk, collecting all the metapointers and populating the used
   * languages accordingly.
   *
   * <p>When instances are added via {@link #addClassifierInstance}, this is done incrementally and
   * this method is effectively a no-op (all languages are already registered). It remains available
   * for chunks built by directly manipulating the backing list (e.g. via {@link #concat}) or for
   * external callers that bypassed the incremental path.
   */
  public void populateUsedLanguages() {
    for (SerializedClassifierInstance classifierInstance : classifierInstances) {
      considerMetaPointers(classifierInstance);
    }
  }

  @Override
  public String toString() {
    return "SerializationBlock{"
        + ", serializationFormatVersion='"
        + serializationFormatVersion
        + '\''
        + ", languages="
        + languages
        + ", classifierInstances="
        + classifierInstances
        + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof SerializationChunk)) return false;
    SerializationChunk that = (SerializationChunk) o;
    return Objects.equals(serializationFormatVersion, that.serializationFormatVersion)
        && Objects.equals(languages, that.languages)
        && Objects.equals(classifierInstances, that.classifierInstances);
  }

  @Override
  public int hashCode() {
    return Objects.hash(serializationFormatVersion, languages, classifierInstances);
  }

  private void considerMetaPointers(SerializedClassifierInstance instance) {
    considerMetaPointer(instance.getClassifier());
    for (SerializedPropertyValue pv : instance.getProperties()) {
      considerMetaPointer(pv.getMetaPointer());
    }
    for (SerializedContainmentValue cv : instance.getContainments()) {
      considerMetaPointer(cv.getMetaPointer());
    }
    for (SerializedReferenceValue rv : instance.getReferences()) {
      considerMetaPointer(rv.getMetaPointer());
    }
  }

  private void considerMetaPointer(MetaPointer metaPointer) {
    if (metaPointer == null
        || metaPointer.getLanguage() == null
        || metaPointer.getVersion() == null) {
      // Incomplete/malformed metapointer — skip silently. Downstream deserialization
      // will raise a proper error when the node is actually processed.
      return;
    }
    LanguageVersion languageVersion = LanguageVersion.fromMetaPointer(metaPointer);
    if (languagesSet.add(languageVersion)) {
      languages.add(languageVersion);
    }
  }
}
