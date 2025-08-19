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
      observer.referredIDChanged(
          this,
          this.referred == null ? null : this.referred.getID(),
          referred == null ? null : referred.getID());
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

  public void registerObserver(@Nullable ReferenceValueObserver observer) {
    if (this.observer == observer) {
      throw new IllegalArgumentException("Observer already registered: " + observer);
    }
    if (this.observer == null) {
      this.observer = observer;
    } else {
      this.observer = CompositeReferenceValueObserver.combine(this.observer, observer);
    }
  }

  public void unregisterObserver(@Nonnull ReferenceValueObserver observer) {
    if (this.observer == observer) {
      this.observer = null;
      return;
    }
    if (this.observer instanceof CompositeReferenceValueObserver) {
      this.observer = ((CompositeReferenceValueObserver) this.observer).remove(observer);
    } else {
      throw new IllegalArgumentException("Observer not registered: " + observer);
    }
  }

  /**
   * In most cases we will have no observers or one observer, shared across many nodes, so we avoid
   * instantiating lists. We Represent multiple observers with a CompositeObserver instead.
   */
  protected @Nullable ReferenceValueObserver observer = null;
}
