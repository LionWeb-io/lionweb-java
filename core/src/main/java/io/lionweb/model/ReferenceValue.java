package io.lionweb.model;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ReferenceValue {
  private Node referred;
  private String resolveInfo;

  public ReferenceValue() {
    this(null, null);
  }

  public ReferenceValue(@Nullable Node referred, @Nullable String resolveInfo) {
    setReferred(referred);
    setResolveInfo(resolveInfo);
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

  public void setReferred(@Nullable Node referred) {
    if (observer != null) {
      observer.referredIDChanged(this, this.referred.getID(), referred.getID());
    }
    this.referred = referred;
  }

  public @Nullable String getResolveInfo() {
    return resolveInfo;
  }

  public void setResolveInfo(@Nullable String resolveInfo) {
    if (observer != null) {
      observer.resolveInfoChanged(this, this.resolveInfo, resolveInfo);
    }
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

  public void addObserver(@Nonnull ReferenceValueObserver observer) {
    if (this.observer != null) {
      throw new UnsupportedOperationException();
    }
    this.observer = observer;
  }

  private @Nullable ReferenceValueObserver observer = null;
}
