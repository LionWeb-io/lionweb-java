package io.lionweb.json.sorted;

import io.lionweb.lioncore.java.serialization.data.SerializedChunk;
import io.lionweb.lioncore.java.serialization.data.SerializedClassifierInstance;
import io.lionweb.lioncore.java.serialization.data.UsedLanguage;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * View of {@link SortedSerializedChunk#delegate } with sorted {@link SortedSerializedChunk#getLanguages() } and {@link SortedSerializedChunk#getClassifierInstances() }.
 */
public class SortedSerializedChunk extends SerializedChunk {
    private final SerializedChunk delegate;

    public SortedSerializedChunk(SerializedChunk delegate) {
        this.delegate = delegate;
    }

    @Override
    public List<UsedLanguage> getLanguages() {
        return delegate.getLanguages().stream().map(new Function<UsedLanguage, SortedUsedLanguage>() {
            public SortedUsedLanguage apply(UsedLanguage it) {
                return new SortedUsedLanguage(it);
            }
        }).sorted().collect(Collectors.<UsedLanguage>toList());
    }

    @Override
    public List<SerializedClassifierInstance> getClassifierInstances() {
        return delegate.getClassifierInstances().stream().map(new Function<SerializedClassifierInstance, SortedSerializedClassifierInstance>() {
            public SortedSerializedClassifierInstance apply(SerializedClassifierInstance it) {
                return new SortedSerializedClassifierInstance(it);
            }
        }).sorted().collect(Collectors.<SerializedClassifierInstance>toList());
    }

    @Override
    public void setSerializationFormatVersion(String value) {
        delegate.setSerializationFormatVersion(value);
    }

    @Override
    public String getSerializationFormatVersion() {
        return delegate.getSerializationFormatVersion();
    }

    @Override
    public void addClassifierInstance(SerializedClassifierInstance instance) {
        delegate.addClassifierInstance(instance);
    }

    @Override
    public SerializedClassifierInstance getInstanceByID(String instanceID) {
        return delegate.getInstanceByID(instanceID);
    }

    @Override
    public Map<String, SerializedClassifierInstance> getClassifierInstancesByID() {
        return delegate.getClassifierInstancesByID();
    }

    @Override
    public void addLanguage(UsedLanguage language) {
        delegate.addLanguage(language);
    }

    @Override
    public String toString() {
        return "SerializationBlock{\n"
                + "serializationFormatVersion='" + getSerializationFormatVersion()
                + "',\n languages=\n"
                + getLanguages().stream().map(Object::toString).collect(Collectors.joining(",\n"))
                + ",\n classifierInstances=\n"
                + getClassifierInstances().stream().map(Object::toString).collect(Collectors.joining(",\n"))
                + "\n}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SerializedChunk)) return false;
        SerializedChunk that = (SerializedChunk) o;
        return getSerializationFormatVersion().equals(that.getSerializationFormatVersion())
                && getLanguages().equals(that.getLanguages())
                && getClassifierInstances().equals(that.getClassifierInstances());
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }
}
