package org.lionweb.lioncore.java.serialization.data;

import java.util.List;

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
}
