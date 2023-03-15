package org.lionweb.lioncore.java.serialization.data;

import org.lionweb.lioncore.java.model.ReferenceValue;

import java.util.List;
import java.util.Objects;

public class SerializedReferenceValue {

    public static class Entry {
        private String resolveInfo;
        private String reference;

        public Entry() {
        }

        public Entry(String reference, String resolveInfo) {
            this.resolveInfo = resolveInfo;
            this.reference = reference;
        }

        public String getResolveInfo() {
            return resolveInfo;
        }

        public void setResolveInfo(String resolveInfo) {
            this.resolveInfo = resolveInfo;
        }

        public String getReference() {
            return reference;
        }

        public void setReference(String reference) {
            this.reference = reference;
        }

        @Override
        public String toString() {
            return "Entry{" +
                    "resolveInfo='" + resolveInfo + '\'' +
                    ", reference='" + reference + '\'' +
                    '}';
        }
    }

    private MetaPointer metaPointer;
    private List<Entry> value;

    public MetaPointer getMetaPointer() {
        return metaPointer;
    }

    public void setMetaPointer(MetaPointer metaPointer) {
        this.metaPointer = metaPointer;
    }

    public List<Entry> getValue() {
        return value;
    }

    public void setValue(List<Entry> value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SerializedReferenceValue)) return false;
        SerializedReferenceValue that = (SerializedReferenceValue) o;
        return Objects.equals(metaPointer, that.metaPointer) && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(metaPointer, value);
    }

    @Override
    public String toString() {
        return "SerializedReferenceValue{" +
                "metaPointer=" + metaPointer +
                ", value=" + value +
                '}';
    }
}
