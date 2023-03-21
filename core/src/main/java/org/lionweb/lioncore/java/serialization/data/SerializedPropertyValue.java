package org.lionweb.lioncore.java.serialization.data;

import java.util.Objects;

public class SerializedPropertyValue {
    private MetaPointer metaPointer;
    private String value;

    public SerializedPropertyValue() {

    }

    public SerializedPropertyValue(MetaPointer metaPointer, String value) {
        this.metaPointer = metaPointer;
        this.value = value;
    }

    public MetaPointer getMetaPointer() {
        return metaPointer;
    }

    public void setMetaPointer(MetaPointer metaPointer) {
        this.metaPointer = metaPointer;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "SerializedPropertyValue{" +
                "metaPointer=" + metaPointer +
                ", value='" + value + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SerializedPropertyValue)) return false;
        SerializedPropertyValue that = (SerializedPropertyValue) o;
        return Objects.equals(metaPointer, that.metaPointer) && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(metaPointer, value);
    }
}
