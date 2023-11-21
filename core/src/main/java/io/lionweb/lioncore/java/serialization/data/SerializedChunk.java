package io.lionweb.lioncore.java.serialization.data;

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

  public Map<String, SerializedClassifierInstance> getClassifierInstancesByID() {
    return Collections.unmodifiableMap(classifierInstancesByID);
  }

  public List<UsedLanguage> getLanguages() {
    return Collections.unmodifiableList(languages);
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
    return serializationFormatVersion.equals(that.serializationFormatVersion)
        && languages.equals(that.languages)
        && classifierInstances.equals(that.classifierInstances);
  }

  @Override
  public int hashCode() {
    return Objects.hash(serializationFormatVersion, languages, classifierInstances);
  }
}
