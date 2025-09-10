package io.lionweb.serialization.data;

import java.util.*;
import javax.annotation.Nonnull;

/** This represents the serialization of the values of a containment link in a Node. */
public class SerializedContainmentValue {
  private final MetaPointer metaPointer;
  private final List<String> childrenIds;

  /**
   * Constructs a SerializedContainmentValue instance with the specified MetaPointer and a list of
   * string values.
   *
   * @param metaPointer the MetaPointer associated with this containment childrenIds; may be null
   * @param childrenIds a non-null list of string values representing Node-IDs; must not be null
   * @throws NullPointerException if the childrenIds parameter is null
   */
  public SerializedContainmentValue(MetaPointer metaPointer, @Nonnull List<String> childrenIds) {
    Objects.requireNonNull(childrenIds, "childrenIds must not be null");
    this.metaPointer = metaPointer;
    this.childrenIds = new ArrayList<>(childrenIds);
  }

  /**
   * Constructs a SerializedContainmentValue instance with the specified MetaPointer and an array of
   * values.
   *
   * @param metaPointer the MetaPointer associated with this containment value; must not be null
   * @param values an optional array of string values representing Node-IDs; may be empty
   */
  public SerializedContainmentValue(MetaPointer metaPointer, String... values) {
    this.metaPointer = metaPointer;
    this.childrenIds = Arrays.asList(values);
  }

  public MetaPointer getMetaPointer() {
    return metaPointer;
  }

  /** This returns the list of Node-IDs contained. */
  public List<String> getChildrenIds() {
    return Collections.unmodifiableList(childrenIds);
  }

  /** This expects the list of Node-IDs contained. */
  public void setChildrenIds(List<String> childrenIds) {
    this.childrenIds.clear();
    this.childrenIds.addAll(childrenIds);
  }

  /**
   * Removes a childId from the list of Node-IDs contained.
   *
   * @param childId the identifier of the childId to be removed; must not be null
   * @return true if the childId was successfully removed, false otherwise
   * @throws NullPointerException if the childId is null
   */
  public boolean removeChild(@Nonnull String childId) {
    Objects.requireNonNull(childId, "childId must not be null");
    return childrenIds.remove(childId);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof SerializedContainmentValue)) return false;
    SerializedContainmentValue that = (SerializedContainmentValue) o;
    return Objects.equals(metaPointer, that.metaPointer)
        && Objects.equals(childrenIds, that.childrenIds);
  }

  @Override
  public int hashCode() {
    return Objects.hash(metaPointer, childrenIds);
  }

  @Override
  public String toString() {
    return "SerializedContainmentValue{"
        + "metaPointer="
        + metaPointer
        + ", value="
        + childrenIds
        + '}';
  }
}
