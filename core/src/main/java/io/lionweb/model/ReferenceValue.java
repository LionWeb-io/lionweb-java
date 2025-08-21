package io.lionweb.model;

import java.util.Objects;
import javax.annotation.Nullable;

public class ReferenceValue {
  private Node referred;
  private String resolveInfo;
  private ClassifierInstance<?> owner;

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

  // TODO for the ReferenceValue to trigger the event, the ReferenceValue must known where it is hold
    // perhaps we could make ReferenceValue immutable and force mutations to happen throw the holding ClassifierInstance
  public void setReferred(@Nullable Node referred) {
      if (owner != null) {
          PartitionObserver observer = owner.registeredPartitionObserver();
          if (observer != null) {
              observer.referredIDChanged(
                      this,
                      this.referred == null ? null : this.referred.getID(),
                      referred == null ? null : referred.getID());
          }
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

    public ClassifierInstance<?> getOwner() {
        return owner;
    }

    public void setOwner(ClassifierInstance<?> owner) {
        this.owner = owner;
    }
}
