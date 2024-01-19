package io.lionweb.json.sorted;

import io.lionweb.lioncore.java.serialization.data.*;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SortedSerializedClassifierInstance extends SerializedClassifierInstance implements Comparable<SerializedClassifierInstance> {
    private final SerializedClassifierInstance delegate;

    public SortedSerializedClassifierInstance(SerializedClassifierInstance delegate) {
        this.delegate = delegate;
    }

    @Override
    public List<SerializedContainmentValue> getContainments() {
        return delegate.getContainments().stream().map(new Function<SerializedContainmentValue, SortedSerializedContainmentValue>() {
            public SortedSerializedContainmentValue apply(SerializedContainmentValue it) {
                return new SortedSerializedContainmentValue(it);
            }
        }).sorted().collect(Collectors.<SerializedContainmentValue>toList());
    }

    @Override
    public List<String> getChildren() {
        List<String> result = delegate.getChildren();
        result.sort(Comparator.nullsLast(Comparator.naturalOrder()));
        return result;
    }

    @Override
    public List<SerializedReferenceValue> getReferences() {
        return delegate.getReferences().stream().map(new Function<SerializedReferenceValue, SortedSerializedReferenceValue>() {
            public SortedSerializedReferenceValue apply(SerializedReferenceValue it) {
                return new SortedSerializedReferenceValue(it);
            }
        }).sorted().collect(Collectors.<SerializedReferenceValue>toList());
    }

    @Override
    public List<SerializedPropertyValue> getProperties() {
        return delegate.getProperties().stream().map(new Function<SerializedPropertyValue, SortedSerializedPropertyValue>() {
            public SortedSerializedPropertyValue apply(SerializedPropertyValue it) {
                return new SortedSerializedPropertyValue(it);
            }
        }).sorted().collect(Collectors.<SerializedPropertyValue>toList());
    }

    @Override
    public List<SerializedReferenceValue.Entry> getReferenceValues(String referenceKey) {
        return delegate.getReferenceValues(referenceKey).stream().map(new Function<SerializedReferenceValue.Entry, SortedSerializedReferenceValue.Entry>() {
            public SortedSerializedReferenceValue.Entry apply(SerializedReferenceValue.Entry it) {
                return new SortedSerializedReferenceValue.Entry(it);
            }
        }).sorted().collect(Collectors.<SerializedReferenceValue.Entry>toList());
    }


    @Override
    public int compareTo(SerializedClassifierInstance other) {
        return Objects.compare(this.getID(), other.getID(), Comparator.nullsLast(Comparator.naturalOrder()));
    }

    @Override
    public String getParentNodeID() {
        return delegate.getParentNodeID();
    }

    @Override
    public void setParentNodeID(String parentNodeID) {
        delegate.setParentNodeID(parentNodeID);
    }

    @Override
    public void addPropertyValue(SerializedPropertyValue propertyValue) {
        delegate.addPropertyValue(propertyValue);
    }

    @Override
    public void addContainmentValue(SerializedContainmentValue containmentValue) {
        delegate.addContainmentValue(containmentValue);
    }

    @Override
    public void addReferenceValue(SerializedReferenceValue referenceValue) {
        delegate.addReferenceValue(referenceValue);
    }

    @Override
    public List<String> getAnnotations() {
        return delegate.getAnnotations();
    }

    @Override
    public MetaPointer getClassifier() {
        return delegate.getClassifier();
    }

    @Override
    public void setClassifier(MetaPointer classifier) {
        delegate.setClassifier(classifier);
    }

    @Override
    public void setAnnotations(List<String> annotationIDs) {
        delegate.setAnnotations(annotationIDs);
    }

    @Override
    public String getID() {
        return delegate.getID();
    }

    @Override
    public void setID(String id) {
        delegate.setID(id);
    }

    @Override
    public void setPropertyValue(MetaPointer property, String serializedValue) {
        delegate.setPropertyValue(property, serializedValue);
    }

    @Override
    public void addChildren(MetaPointer containment, List<String> childrenIds) {
        delegate.addChildren(containment, childrenIds);
    }

    @Override
    public void addReferenceValue(MetaPointer reference, List<SerializedReferenceValue.Entry> referenceValues) {
        delegate.addReferenceValue(reference, referenceValues);
    }

    @Override
    public String getPropertyValue(String propertyKey) {
        return delegate.getPropertyValue(propertyKey);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SerializedClassifierInstance)) return false;
        SerializedClassifierInstance that = (SerializedClassifierInstance) o;
        return Objects.equals(getID(), that.getID())
                && Objects.equals(getClassifier(), that.getClassifier())
                && Objects.equals(getParentNodeID(), that.getParentNodeID())
                && Objects.equals(getProperties(), that.getProperties())
                && Objects.equals(getContainments(), that.getContainments())
                && Objects.equals(getReferences(), that.getReferences())
                && Objects.equals(getAnnotations(), that.getAnnotations());
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public String toString() {
        return "SerializedClassifierInstance{"
                + "id='" + getID()
                + "', parentNodeID='" + getParentNodeID()
                + "',\n classifier=" + getClassifier()
                + ",\n properties="
                + getProperties().stream().map(Objects::toString).collect(Collectors.joining(",\n"))
                + ",\n containments="
                + getContainments().stream().map(Objects::toString).collect(Collectors.joining(",\n"))
                + ",\n references="
                + getReferences().stream().map(Objects::toString).collect(Collectors.joining(",\n"))
                + ",\n annotations="
                + getAnnotations().stream().map(Objects::toString).collect(Collectors.joining(",\n"))
                + "'\n}";

    }

}
