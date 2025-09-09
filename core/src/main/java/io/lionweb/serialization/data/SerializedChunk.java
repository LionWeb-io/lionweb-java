package io.lionweb.serialization.data;

import io.lionweb.LionWebVersion;
import java.util.*;
import javax.annotation.Nonnull;

/**
 * This represents a chunk of nodes which have been serialized. The serialization could be
 * inconsistent. This is a low-level representation, intended to represent broken chunks or as an
 * intermediate step during serialization or deserialization.
 */
public class SerializedChunk {

  private final Map<String, SerializedClassifierInstance> classifierInstancesByID = new HashMap<>();

  private String serializationFormatVersion;
  private final List<UsedLanguage> languages = new ArrayList<>();
  private final List<SerializedClassifierInstance> classifierInstances = new ArrayList<>();

  public static SerializedChunk fromNodes(
      @Nonnull LionWebVersion lionWebVersion, @Nonnull List<SerializedClassifierInstance> nodes) {
    Objects.requireNonNull(lionWebVersion);
    Objects.requireNonNull(nodes);
    if (nodes.isEmpty()) {
      throw new IllegalArgumentException();
    }
    SerializedChunk instance = new SerializedChunk();
    instance.setSerializationFormatVersion(lionWebVersion.getVersionString());
    nodes.forEach(n -> instance.addClassifierInstance(n));
    instance.populateUsedLanguages();
    return instance;
  }

  public void setSerializationFormatVersion(String value) {
    this.serializationFormatVersion = value;
  }

  public String getSerializationFormatVersion() {
    return serializationFormatVersion;
  }

  public List<SerializedClassifierInstance> getClassifierInstances() {
    return Collections.unmodifiableList(classifierInstances);
  }

  public void addClassifierInstance(SerializedClassifierInstance instance) {
    this.classifierInstancesByID.put(instance.getID(), instance);
    classifierInstances.add(instance);
  }

  public void addClassifierInstances(SerializedClassifierInstance... instances) {
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

  public void addLanguage(UsedLanguage language) {
    this.languages.add(language);
  }

  public void addLanguages(UsedLanguage... languages) {
    for (UsedLanguage language : languages) {
      addLanguage(language);
    }
  }

  public Map<String, SerializedClassifierInstance> getClassifierInstancesByID() {
    return Collections.unmodifiableMap(classifierInstancesByID);
  }

  public List<UsedLanguage> getLanguages() {
    return Collections.unmodifiableList(languages);
  }

  /**
   * Traverse the SerializedChunk, collecting all the metapointers and populating the used languages
   * accordingly.
   */
  public void populateUsedLanguages() {
    for (SerializedClassifierInstance classifierInstance : classifierInstances) {
      considerMetaPointer(classifierInstance.getClassifier());
      for (SerializedContainmentValue containmentValue : classifierInstance.getContainments()) {
        considerMetaPointer(containmentValue.getMetaPointer());
      }
      for (SerializedReferenceValue referenceValue : classifierInstance.getReferences()) {
        considerMetaPointer(referenceValue.getMetaPointer());
      }
      for (SerializedPropertyValue propertyValue : classifierInstance.getProperties()) {
        considerMetaPointer(propertyValue.getMetaPointer());
      }
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
    if (!(o instanceof SerializedChunk)) return false;
    SerializedChunk that = (SerializedChunk) o;
    return Objects.equals(serializationFormatVersion, that.serializationFormatVersion)
        && Objects.equals(languages, that.languages)
        && Objects.equals(classifierInstances, that.classifierInstances);
  }

  @Override
  public int hashCode() {
    return Objects.hash(serializationFormatVersion, languages, classifierInstances);
  }

  private void considerMetaPointer(MetaPointer metaPointer) {
    UsedLanguage usedLanguage = UsedLanguage.fromMetaPointer(metaPointer);
    if (!languages.contains(usedLanguage)) {
      languages.add(usedLanguage);
    }
  }
}
