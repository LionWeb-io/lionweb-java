package org.lionweb.lioncore.java.serialization.data;

public class SerializedPropertyValue {
    private MetaPointer metaPointer;
    private String value;

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
}
