package org.lionweb.lioncore.java.model;

import javax.annotation.Nullable;
import java.util.Objects;

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

    public void setReferred(@Nullable Node referred) {
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
        if (!(o instanceof ReferenceValue that)) return false;
        return Objects.equals(referred, that.referred) && Objects.equals(resolveInfo, that.resolveInfo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(referred, resolveInfo);
    }
}
