package org.lionweb.lioncore.java.serialization.data;

import org.lionweb.lioncore.java.metamodel.Concept;
import org.lionweb.lioncore.java.metamodel.MetamodelElement;

import java.util.Objects;

public class MetaPointer {
    private String key;
    private String version;
    private String metamodel;

    public static MetaPointer from(MetamodelElement<?> metamodelElement) {
        MetaPointer metaPointer = new MetaPointer();
        metaPointer.setKey(metamodelElement.getKey());
        if (metamodelElement.getMetamodel() != null) {
            metaPointer.setMetamodel(metamodelElement.getMetamodel().getKey());
            if (metamodelElement.getMetamodel().getVersion() != null) {
                metaPointer.setVersion(Integer.toString(metamodelElement.getMetamodel().getVersion()));
            }
        }
        return metaPointer;
    }

    public String getMetamodel() {
        return metamodel;
    }

    public void setMetamodel(String metamodel) {
        this.metamodel = metamodel;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MetaPointer)) return false;
        MetaPointer that = (MetaPointer) o;
        return Objects.equals(key, that.key) && Objects.equals(version, that.version) && Objects.equals(metamodel, that.metamodel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, version, metamodel);
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "MetaPointer{" +
                "key='" + key + '\'' +
                ", version='" + version + '\'' +
                ", metamodel='" + metamodel + '\'' +
                '}';
    }
}
