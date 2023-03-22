package org.lionweb.lioncore.java.serialization.data;

import org.lionweb.lioncore.java.metamodel.HasKey;
import org.lionweb.lioncore.java.metamodel.Metamodel;
import org.lionweb.lioncore.java.metamodel.MetamodelElement;

import java.util.Objects;

/**
 * A MetaPointer is the combination of the pair Metamodel and Version with a Key, which identify one element within
 * that metamodel.
 */
public class MetaPointer {
    private String key;
    private String version;
    private String metamodel;

    public MetaPointer(String metamodel, String version, String key) {
        this.key = key;
        this.version = version;
        this.metamodel = metamodel;
    }

    public MetaPointer() {

    }

    public static MetaPointer from(MetamodelElement<?> metamodelElement) {
        MetaPointer metaPointer = new MetaPointer();
        metaPointer.setKey(metamodelElement.getKey());
        if (metamodelElement.getMetamodel() != null) {
            metaPointer.setMetamodel(metamodelElement.getMetamodel().getKey());
            if (metamodelElement.getMetamodel().getVersion() != null) {
                metaPointer.setVersion(metamodelElement.getMetamodel().getVersion());
            }
        }
        return metaPointer;
    }

    public static MetaPointer from(HasKey<?> elementWithKey, Metamodel metamodel) {
        MetaPointer metaPointer = new MetaPointer();
        metaPointer.setKey(elementWithKey.getKey());
        if (metamodel != null) {
            metaPointer.setMetamodel(metamodel.getKey());
            if (metamodel.getVersion() != null) {
                metaPointer.setVersion(metamodel.getVersion());
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
