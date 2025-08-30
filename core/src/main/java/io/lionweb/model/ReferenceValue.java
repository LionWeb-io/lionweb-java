package io.lionweb.model;

import java.util.Objects;
import javax.annotation.Nullable;

public class ReferenceValue {
  private Node referred;
  private String resolveInfo;

  public ReferenceValue() {
    this(null, null);
  }

  public ReferenceValue(@Nullable Node referred, @Nullable String resolveInfo) {
    this.referred = referred;
    this.resolveInfo = resolveInfo;
  }

  public @Nullable Node getReferred() {
    return referred;
  }

  public @Nullable String getReferredID() {
    if (referred == null) {
      return null;
    }
    return referred.getID();
  }

  /**
   * Note that changing a ReferenceValue directly will not trigger notifications to observers of the
   * holding Node, if any. For this reason using this method is discouraged.
   */
  @Deprecated
  public void setReferred(@Nullable Node referred) {
    this.referred = referred;
  }

  public @Nullable String getResolveInfo() {
    return resolveInfo;
  }

  /**
   * Note that changing a ReferenceValue directly will not trigger notifications to observers of the
   * holding Node, if any. For this reason using this method is discouraged.
   */
  @Deprecated
  public void setResolveInfo(@Nullable String resolveInfo) {
    this.resolveInfo = resolveInfo;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ReferenceValue)) return false;
    ReferenceValue that = (ReferenceValue) o;
    return Objects.equals(referred, that.referred) && Objects.equals(resolveInfo, that.resolveInfo);
  }

  @Override
  public int hashCode() {
    return Objects.hash(referred, resolveInfo);
  }

  @Override
  public String toString() {
    return "ReferenceValue{"
        + "referred="
        + (referred == null ? "null" : referred.getID())
        + ", resolveInfo='"
        + resolveInfo
        + '\''
        + '}';
  }

  /**
   * Creates a new ReferenceValue instance with the given referred Node while retaining the current
   * resolveInfo.
   *
   * @param referredNode the Node to be referred to by the new ReferenceValue. Can be null.
   * @return a new ReferenceValue object with the specified referred Node and the current
   *     resolveInfo.
   */
  public ReferenceValue withReferred(@Nullable Node referredNode) {
    return new ReferenceValue(referredNode, this.resolveInfo);
  }

  /**
   * Creates a new ReferenceValue instance with the given resolve information while retaining the
   * current referred Node.
   *
   * @param resolveInfo the resolve information to be associated with the new ReferenceValue. Can be
   *     null.
   * @return a new ReferenceValue object with the specified resolve information and the current
   *     referred Node.
   */
  public ReferenceValue withResolveInfo(@Nullable String resolveInfo) {
    return new ReferenceValue(this.referred, resolveInfo);
  }
}
