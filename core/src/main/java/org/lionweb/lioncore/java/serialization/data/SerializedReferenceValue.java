package org.lionweb.lioncore.java.serialization.data;

import org.lionweb.lioncore.java.model.ReferenceValue;

import java.util.List;

public class SerializedReferenceValue {
    private MetaPointer metaPointer;
    private List<ReferenceValue> value;

    public MetaPointer getMetaPointer() {
        return metaPointer;
    }

    public void setMetaPointer(MetaPointer metaPointer) {
        this.metaPointer = metaPointer;
    }

    public List<ReferenceValue> getValue() {
        return value;
    }

    public void setValue(List<ReferenceValue> value) {
        this.value = value;
    }
}
