package org.lionweb.lioncore.java.serialization.data;

import java.util.Objects;

public class RawReferenceValue {
  public String referredId;
  public String resolveInfo;

  public String getReferredId() {
    return referredId;
  }

  public void setReferredId(String referredId) {
    this.referredId = referredId;
  }

  public String getResolveInfo() {
    return resolveInfo;
  }

  public void setResolveInfo(String resolveInfo) {
    this.resolveInfo = resolveInfo;
  }

  public RawReferenceValue(String referredId, String resolveInfo) {
    this.referredId = referredId;
    this.resolveInfo = resolveInfo;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof RawReferenceValue)) return false;
    RawReferenceValue that = (RawReferenceValue) o;
    return Objects.equals(referredId, that.referredId)
        && Objects.equals(resolveInfo, that.resolveInfo);
  }

  @Override
  public int hashCode() {
    return Objects.hash(referredId, resolveInfo);
  }
}
