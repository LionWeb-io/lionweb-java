package io.lionweb.lioncore.java.model;

import io.lionweb.lioncore.java.versions.LionWebVersionDependent;
import io.lionweb.lioncore.java.versions.LionWebVersionToken;

import java.util.Objects;
import javax.annotation.Nullable;

public class ReferenceValue<V extends LionWebVersionToken> implements LionWebVersionDependent<V> {
  private Node<V> referred;
  private String resolveInfo;

  public ReferenceValue() {
    this(null, null);
  }

  public ReferenceValue(@Nullable Node<V> referred, @Nullable String resolveInfo) {
    setReferred(referred);
    setResolveInfo(resolveInfo);
  }

  public @Nullable Node<V> getReferred() {
    return referred;
  }

  public @Nullable String getReferredID() {
    if (referred == null) {
      return null;
    }
    return referred.getID();
  }

  public void setReferred(@Nullable Node<V> referred) {
    this.referred = referred;
  }

  public @Nullable String getResolveInfo() {
    return resolveInfo;
  }

  public void setResolveInfo(@Nullable String resolveInfo) {
    this.resolveInfo = resolveInfo;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ReferenceValue)) return false;
    ReferenceValue<V> that = (ReferenceValue<V>) o;
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
}
