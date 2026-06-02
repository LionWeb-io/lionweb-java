package io.lionweb.serialization.data;

import java.util.Objects;
import javax.annotation.Nullable;

/**
 * A raw, unresolved reference value as it appears in a serialized LionWeb payload — carrying only a
 * {@code referredId} string and optional {@code resolveInfo}, before the referred node has been
 * looked up in the deserialized model.
 */
public class RawReferenceValue {
  public String referredId;
  public String resolveInfo;

  public @Nullable String getReferredId() {
    return referredId;
  }

  public void setReferredId(@Nullable String referredId) {
    this.referredId = referredId;
  }

  public @Nullable String getResolveInfo() {
    return resolveInfo;
  }

  public void setResolveInfo(@Nullable String resolveInfo) {
    this.resolveInfo = resolveInfo;
  }

  public RawReferenceValue(@Nullable String referredId, @Nullable String resolveInfo) {
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
