package org.lionweb.lioncore.java.serialization.data;

import java.util.List;
import java.util.Objects;

public class SerializedContainmentValue {
    private MetaPointer metaPointer;
    private List<String> value;

    public MetaPointer getMetaPointer() {
        return metaPointer;
    }

    public void setMetaPointer(MetaPointer metaPointer) {
        this.metaPointer = metaPointer;
    }

    public List<String> getValue() {
        return value;
    }

    public void setValue(List<String> value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SerializedContainmentValue)) return false;
        SerializedContainmentValue that = (SerializedContainmentValue) o;
        return Objects.equals(metaPointer, that.metaPointer) && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(metaPointer, value);
    }

    @Override
    public String toString() {
        return "SerializedContainmentValue{" +
                "metaPointer=" + metaPointer +
                ", value=" + value +
                '}';
    }
}
