package org.lionweb.lioncore.java.serialization.data;

import org.checkerframework.checker.units.qual.A;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Lower level representation of Node which is used to load broken nodes during serialization.
 */
public class SerializedNode {
    private String id;
    private MetaPointer concept;
    private String parentNodeID;

    private List<SerializedPropertyValue> properties = new ArrayList<>();
    private List<SerializedContainmentValue> containments = new ArrayList<>();
    private List<SerializedReferenceValue> references = new ArrayList<>();

    public String getParentNodeID() {
        return parentNodeID;
    }

    public void setParentNodeID(String parentNodeID) {
        this.parentNodeID = parentNodeID;
    }

    public List<String> getChildren() {
//        List<String> children = new ArrayList<>();
//        for (List<String> ch : containmentsValues.values()) {
//            children.addAll(ch);
//        }
//        return children;
        throw new UnsupportedOperationException();
    }

    public List<SerializedPropertyValue> getProperties() {
        return properties;
    }

    public static class RawReferenceValue {
        public String referredId;
        public String resolveInfo;

        public String getReferredId() {
            return referredId;
        }

        public void setReferredId(String referredId) {
            this.referredId = referredId;
        }

        public String getResolveInfo() {
            return resolveInfo;
        }

        public void setResolveInfo(String resolveInfo) {
            this.resolveInfo = resolveInfo;
        }

        public RawReferenceValue(String referredId, String resolveInfo) {
            this.referredId = referredId;
            this.resolveInfo = resolveInfo;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof RawReferenceValue)) return false;
            RawReferenceValue that = (RawReferenceValue) o;
            return Objects.equals(referredId, that.referredId) && Objects.equals(resolveInfo, that.resolveInfo);
        }

        @Override
        public int hashCode() {
            return Objects.hash(referredId, resolveInfo);
        }
    }

    public SerializedNode() {

    }

    public SerializedNode(String id, MetaPointer concept) {
        setID(id);
        setConcept(concept);
    }

    public MetaPointer getConcept() {
        return concept;
    }

    public void setConcept(MetaPointer concept) {
        this.concept = concept;
    }

    @Nullable
    public String getID() {
        return id;
    }


    public void setID(String id) {
        this.id = id;
    }

    public void setPropertyValue(String propertyId, String serializedValue) {
        //this.propertyValues.put(propertyId, serializedValue);
        throw new UnsupportedOperationException();
    }

    public void addChild(String containmentID, String childId) {
        //this.containmentsValues.computeIfAbsent(containmentID, s -> new ArrayList<>()).add(childId);
        throw new UnsupportedOperationException();
    }

    public void addReferenceValue(String referenceID, RawReferenceValue referenceValue) {
        //this.referencesValues.computeIfAbsent(referenceID, s -> new ArrayList<>()).add(referenceValue);
        throw new UnsupportedOperationException();
    }

    @Nullable
    public String getPropertyValue(String propertyId) {
        //return this.propertyValues.get(propertyId);
        throw new UnsupportedOperationException();
    }

    @Nullable
    public List<RawReferenceValue> getReferenceValues(String referenceID) {
        //return this.referencesValues.getOrDefault(referenceID, new ArrayList<>());
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SerializedNode)) return false;
        SerializedNode that = (SerializedNode) o;
        return Objects.equals(id, that.id) && Objects.equals(concept, that.concept) && Objects.equals(parentNodeID, that.parentNodeID) && Objects.equals(properties, that.properties) && Objects.equals(containments, that.containments) && Objects.equals(references, that.references);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, concept, parentNodeID, properties, containments, references);
    }

    @Override
    public String toString() {
        return "SerializedNode{" +
                "id='" + id + '\'' +
                ", concept=" + concept +
                ", parentNodeID='" + parentNodeID + '\'' +
                ", properties=" + properties +
                ", containments=" + containments +
                ", references=" + references +
                '}';
    }
}
