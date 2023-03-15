package org.lionweb.lioncore.java.serialization.data;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Lower level representation of Node which is used to load broken nodes during serialization.
 */
public class SerializedNode {
    private String id;
    private String conceptId;
    private String parentNodeID;
    private Map<String, String> propertyValues = new HashMap<>();
    private Map<String, List<String>> containmentsValues = new HashMap<>();
    private Map<String, List<RawReferenceValue>> referencesValues = new HashMap<>();

    public String getParentNodeID() {
        return parentNodeID;
    }

    public void setParentNodeID(String parentNodeID) {
        this.parentNodeID = parentNodeID;
    }

    public List<String> getChildren() {
        List<String> children = new ArrayList<>();
        for (List<String> ch : containmentsValues.values()) {
            children.addAll(ch);
        }
        return children;
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

    public SerializedNode(String id, String conceptId) {
        setID(id);
        setConceptID(conceptId);
    }

    public String getConceptID() {
        return conceptId;
    }

    public void setConceptID(String conceptId) {
        this.conceptId = conceptId;
    }

    @Nullable
    public String getID() {
        return id;
    }


    public void setID(String id) {
        this.id = id;
    }

    public void setPropertyValue(String propertyId, String serializedValue) {
        this.propertyValues.put(propertyId, serializedValue);
    }

    public void addChild(String containmentID, String childId) {
        this.containmentsValues.computeIfAbsent(containmentID, s -> new ArrayList<>()).add(childId);
    }

    public void addReferenceValue(String referenceID, RawReferenceValue referenceValue) {
        this.referencesValues.computeIfAbsent(referenceID, s -> new ArrayList<>()).add(referenceValue);
    }

    @Nullable
    public String getPropertyValue(String propertyId) {
        return this.propertyValues.get(propertyId);
    }

    @Nullable
    public List<RawReferenceValue> getReferenceValues(String referenceID) {
        return this.referencesValues.getOrDefault(referenceID, new ArrayList<>());
    }
}
